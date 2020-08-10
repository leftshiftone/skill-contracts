import com.fasterxml.jackson.databind.ObjectMapper
import dynabuffers.Dynabuffers
import dynabuffers.DynabuffersEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ValidationTest {

    companion object {
        @JvmStatic
        val INCOMING_NS = "incoming"
        @JvmStatic
        val OUTGOING_NS = "outgoing"
    }

    @TestFactory
    fun `Test`() = listOf(
            TestCase("ner", "/leftshiftone/ner"),
            TestCase("ner-box", "/leftshiftone/ner-box")
    ).map {testCase ->
        DynamicTest.dynamicTest("TestCase: ${testCase.name}, path: ${testCase.path} ") {
            val schema= "schema.dbs"
            val engine = Dynabuffers.parse(ValidationTest::class.java.getResourceAsStream("${testCase.path}${File.separator}$schema"))
            testIncoming(engine, testCase.path)
            testOutgoing(engine, testCase.path)
        }
    }

    fun testIncoming(engine: DynabuffersEngine, testCasePath: String){
        val messageAsString=ValidationTest::class.java.getResourceAsStream("${testCasePath}${File.separator}__validation__${File.separator}valid_request.data").use{
            BufferedReader(InputStreamReader(it)).readLines().joinToString(" ")
        }
        testSerialization(engine, messageAsString, INCOMING_NS)
    }

    fun testOutgoing(engine: DynabuffersEngine, testCasePath: String){
        val messageAsString=ValidationTest::class.java.getResourceAsStream("${testCasePath}${File.separator}__validation__${File.separator}valid_response.data").use{
            BufferedReader(InputStreamReader(it)).readLines().joinToString(" ")
        }
        testSerialization(engine, messageAsString, OUTGOING_NS)
    }

    fun testSerialization(engine: DynabuffersEngine, message: String, namespace: String){
        val messageMap = preprocess(ObjectMapper().readValue(message, Map::class.java))
        val bytes = engine.serialize(namespace, messageMap as Map<String,Any>)
        val deserializedMap = engine.deserialize(namespace, bytes)
        assertMap(deserializedMap.toMap(), messageMap)
    }

    fun assertMap(actualMap: Map<String, Any?>, expectedMap: Map<String, Any?>) {
        actualMap.map { entry ->
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
