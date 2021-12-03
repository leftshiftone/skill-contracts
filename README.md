# SKILL-CONTRACTS
This repository contains all leftshiftone contracts used by their skills

[![CircleCI branch](https://img.shields.io/circleci/project/github/leftshiftone/skill-contracts/master.svg?style=flat-square)](https://circleci.com/gh/leftshiftone/skill-contracts)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/leftshiftone/skill-contracts.svg?style=flat-square)](https://github.com/leftshiftone/skill-contracts/tags)



## Development

### Release
Releases are triggered locally. Just a tag will be pushed and CI takes care of the rest.

#### Major
Run `./gradlew final -x sendReleaseEmail -Prelease.scope=major` locally.

#### Minor

Run `./gradlew final -x sendReleaseEmail -Prelease.scope=minor` locally.

#### Patch

Must be released from branch (e.g. `release/1.0.x`)

Run `./gradlew final -x sendReleaseEmail -Prelease.scope=patch` locally.
