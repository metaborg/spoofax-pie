[![GitHub license](https://img.shields.io/github/license/metaborg/spoofax-pie)](https://github.com/metaborg/spoofax-pie/blob/master/LICENSE)
[![Jenkins](https://img.shields.io/jenkins/build/https/buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master)](https://buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master/lastBuild)
[![Jenkins Tests](https://img.shields.io/jenkins/tests/https/buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master)](https://buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master/lastBuild/testReport/)
[![Spoofax 3 core](https://img.shields.io/maven-metadata/v?label=spoofax.core&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.core%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/org.metaborg/spoofax.core?repo=metaborg-releases)
[![Documentation](https://img.shields.io/badge/docs-latest-brightgreen)](https://metaborg.github.io/spoofax-pie/develop/)

# Spoofax 3

Spoofax 3 is a _modular_ and _incremental_ textual language workbench running on the JVM: a collection of tools and Java libraries that enable the development of textual languages, embeddable into batch compilers, code editors and IDEs, or custom applications.
It is a reimplementation of [Spoofax 2](http://spoofax.org), with the goal of being more modular, flexible, and correctly incremental.

Currently, Spoofax 3 is experimental and still a work-in-progress.
Therefore, it does not have a stable API, lacks documentation and test coverage, and has not yet been applied to real-world use cases.
If you are looking for a more mature alternative, see [Spoofax 2](http://spoofax.org), which Spoofax 3 is based on.

**See the (incomplete/under construction) [documentation website](https://metaborg.github.io/spoofax-pie/develop/) for the motivation, key ideas, and current status of Spoofax 3**.

## Copyright and License

Copyright © 2018-2021 Delft University of Technology

The code and files in this project are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this project in compliance with the license.

## Directory structure

```
.
├── build.gradle.kts        # Gradle build configuration
├── settings.gradle.kts     # Gradle build settings (multi-project and composite build)
├── core                    # Spoofax 3 core libraries
├── metalib                 # Spoofax 3 meta libraries, in a separate composite build because it
│                             uses a Gradle plugin from core.
├── lwb                     # Spoofax 3 language workbench, in a separate composite build because
│                             it uses a Gradle plugin from core.
├── example                 # Spoofax 3 examples, in a separate composite build because
│                             it uses Gradle plugins from core and lwb.
├── gradle.properties       # Properties f
├── mkdocs.yml              # Documentation configuration file (MkDocs)
├── mkdocs_requirements.txt # Documentation Python requirements file
├── docs                    # Documentation root
├── .github                 # GitHub issue template and action workflows
├── LICENSE                 # License file
├── NOTICE                  # License NOTICE file
├── CHANGELOG.md            # Changelog
└── README.md
```
