[![GitHub license](https://img.shields.io/github/license/metaborg/spoofax-pie)](https://github.com/metaborg/spoofax-pie/blob/master/LICENSE)
[![GitHub actions](https://img.shields.io/github/actions/workflow/status/metaborg/spoofax-pie/build.yml?branch=master)](https://github.com/metaborg/spoofax-pie/actions/workflows/build.yml)
[![Jenkins](https://img.shields.io/jenkins/build/https/buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master?label=Jenkins)](https://buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master/lastBuild)
[![Jenkins Tests](https://img.shields.io/jenkins/tests/https/buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master?label=Jenkins%20tests)](https://buildfarm.metaborg.org/job/metaborg/job/spoofax-pie/job/master/lastBuild/testReport/)
[![Spoofax 3 core](https://img.shields.io/maven-metadata/v?label=spoofax.core&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.core%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/org.metaborg/spoofax.core?repo=metaborg-releases)
[![Documentation](https://img.shields.io/badge/docs-latest-brightgreen)](https://metaborg.github.io/spoofax-pie/develop/)

# Spoofax 3

Spoofax 3 is a _modular_ and _incremental_ textual language workbench running on the JVM: a collection of tools and Java libraries that enable the development of textual languages, embeddable into batch compilers, code editors and IDEs, or custom applications.
It is a reimplementation of [Spoofax 2](http://spoofax.org), with the goal of being more modular, flexible, and correctly incremental.

Currently, Spoofax 3 is experimental and still a work-in-progress.
Therefore, it does not have a stable API, lacks documentation and test coverage, and has not yet been applied to real-world use cases.
If you are looking for a more mature alternative, see [Spoofax 2](http://spoofax.org), which Spoofax 3 is based on.

**See the (incomplete/under construction) [documentation website](https://metaborg.github.io/spoofax-pie/develop/) for the motivation, key ideas, and current status of Spoofax 3**.

## Directory structure

```
.
├── build.gradle.kts        # Gradle build configuration
├── settings.gradle.kts     # Gradle build settings (multi-project and composite build)
├── core                    # Spoofax 3 core libraries
├── lwb                     # Spoofax 3 language workbench, in a separate composite build because
│                             it uses Gradle plugins from core.
├── lwb.distrib             # Spoofax 3 language workbench distribution, in a separate composite
│                             build because it uses Gradle plugins from core and lwb.
├── example                 # Spoofax 3 examples, in a separate composite build because
│                             it uses Gradle plugins from core and lwb.
├── gradle.properties       # Gradle properties file
├── mkdocs.yml              # Documentation configuration file (MkDocs)
├── mkdocs_requirements.txt # Documentation Python requirements file
├── docs                    # Documentation root
├── .github                 # GitHub issue template and action workflows
├── LICENSE                 # License file
├── NOTICE                  # License NOTICE file
├── CHANGELOG.md            # Changelog
└── README.md
```

## Development

### Git conventions
The `master` branch of this repository is buildable in isolation, and is used to publish new releases.
The `develop` branch of this repository is built via the [devenv repository](https://github.com/metaborg/devenv), against the `develop` branch of other repositories that are part of devenv, and is used for development.
Other branches are feature branches and should be merged into `develop` at some point.


### Building
The `master` branch of this repository can be built in isolation.
However, the `develop` branch must be built via the [devenv repository](https://github.com/metaborg/devenv), due to it depending on development versions of other projects.

This repository is built with Gradle, which requires a JDK of at least version 8 to be installed. Higher versions may work depending on [which version of Gradle is used](https://docs.gradle.org/current/userguide/compatibility.html).

To build this repository, run:

```shell
./repo checkout
./gradlew buildAll
```

> [!NOTE]
> On Windows, instead run:
>
> ```shell
> repo.bat checkout
> gradlew.bat buildAll
> ```

> [!IMPORTANT]
> When using MacOS, ensure you have Docker installed and running.

To run a local Eclipse instance using the Spoofax version in this repository, run:

```shell
./gradlew :spoofax3.lwb.distrib.root:spoofax.lwb.eclipse:runEclipse
```

> [!NOTE]
> On Windows, instead run:
>
> ```shell
> gradlew.bat :spoofax3.lwb.distrib.root:spoofax.lwb.eclipse:runEclipse
> ```

To build the documentation, see [docs/README.md](docs/README.md).


### Automated Builds
This repository is built on:
- [GitHub actions](https://github.com/metaborg/spoofax-pie/actions/workflows/build.yml) via `.github/workflows/build.yml`. Only the `master` branch is built here.
- Our [Jenkins buildfarm](https://buildfarm.metaborg.org/view/Devenv/job/metaborg/job/spoofax-pie/) via `Jenkinsfile` which uses our [Jenkins pipeline library](https://github.com/metaborg/jenkins.pipeline/).

### Publishing

This repository is published via Gradle and Git with the [Gitonium](https://github.com/metaborg/gitonium) and [Gradle Config](https://github.com/metaborg/gradle.config) plugins.
It is published to our [artifact server](https://artifacts.metaborg.org) in the [releases repository](https://artifacts.metaborg.org/content/repositories/releases/).

First, ensure that you depend on only released versions of other projects. That is, no `SNAPSHOT` or other development versions.
Most dependencies are managed in the `core/spoofax.depconstraints/build.gradle.kts` file.
Spoofax 2 versions are managed in `gradle.properties`:
- `systemProp.spoofax2Version` sets the version of Spoofax 2 that Spoofax 3 uses, for Spoofax 2 artifacts. At the moment of writing, this is only the `org.metaborg:strategoxt-min-jar` artifact.
- `systemProp.spoofax2DevenvVersion` sets the version of Spoofax 2 that Spoofax 3 uses, which is built as part of the `spoofax3` branch of [spoofax-deploy](https://github.com/metaborg/spoofax-deploy/tree/spoofax3/gradle).

Then, update `CHANGELOG.md` with your changes, create a new release entry, and update the release links at the bottom of the file.
Commit your changes and merge them from the `develop` branch into the `master` branch.

To make a new release, create a tag in the form of `release-*` where `*` is the version of the release you'd like to make.
Then first build the project with `./gradlew buildAll` to check if building succeeds.

If you want our buildfarm to publish this release, just push the tag you just made, and our buildfarm will build the repository and publish the release.

If you want to publish this release locally, you will need an account with write access to our artifact server, and tell Gradle about this account.
Create the `~/.gradle/gradle.properties` file if it does not exist.
Add the following lines to it, replacing `<username>` and `<password>` with those of your artifact server account:
```
publish.repository.metaborg.artifacts.username=<username>
publish.repository.metaborg.artifacts.password=<password>
```
Then run `./gradlew publishAll` to publish all built artifacts.
You should also push the release tag you made such that this release is reproducible by others.

Finally, add the release to the documentation by adding the release and date to the top of the `release_versions` dictionary in `docs/macro.py`.
Push this change to the `develop` branch and a GitHub actions build will automatically update the [documentation website](https://metaborg.github.io/spoofax-pie/develop/download/).

## Copyright and License

Copyright © 2018-2022 Delft University of Technology

The files in this repository are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this repository in compliance with the license.
