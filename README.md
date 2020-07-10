# SKILL-CONTRACTS
This repository contains all leftshiftone contracts used by their skills

[![CircleCI branch](https://img.shields.io/circleci/project/github/leftshiftone/skill-contracts/master.svg?style=flat-square)](https://circleci.com/gh/leftshiftone/skill-contracts)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/leftshiftone/skill-contracts.svg?style=flat-square)](https://github.com/leftshiftone/skill-contracts/tags)

[![Bintray](https://img.shields.io/badge/dynamic/json.svg?label=bintray&query=name&style=flat-square&url=https%3A%2F%2Fapi.bintray.com%2Fpackages%2Fleftshiftone%2Fskill-contracts%2Fone.leftshift.skill.skill-contracts%2Fversions%2F_latest)](https://bintray.com/leftshiftone/skill-contracts/one.leftshift.skill.skill-contracts/_latestVersion)



## Development

### Release
Releases are triggered locally. Just a tag will be pushed and CI takes care of the rest.

#### Major
Run `./gradlew final -x bintrayUpload -Prelease.scope=major` locally.

#### Minor
Run `./gradlew final -x bintrayUpload -Prelease.scope=minor` locally.

#### Patch
Must be released from branch (e.g. `release/1.0.x`)

Run `./gradlew final -x bintrayUpload -Prelease.scope=patch` locally.
