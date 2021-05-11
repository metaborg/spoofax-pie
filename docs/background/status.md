# Current Status

We have stated our key ideas, but since Spoofax 3 is still under heavy development, they have not all been implemented yet.
We now discuss the current status of Spoofax 3 by summarizing the key ideas and whether they has been implemented, along with any comments.

* [x] **Decoupling**: Spoofax Core not depend on any meta-components. Language implementations instead depend on the meta-components they require.
* [x] **Flexible, modular and incremental pipelines**: Use [PIE](https://github.com/metaborg/pie).
* [x] **Static loading**: Use static loading by default, making language implementation plain JAR files, which are easy to use in the Java ecosystem.
* [x] **`LanguageInstance` interface**: Language implementations must implement the `LanguageInstance` interface, which a platform library uses to integrate a language with the platform.
  * An initial version of the `LanguageInstance` interface exists, but this interface is not yet stable and will receive many new features.
  * Currently, this interface contains features pertaining both command-line platforms and IDE/code editor platforms. These may be split up in the future.
* [x] **Generate Java boilerplate**: Generate the Java boilerplate that Spoofax 3 now requires due to the `LanguageInstance` interface and language implementations being plain JAR files.
  * Configuration for the Spoofax 3 compiler is provided through a Gradle build script, which is verbose.
* [ ] **Quick language prototyping**: Support dynamic language loading in environments that support this, to enable quick language prototyping.
* [ ] **Configuration DSL**: Use a configuration DSL to improve the developer/user experience.
* [ ] **Error origin tracking**: Perform origin tracking and propagation on errors to improve the developer/user experience.
  * Errors are traced through most PIE tasks, but do not always contain specific origin information
* [x] **Commands**: More flexible and incremental version of "builders" from Spoofax 2.
  * [x] **Non-Stratego commands**: Commands execute PIE tasks, which execute Java code.
  * [x] **Incremental commands**: Commands are incremental because they execute PIE tasks.
  * [x] **Separate commands from how they are executed**: Commands can be bound to IDE/editor menus, command-line commands, or to resource changes.
  * [x] **Command parameters/arguments**: Commands can specify parameters, which must be provided as arguments when executed.
* [x] **Modular and incremental development**: Use Gradle (instead of Maven) to build Spoofax 3, which increases modularity and provides incremental builds for faster iteration times.

Furthermore, we now discuss the status of features that were not new key ideas.

* [x] Language builds
  * We have an initial version of a Spoofax 3 compiler which is completely based on Spoofax 3 and PIE (independent from Spoofax 2), but it is still unstable.
* [ ] Meta-language bootstrapping
  * Bootstrapping requires implementation of the meta-languages in Spoofax 3, which we have not done yet.
* Meta-tools
  * Syntax specification
    * [x] SDF3
  * Parsing
    * [x] JSGLR1
    * [ ] JSGLR2
      * [ ] Incremental parsing
    * [ ] New water rules
  * Styling specification
    * [x] ESV (syntax-based)
  * Semantic analysis
    * [x] NaBL2
    * [x] Statix
      * [ ] Statix signature generation based on SDF3 specification
    * [ ] FlowSpec
    * [x] Stratego
  * Transformation (compilation)
    * [x] Stratego
* Editor services
  * [x] Syntax-based styling
  * [x] Inline error/warning/note messages
  * [ ] Code completion
  * [ ] Outline
* Platforms
  * [x] Command-line
  * [ ] Eclipse
    * An Eclipse plugin for your language is provided, but it not yet mature
    * Concurrency/parallelism is completely ignored. Therefore, things may run concurrently that are not suppose to which cause data races and crashes.
  * [ ] IntelliJ
    * A very minimal IntelliJ plugin for your language is provided, currently only supporting syntax highlighting and inline parse errors.
  * [ ] Gradle
  * [ ] Maven
  * [ ] REPL

The following features are being prototyped/experimented with Spoofax 3:

* Multi-lingual semantic analysis with Statix (Aron Zwaan)
* Semantic code completion based on Statix specification (Daniel Pelsmaeker)

The following features will most likely not be supported:

* Analysis with NaBL/TS
