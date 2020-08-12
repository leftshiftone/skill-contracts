import com.fasterxml.jackson.databind.ObjectMapper
import dynabuffers.Dynabuffers
import dynabuffers.DynabuffersEngine
import dynabuffers.exception.DynabuffersException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception

class ValidationTest {

    companion object {
        const val INCOMING_NS = "incoming"
        const val OUTGOING_NS = "outgoing"
        const val TEST_FOLDER_NAME = "__tests__"
    }

    @TestFactory
    fun `Verify valid requests`() = File(ValidationTest::class.java.getResource("/leftshiftone").path).listFiles()
                .filter { it.isDirectory }
                .filter { File("$it${File.separator}schema.dbs").exists() }
                .filter { File("$it${File.separator}${TEST_FOLDER_NAME}").exists() }
                .filter { File("$it${File.separator}${TEST_FOLDER_NAME}").listFiles().filter { it.name.startsWith("valid_") }.count()> 0 }
                .map {
                    val testCase1 = TestCase(it.name, it.path)
                    DynamicTest.dynamicTest("TestCase: ${testCase1.name}, path: ${testCase1.path} ") {
                        val schema = "schema.dbs"
                        val engine = Dynabuffers.parse(File("${testCase1.path}${File.separator}$schema").inputStream())
                        testIncoming(engine, testCase1.path)
                        testOutgoing(engine, testCase1.path)
                    }
                }

    @TestFactory
    fun `validate bad requests`() = File(ValidationTest::class.java.getResource("/leftshiftone").path).listFiles()
            .filter { it.isDirectory }
            .filter { File("$it${File.separator}schema.dbs").exists() }
            .filter { File("$it${File.separator}${TEST_FOLDER_NAME}").exists() }
            .filter { File("$it${File.separator}${TEST_FOLDER_NAME}").listFiles().filter { it.name.startsWith("bad_") }.count()> 0 }
            .map {
                val testCase = TestCase(it.name, it.path)
                DynamicTest.dynamicTest("TestCase: ${testCase.name}, path: ${testCase.path} ") {
                    val schema = "schema.dbs"
                    val engine = Dynabuffers.parse(File("${testCase.path}${File.separator}$schema").inputStream())
                    assertDynabuffersException(engine, testCase.path,"bad_request", INCOMING_NS)
                    assertDynabuffersException(engine, testCase.path,"bad_response", OUTGOING_NS)
                }
            }


    fun assertDynabuffersException(engine: DynabuffersEngine, testCasePath: String, fileNamePrefix: String, namespace: String){
        try{
            testNamespaceSerialization(engine, testCasePath ,fileNamePrefix, namespace)
            assertThat(true).isFalse()
        }catch (ex: Exception){
            assertThat(ex).isExactlyInstanceOf(DynabuffersException::class.java)
        }

    }
    fun testNamespaceSerialization(engine: DynabuffersEngine, testCasePath: String, fileNamePrefix: String, namespace: String){
        readCaseFiles("${testCasePath}${File.separator}${TEST_FOLDER_NAME}",fileNamePrefix)
                .map { messageAsString-> testSerialization(engine, messageAsString, namespace) }

    }
    fun testIncoming(engine: DynabuffersEngine, testCasePath: String, fileNamePrefix: String="valid_request") = testNamespaceSerialization(engine,testCasePath, fileNamePrefix, INCOMING_NS)
    fun testOutgoing(engine: DynabuffersEngine, testCasePath: String,fileNamePrefix: String="valid_response") = testNamespaceSerialization(engine,testCasePath, fileNamePrefix, OUTGOING_NS)

    fun readCaseFiles(path: String, fileNamePattern: String) = File(path)
            .listFiles()
            .filter { it.isFile }
            .filter { it.name.contains(fileNamePattern) }
            .filter { it.name.endsWith("data") }
            .map {
                it.inputStream()
                .use {
                    BufferedReader(InputStreamReader(it)).readLines().joinToString(" ")
                }
            }.toList()

    fun testSerialization(engine: DynabuffersEngine, message: String, namespace: String){
        val messageMap = preprocess(ObjectMapper().readValue(message, Map::class.java))
        val bytes = engine.serialize(namespace, messageMap as Map<String,Any>)
        val deserializedMap = engine.deserialize(namespace, bytes)
        assertMap(deserializedMap.toMap(), messageMap)
    }

    fun assertMap(actualMap: Map<String, Any?>, expectedMap: Map<String, Any?>) {
        actualMap
                .filter {it.key == ":type"}
                .map { entry ->
                assertThat(expectedMap).containsKey(entry.key)
                assertThat(expectedMap.get(entry.key)).isEqualTo(entry.value)
            }
        }




    fun preprocess(obj: Any?) : Any? {
        val value = when(obj) {
            null -> return null
            is Collection<*> -> preprocess(obj.toTypedArray())
            is Array<*> -> return obj.map { preprocess(it) }.toTypedArray()
            is Double -> obj.toFloat()
            is Map<*, *> -> {
                val preprocessMap= mutableMapOf<String,Any?>()
                obj.map {
                    val value= preprocess(it.value)
                    preprocessMap.put(it.key as String, value)
                }
                return preprocessMap
            }
            else -> obj
        }
         return value
    }

}

data class TestCase(val name: String, val path: String)
