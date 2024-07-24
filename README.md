# Spoofax 3
[![Build][github-badge:build]][github:build]
[![License][license-badge]][license]
[![GitHub Release][github-badge:release]][github:release]
[![Documentation][documentation-badge]][documentation]

Spoofax 3, a _modular_ and _incremental_ textual language workbench running on the JVM.

This is a collection of tools and Java libraries that enable the development of textual languages, embeddable into batch compilers, code editors and IDEs, or custom applications. It is a reimplementation of [Spoofax 2](https://spoofax.dev/), with the goal of being more modular, flexible, and correctly incremental.

Currently, Spoofax 3 is experimental and still a work-in-progress. Therefore, it does not have a stable API, lacks documentation and test coverage, and has not yet been applied to real-world use cases. If you are looking for a more mature alternative, see [Spoofax 2](http://spoofax.org), which Spoofax 3 is based on.

[![Documentation][documentation-button]][documentation]

| Artifact                                        | Latest Release                                                                                                                           |
|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `org.metaborg:spoofax.common`                   | [![org.metaborg:spoofax.common][maven-badge:spoofax.common]][maven:spoofax.common]                                                       |
| `org.metaborg:aterm.common`                     | [![org.metaborg:aterm.common][maven-badge:aterm.common]][maven:aterm.common]                                                             |
| `org.metaborg:jsglr.common`                     | [![org.metaborg:jsglr.common][maven-badge:jsglr.common]][maven:jsglr.common]                                                             |
| `org.metaborg:jsglr.pie`                        | [![org.metaborg:jsglr.pie][maven-badge:jsglr.pie]][maven:jsglr.pie]                                                                      |
| `org.metaborg:jsglr1.common`                    | [![org.metaborg:jsglr1.common][maven-badge:jsglr1.common]][maven:jsglr1.common]                                                          |
| `org.metaborg:jsglr2.common`                    | [![org.metaborg:jsglr2.common][maven-badge:jsglr2.common]][maven:jsglr2.common]                                                          |
| `org.metaborg:esv.common`                       | [![org.metaborg:esv.common][maven-badge:esv.common]][maven:esv.common]                                                                   |
| `org.metaborg:stratego.common`                  | [![org.metaborg:stratego.common][maven-badge:stratego.common]][maven:stratego.common]                                                    |
| `org.metaborg:stratego.pie`                     | [![org.metaborg:stratego.pie][maven-badge:stratego.pie]][maven:stratego.pie]                                                             |
| `org.metaborg:constraint.common`                | [![org.metaborg:constraint.common][maven-badge:constraint.common]][maven:constraint.common]                                              |
| `org.metaborg:constraint.pie`                   | [![org.metaborg:constraint.pie][maven-badge:constraint.pie]][maven:constraint.pie]                                                       |
| `org.metaborg:nabl2.common`                     | [![org.metaborg:nabl2.common][maven-badge:nabl2.common]][maven:nabl2.common]                                                             |
| `org.metaborg:statix.codecompletion`            | [![org.metaborg:statix.codecompletion][maven-badge:statix.codecompletion]][maven:statix.codecompletion]                                  |
| `org.metaborg:statix.codecompletion.pie`        | [![org.metaborg:statix.codecompletion.pie][maven-badge:statix.codecompletion.pie]][maven:statix.codecompletion.pie]                      |
| `org.metaborg:statix.common`                    | [![org.metaborg:statix.common][maven-badge:statix.common]][maven:statix.common]                                                          |
| `org.metaborg:statix.pie`                       | [![org.metaborg:statix.pie][maven-badge:statix.pie]][maven:statix.pie]                                                                   |
| `org.metaborg:statix.multilang`                 | [![org.metaborg:statix.multilang][maven-badge:statix.multilang]][maven:statix.multilang]                                                 |
| `org.metaborg:statix.multilang.eclipse`         | [![org.metaborg:statix.multilang.eclipse][maven-badge:statix.multilang.eclipse]][maven:statix.multilang.eclipse]                         |
| `org.metaborg:spt.api`                          | [![org.metaborg:spt.api][maven-badge:spt.api]][maven:spt.api]                                                                            |
| `org.metaborg:tego.runtime`                     | [![org.metaborg:tego.runtime][maven-badge:tego.runtime]][maven:tego.runtime]                                                             |
| `org.metaborg:spoofax2.common`                  | [![org.metaborg:spoofax2.common][maven-badge:spoofax2.common]][maven:spoofax2.common]                                                    |
| `org.metaborg:tooling.eclipsebundle`            | [![org.metaborg:tooling.eclipsebundle][maven-badge:tooling.eclipsebundle]][maven:tooling.eclipsebundle]                                  |
| `org.metaborg:transform.pie`                    | [![org.metaborg:transform.pie][maven-badge:transform.pie]][maven:transform.pie]                                                          |
| `org.metaborg:spoofax.core`                     | [![org.metaborg:spoofax.core][maven-badge:spoofax.core]][maven:spoofax.core]                                                             |
| `org.metaborg:spoofax.resource`                 | [![org.metaborg:spoofax.resource][maven-badge:spoofax.resource]][maven:spoofax.resource]                                                 |
| `org.metaborg:spoofax.test`                     | [![org.metaborg:spoofax.test][maven-badge:spoofax.test]][maven:spoofax.test]                                                             |
| `org.metaborg:spoofax.cli`                      | [![org.metaborg:spoofax.cli][maven-badge:spoofax.cli]][maven:spoofax.cli]                                                                |
| `org.metaborg:spoofax.intellij`                 | [![org.metaborg:spoofax.intellij][maven-badge:spoofax.intellij]][maven:spoofax.intellij]                                                 |
| `org.metaborg:spoofax.eclipse`                  | [![org.metaborg:spoofax.eclipse][maven-badge:spoofax.eclipse]][maven:spoofax.eclipse]                                                    |
| `org.metaborg:spoofax.compiler`                 | [![org.metaborg:spoofax.compiler][maven-badge:spoofax.compiler]][maven:spoofax.compiler]                                                 |
| `org.metaborg:spoofax.compiler.spoofax2`        | [![org.metaborg:spoofax.compiler.spoofax2][maven-badge:spoofax.compiler.spoofax2]][maven:spoofax.compiler.spoofax2]                      |
| `org.metaborg:spoofax.compiler.spoofax2.dagger` | [![org.metaborg:spoofax.compiler.spoofax2.dagger][maven-badge:spoofax.compiler.spoofax2.dagger]][maven:spoofax.compiler.spoofax2.dagger] |
| `org.metaborg:spoofax.compiler.interfaces`      | [![org.metaborg:spoofax.compiler.interfaces][maven-badge:spoofax.compiler.interfaces]][maven:spoofax.compiler.interfaces]                |
| `org.metaborg:spoofax.compiler.gradle`          | [![org.metaborg:spoofax.compiler.gradle][maven-badge:spoofax.compiler.gradle]][maven:spoofax.compiler.gradle]                            |
| `org.metaborg:spoofax.compiler.gradle.spoofax2` | [![org.metaborg:spoofax.compiler.gradle.spoofax2][maven-badge:spoofax.compiler.gradle.spoofax2]][maven:spoofax.compiler.gradle.spoofax2] |
| `org.metaborg:spoofax.compiler.eclipsebundle`   | [![org.metaborg:spoofax.compiler.eclipsebundle][maven-badge:spoofax.compiler.eclipsebundle]][maven:spoofax.compiler.eclipsebundle]       |
| `org.metaborg:spoofax.lwb.compiler`             | [![org.metaborg:spoofax.lwb.compiler][maven-badge:spoofax.lwb.compiler]][maven:spoofax.lwb.compiler]                                     |
| `org.metaborg:spoofax.lwb.compiler.gradle`      | [![org.metaborg:spoofax.lwb.compiler.gradle][maven-badge:spoofax.lwb.compiler.gradle]][maven:spoofax.lwb.compiler.gradle]                |
| `org.metaborg:spoofax.lwb.dynamicloading`       | [![org.metaborg:spoofax.lwb.dynamicloading][maven-badge:spoofax.lwb.dynamicloading]][maven:spoofax.lwb.dynamicloading]                   |
| `org.metaborg:cfg`                              | [![org.metaborg:cfg][maven-badge:cfg]][maven:cfg]                                                                                        |
| `org.metaborg:cfg.cli`                          | [![org.metaborg:cfg.cli][maven-badge:cfg.cli]][maven:cfg.cli]                                                                            |
| `org.metaborg:cfg.eclipse`                      | [![org.metaborg:cfg.eclipse][maven-badge:cfg.eclipse]][maven:cfg.eclipse]                                                                |
| `org.metaborg:cfg.intellij`                     | [![org.metaborg:cfg.intellij][maven-badge:cfg.intellij]][maven:cfg.intellij]                                                             |
| `org.metaborg:cfg.spoofax2`                     | [![org.metaborg:cfg.spoofax2][maven-badge:cfg.spoofax2]][maven:cfg.spoofax2]                                                             |
| `org.metaborg:dynamix`                          | [![org.metaborg:dynamix][maven-badge:dynamix]][maven:dynamix]                                                                            |
| `org.metaborg:dynamix.cli`                      | [![org.metaborg:dynamix.cli][maven-badge:dynamix.cli]][maven:dynamix.cli]                                                                |
| `org.metaborg:dynamix.eclipse`                  | [![org.metaborg:dynamix.eclipse][maven-badge:dynamix.eclipse]][maven:dynamix.eclipse]                                                    |
| `org.metaborg:dynamix.intellij`                 | [![org.metaborg:dynamix.intellij][maven-badge:dynamix.intellij]][maven:dynamix.intellij]                                                 |
| `org.metaborg:dynamix.spoofax2`                 | [![org.metaborg:dynamix.spoofax2][maven-badge:dynamix.spoofax2]][maven:dynamix.spoofax2]                                                 |
| `org.metaborg:sdf3_ext_dynamix`                 | [![org.metaborg:sdf3_ext_dynamix][maven-badge:sdf3_ext_dynamix]][maven:sdf3_ext_dynamix]                                                 |
| `org.metaborg:sdf3_ext_dynamix.eclipse`         | [![org.metaborg:sdf3_ext_dynamix.eclipse][maven-badge:sdf3_ext_dynamix.eclipse]][maven:sdf3_ext_dynamix.eclipse]                         |
| `org.metaborg:sdf3_ext_dynamix.spoofax2`        | [![org.metaborg:sdf3_ext_dynamix.spoofax2][maven-badge:sdf3_ext_dynamix.spoofax2]][maven:sdf3_ext_dynamix.spoofax2]                      |
| `org.metaborg:sdf3`                             | [![org.metaborg:sdf3][maven-badge:sdf3]][maven:sdf3]                                                                                     |
| `org.metaborg:sdf3.cli`                         | [![org.metaborg:sdf3.cli][maven-badge:sdf3.cli]][maven:sdf3.cli]                                                                         |
| `org.metaborg:sdf3.eclipse`                     | [![org.metaborg:sdf3.eclipse][maven-badge:sdf3.eclipse]][maven:sdf3.eclipse]                                                             |
| `org.metaborg:sdf3.intellij`                    | [![org.metaborg:sdf3.intellij][maven-badge:sdf3.intellij]][maven:sdf3.intellij]                                                          |
| `org.metaborg:stratego`                         | [![org.metaborg:stratego][maven-badge:stratego]][maven:stratego]                                                                         |
| `org.metaborg:stratego.cli`                     | [![org.metaborg:stratego.cli][maven-badge:stratego.cli]][maven:stratego.cli]                                                             |
| `org.metaborg:stratego.eclipse`                 | [![org.metaborg:stratego.eclipse][maven-badge:stratego.eclipse]][maven:stratego.eclipse]                                                 |
| `org.metaborg:stratego.intellij`                | [![org.metaborg:stratego.intellij][maven-badge:stratego.intellij]][maven:stratego.intellij]                                              |
| `org.metaborg:esv`                              | [![org.metaborg:esv][maven-badge:esv]][maven:esv]                                                                                        |
| `org.metaborg:esv.cli`                          | [![org.metaborg:esv.cli][maven-badge:esv.cli]][maven:esv.cli]                                                                            |
| `org.metaborg:esv.eclipse`                      | [![org.metaborg:esv.eclipse][maven-badge:esv.eclipse]][maven:esv.eclipse]                                                                |
| `org.metaborg:esv.intellij`                     | [![org.metaborg:esv.intellij][maven-badge:esv.intellij]][maven:esv.intellij]                                                             |
| `org.metaborg:statix`                           | [![org.metaborg:statix][maven-badge:statix]][maven:statix]                                                                               |
| `org.metaborg:statix.cli`                       | [![org.metaborg:statix.cli][maven-badge:statix.cli]][maven:statix.cli]                                                                   |
| `org.metaborg:statix.eclipse`                   | [![org.metaborg:statix.eclipse][maven-badge:statix.eclipse]][maven:statix.eclipse]                                                       |
| `org.metaborg:statix.intellij`                  | [![org.metaborg:statix.intellij][maven-badge:statix.intellij]][maven:statix.intellij]                                                    |
| `org.metaborg:sdf3_ext_statix`                  | [![org.metaborg:sdf3_ext_statix][maven-badge:sdf3_ext_statix]][maven:sdf3_ext_statix]                                                    |
| `org.metaborg:sdf3_ext_statix.eclipse`          | [![org.metaborg:sdf3_ext_statix.eclipse][maven-badge:sdf3_ext_statix.eclipse]][maven:sdf3_ext_statix.eclipse]                            |
| `org.metaborg:spt`                              | [![org.metaborg:spt][maven-badge:spt]][maven:spt]                                                                                        |
| `org.metaborg:spt.dynamicloading`               | [![org.metaborg:spt.dynamicloading][maven-badge:spt.dynamicloading]][maven:spt.dynamicloading]                                           |
| `org.metaborg:spt.cli`                          | [![org.metaborg:spt.cli][maven-badge:spt.cli]][maven:spt.cli]                                                                            |
| `org.metaborg:spt.eclipse`                      | [![org.metaborg:spt.eclipse][maven-badge:spt.eclipse]][maven:spt.eclipse]                                                                |
| `org.metaborg:spt.intellij`                     | [![org.metaborg:spt.intellij][maven-badge:spt.intellij]][maven:spt.intellij]                                                             |
| `org.metaborg:libspoofax2`                      | [![org.metaborg:libspoofax2][maven-badge:libspoofax2]][maven:libspoofax2]                                                                |
| `org.metaborg:libspoofax2.eclipse`              | [![org.metaborg:libspoofax2.eclipse][maven-badge:libspoofax2.eclipse]][maven:libspoofax2.eclipse]                                        |
| `org.metaborg:libstatix`                        | [![org.metaborg:libstatix][maven-badge:libstatix]][maven:libstatix]                                                                      |
| `org.metaborg:libstatix.eclipse`                | [![org.metaborg:libstatix.eclipse][maven-badge:libstatix.eclipse]][maven:libstatix.eclipse]                                              |
| `org.metaborg:strategolib`                      | [![org.metaborg:strategolib][maven-badge:strategolib]][maven:strategolib]                                                                |
| `org.metaborg:strategolib.eclipse`              | [![org.metaborg:strategolib.eclipse][maven-badge:strategolib.eclipse]][maven:strategolib.eclipse]                                        |
| `org.metaborg:gpp`                              | [![org.metaborg:gpp][maven-badge:gpp]][maven:gpp]                                                                                        |
| `org.metaborg:gpp.eclipse`                      | [![org.metaborg:gpp.eclipse][maven-badge:gpp.eclipse]][maven:gpp.eclipse]                                                                |



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
- `systemProp.spoofax2DevenvVersion` sets the version of Spoofax 2 that Spoofax 3 uses, which is built as part of the `master` branch of [spoofax-deploy](https://github.com/metaborg/spoofax-deploy/).

**NOTE:** If you're updating the `spoofax2Version` because you want to use some changes in a new Spoofax 2 release, you most likely will need to update to `spoofax2DevenvVersion` too, and in order to do so need to follow the above link and follow the instructions for releasing a new devenv version for the Spoofax 2 artifacts.

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


## License
Copyright 2018-2024 [Programming Languages Group](https://pl.ewi.tudelft.nl/), [Delft University of Technology](https://www.tudelft.nl/)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.

This product includes software developed at The Apache Software Foundation (http://www.apache.org/), copyright (C) 2001-2019 The Apache Software Foundation.

[github-badge:build]: https://img.shields.io/github/actions/workflow/status/metaborg/spoofax-pie/build.yaml
[github:build]: https://github.com/metaborg/spoofax-pie/actions
[license-badge]: https://img.shields.io/github/license/metaborg/spoofax-pie
[license]: https://github.com/metaborg/spoofax-pie/blob/master/LICENSE
[github-badge:release]: https://img.shields.io/github/v/release/metaborg/spoofax-pie
[github:release]: https://github.com/metaborg/spoofax-pie/releases
[documentation-badge]: https://img.shields.io/badge/docs-latest-brightgreen
[documentation]: https://metaborg.github.io/spoofax-pie/develop/
[documentation-button]: https://img.shields.io/badge/Documentation-blue?style=for-the-badge&logo=googledocs&logoColor=white



[maven:spoofax.common]:                   https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.common~~~
[maven:aterm.common]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~aterm.common~~~
[maven:jsglr.common]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~jsglr.common~~~
[maven:jsglr.pie]:                        https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~jsglr.pie~~~
[maven:jsglr1.common]:                    https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~jsglr1.common~~~
[maven:jsglr2.common]:                    https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~jsglr2.common~~~
[maven:esv.common]:                       https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~esv.common~~~
[maven:stratego.common]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~stratego.common~~~
[maven:stratego.pie]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~stratego.pie~~~
[maven:constraint.common]:                https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~constraint.common~~~
[maven:constraint.pie]:                   https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~constraint.pie~~~
[maven:nabl2.common]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~nabl2.common~~~
[maven:statix.codecompletion]:            https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.codecompletion~~~
[maven:statix.codecompletion.pie]:        https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.codecompletion.pie~~~
[maven:statix.common]:                    https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.common~~~
[maven:statix.pie]:                       https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.pie~~~
[maven:statix.multilang]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.multilang~~~
[maven:statix.multilang.eclipse]:         https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.multilang.eclipse~~~
[maven:spt.api]:                          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spt.api~~~
[maven:tego.runtime]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~tego.runtime~~~
[maven:spoofax2.common]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax2.common~~~
[maven:tooling.eclipsebundle]:            https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~tooling.eclipsebundle~~~
[maven:transform.pie]:                    https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~transform.pie~~~
[maven:spoofax.core]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.core~~~
[maven:spoofax.resource]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.resource~~~
[maven:spoofax.test]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.test~~~
[maven:spoofax.cli]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.cli~~~
[maven:spoofax.intellij]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.intellij~~~
[maven:spoofax.eclipse]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.eclipse~~~
[maven:spoofax.compiler]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler~~~
[maven:spoofax.compiler.spoofax2]:        https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler.spoofax2~~~
[maven:spoofax.compiler.spoofax2.dagger]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler.spoofax2.dagger~~~
[maven:spoofax.compiler.interfaces]:      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler.interfaces~~~
[maven:spoofax.compiler.gradle]:          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler.gradle~~~
[maven:spoofax.compiler.gradle.spoofax2]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler.gradle.spoofax2~~~
[maven:spoofax.compiler.eclipsebundle]:   https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.compiler.eclipsebundle~~~
[maven:spoofax.lwb.compiler]:             https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.lwb.compiler~~~
[maven:spoofax.lwb.compiler.gradle]:      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.lwb.compiler.gradle~~~
[maven:spoofax.lwb.dynamicloading]:       https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spoofax.lwb.dynamicloading~~~
[maven:cfg]:                              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~cfg~~~
[maven:cfg.cli]:                          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~cfg.cli~~~
[maven:cfg.eclipse]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~cfg.eclipse~~~
[maven:cfg.intellij]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~cfg.intellij~~~
[maven:cfg.spoofax2]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~cfg.spoofax2~~~
[maven:dynamix]:                          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~dynamix~~~
[maven:dynamix.cli]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~dynamix.cli~~~
[maven:dynamix.eclipse]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~dynamix.eclipse~~~
[maven:dynamix.intellij]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~dynamix.intellij~~~
[maven:dynamix.spoofax2]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~dynamix.spoofax2~~~
[maven:sdf3_ext_dynamix]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3_ext_dynamix~~~
[maven:sdf3_ext_dynamix.eclipse]:         https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3_ext_dynamix.eclipse~~~
[maven:sdf3_ext_dynamix.spoofax2]:        https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3_ext_dynamix.spoofax2~~~
[maven:sdf3]:                             https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3~~~
[maven:sdf3.cli]:                         https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3.cli~~~
[maven:sdf3.eclipse]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3.eclipse~~~
[maven:sdf3.intellij]:                    https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3.intellij~~~
[maven:stratego]:                         https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~stratego~~~
[maven:stratego.cli]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~stratego.cli~~~
[maven:stratego.eclipse]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~stratego.eclipse~~~
[maven:stratego.intellij]:                https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~stratego.intellij~~~
[maven:esv]:                              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~esv~~~
[maven:esv.cli]:                          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~esv.cli~~~
[maven:esv.eclipse]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~esv.eclipse~~~
[maven:esv.intellij]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~esv.intellij~~~
[maven:statix]:                           https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix~~~
[maven:statix.cli]:                       https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.cli~~~
[maven:statix.eclipse]:                   https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.eclipse~~~
[maven:statix.intellij]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~statix.intellij~~~
[maven:sdf3_ext_statix]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3_ext_statix~~~
[maven:sdf3_ext_statix.eclipse]:          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~sdf3_ext_statix.eclipse~~~
[maven:spt]:                              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spt~~~
[maven:spt.dynamicloading]:               https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spt.dynamicloading~~~
[maven:spt.cli]:                          https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spt.cli~~~
[maven:spt.eclipse]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spt.eclipse~~~
[maven:spt.intellij]:                     https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~spt.intellij~~~
[maven:libspoofax2]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~libspoofax2~~~
[maven:libspoofax2.eclipse]:              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~libspoofax2.eclipse~~~
[maven:libstatix]:                        https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~libstatix~~~
[maven:libstatix.eclipse]:                https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~libstatix.eclipse~~~
[maven:strategolib]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~strategolib~~~
[maven:strategolib.eclipse]:              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~strategolib.eclipse~~~
[maven:gpp]:                              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~gpp~~~
[maven:gpp.eclipse]:                      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~gpp.eclipse~~~


[maven-badge:spoofax.common]:                   https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.common%2Fmaven-metadata.xml
[maven-badge:aterm.common]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Faterm.common%2Fmaven-metadata.xml
[maven-badge:jsglr.common]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fjsglr.common%2Fmaven-metadata.xml
[maven-badge:jsglr.pie]:                        https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fjsglr.pie%2Fmaven-metadata.xml
[maven-badge:jsglr1.common]:                    https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fjsglr1.common%2Fmaven-metadata.xml
[maven-badge:jsglr2.common]:                    https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fjsglr2.common%2Fmaven-metadata.xml
[maven-badge:esv.common]:                       https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fesv.common%2Fmaven-metadata.xml
[maven-badge:stratego.common]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstratego.common%2Fmaven-metadata.xml
[maven-badge:stratego.pie]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstratego.pie%2Fmaven-metadata.xml
[maven-badge:constraint.common]:                https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fconstraint.common%2Fmaven-metadata.xml
[maven-badge:constraint.pie]:                   https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fconstraint.pie%2Fmaven-metadata.xml
[maven-badge:nabl2.common]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fnabl2.common%2Fmaven-metadata.xml
[maven-badge:statix.codecompletion]:            https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.codecompletion%2Fmaven-metadata.xml
[maven-badge:statix.codecompletion.pie]:        https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.codecompletion.pie%2Fmaven-metadata.xml
[maven-badge:statix.common]:                    https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.common%2Fmaven-metadata.xml
[maven-badge:statix.pie]:                       https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.pie%2Fmaven-metadata.xml
[maven-badge:statix.multilang]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.multilang%2Fmaven-metadata.xml
[maven-badge:statix.multilang.eclipse]:         https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.multilang.eclipse%2Fmaven-metadata.xml
[maven-badge:spt.api]:                          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspt.api%2Fmaven-metadata.xml
[maven-badge:tego.runtime]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Ftego.runtime%2Fmaven-metadata.xml
[maven-badge:spoofax2.common]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax2.common%2Fmaven-metadata.xml
[maven-badge:tooling.eclipsebundle]:            https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Ftooling.eclipsebundle%2Fmaven-metadata.xml
[maven-badge:transform.pie]:                    https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Ftransform.pie%2Fmaven-metadata.xml
[maven-badge:spoofax.core]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.core%2Fmaven-metadata.xml
[maven-badge:spoofax.resource]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.resource%2Fmaven-metadata.xml
[maven-badge:spoofax.test]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.test%2Fmaven-metadata.xml
[maven-badge:spoofax.cli]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.cli%2Fmaven-metadata.xml
[maven-badge:spoofax.intellij]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.intellij%2Fmaven-metadata.xml
[maven-badge:spoofax.eclipse]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.eclipse%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler.spoofax2]:        https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler.spoofax2%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler.spoofax2.dagger]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler.spoofax2.dagger%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler.interfaces]:      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler.interfaces%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler.gradle]:          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler.gradle%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler.gradle.spoofax2]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler.gradle.spoofax2%2Fmaven-metadata.xml
[maven-badge:spoofax.compiler.eclipsebundle]:   https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.compiler.eclipsebundle%2Fmaven-metadata.xml
[maven-badge:spoofax.lwb.compiler]:             https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.lwb.compiler%2Fmaven-metadata.xml
[maven-badge:spoofax.lwb.compiler.gradle]:      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.lwb.compiler.gradle%2Fmaven-metadata.xml
[maven-badge:spoofax.lwb.dynamicloading]:       https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspoofax.lwb.dynamicloading%2Fmaven-metadata.xml
[maven-badge:cfg]:                              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fcfg%2Fmaven-metadata.xml
[maven-badge:cfg.cli]:                          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fcfg.cli%2Fmaven-metadata.xml
[maven-badge:cfg.eclipse]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fcfg.eclipse%2Fmaven-metadata.xml
[maven-badge:cfg.intellij]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fcfg.intellij%2Fmaven-metadata.xml
[maven-badge:cfg.spoofax2]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fcfg.spoofax2%2Fmaven-metadata.xml
[maven-badge:dynamix]:                          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fdynamix%2Fmaven-metadata.xml
[maven-badge:dynamix.cli]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fdynamix.cli%2Fmaven-metadata.xml
[maven-badge:dynamix.eclipse]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fdynamix.eclipse%2Fmaven-metadata.xml
[maven-badge:dynamix.intellij]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fdynamix.intellij%2Fmaven-metadata.xml
[maven-badge:dynamix.spoofax2]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fdynamix.spoofax2%2Fmaven-metadata.xml
[maven-badge:sdf3_ext_dynamix]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3_ext_dynamix%2Fmaven-metadata.xml
[maven-badge:sdf3_ext_dynamix.eclipse]:         https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3_ext_dynamix.eclipse%2Fmaven-metadata.xml
[maven-badge:sdf3_ext_dynamix.spoofax2]:        https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3_ext_dynamix.spoofax2%2Fmaven-metadata.xml
[maven-badge:sdf3]:                             https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3%2Fmaven-metadata.xml
[maven-badge:sdf3.cli]:                         https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3.cli%2Fmaven-metadata.xml
[maven-badge:sdf3.eclipse]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3.eclipse%2Fmaven-metadata.xml
[maven-badge:sdf3.intellij]:                    https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3.intellij%2Fmaven-metadata.xml
[maven-badge:stratego]:                         https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstratego%2Fmaven-metadata.xml
[maven-badge:stratego.cli]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstratego.cli%2Fmaven-metadata.xml
[maven-badge:stratego.eclipse]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstratego.eclipse%2Fmaven-metadata.xml
[maven-badge:stratego.intellij]:                https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstratego.intellij%2Fmaven-metadata.xml
[maven-badge:esv]:                              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fesv%2Fmaven-metadata.xml
[maven-badge:esv.cli]:                          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fesv.cli%2Fmaven-metadata.xml
[maven-badge:esv.eclipse]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fesv.eclipse%2Fmaven-metadata.xml
[maven-badge:esv.intellij]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fesv.intellij%2Fmaven-metadata.xml
[maven-badge:statix]:                           https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix%2Fmaven-metadata.xml
[maven-badge:statix.cli]:                       https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.cli%2Fmaven-metadata.xml
[maven-badge:statix.eclipse]:                   https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.eclipse%2Fmaven-metadata.xml
[maven-badge:statix.intellij]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstatix.intellij%2Fmaven-metadata.xml
[maven-badge:sdf3_ext_statix]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3_ext_statix%2Fmaven-metadata.xml
[maven-badge:sdf3_ext_statix.eclipse]:          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fsdf3_ext_statix.eclipse%2Fmaven-metadata.xml
[maven-badge:spt]:                              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspt%2Fmaven-metadata.xml
[maven-badge:spt.dynamicloading]:               https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspt.dynamicloading%2Fmaven-metadata.xml
[maven-badge:spt.cli]:                          https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspt.cli%2Fmaven-metadata.xml
[maven-badge:spt.eclipse]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspt.eclipse%2Fmaven-metadata.xml
[maven-badge:spt.intellij]:                     https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fspt.intellij%2Fmaven-metadata.xml
[maven-badge:libspoofax2]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Flibspoofax2%2Fmaven-metadata.xml
[maven-badge:libspoofax2.eclipse]:              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Flibspoofax2.eclipse%2Fmaven-metadata.xml
[maven-badge:libstatix]:                        https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Flibstatix%2Fmaven-metadata.xml
[maven-badge:libstatix.eclipse]:                https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Flibstatix.eclipse%2Fmaven-metadata.xml
[maven-badge:strategolib]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstrategolib%2Fmaven-metadata.xml
[maven-badge:strategolib.eclipse]:              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fstrategolib.eclipse%2Fmaven-metadata.xml
[maven-badge:gpp]:                              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fgpp%2Fmaven-metadata.xml
[maven-badge:gpp.eclipse]:                      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fgpp.eclipse%2Fmaven-metadata.xml

