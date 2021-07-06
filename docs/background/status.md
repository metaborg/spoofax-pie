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
    * Configuration for Spoofax 3 language implementations based on Spoofax 2 language definitions is provided through a Gradle build script, which is verbose.
* [x] **Quick language prototyping**: Support dynamic language loading in environments that support this, to enable quick language prototyping.
* [x] **Configuration DSL**: Use a configuration DSL to improve the developer/user experience.
* [x] **Error origin tracking**: Perform origin tracking and propagation on errors to improve the developer/user experience.
    * Not all PIE tasks trace errors, and some errors do not have location information yet.
* [x] **Commands**: More flexible and incremental version of "builders" from Spoofax 2.
    * [x] **Non-Stratego commands**: Commands execute PIE tasks, which execute Java code.
    * [x] **Incremental commands**: Commands are incremental because they execute PIE tasks.
    * [x] **Separate commands from how they are executed**: Commands can be bound to IDE/editor menus, command-line commands, or to resource changes.
    * [x] **Command parameters/arguments**: Commands can specify parameters, which must be provided as arguments when executed.
* [x] **Modular and incremental development**: Use Gradle (instead of Maven) to build Spoofax 3, which increases modularity and provides incremental builds for faster iteration times.
    * Certain changes to core components may trigger long rebuilds, as a lot of projects (indirectly) depend on these core components and require recompilation.
    * Certain changes trigger recompilation of Gradle plugins which are required by the rest of the build. This may cause a long configuration phase which is not parallelized.
    * Our Gradle plugins do not support the Gradle build cache yet.
    * Our Gradle plugins do not support the configuration cache yet.
    * Sometimes multiple imports into IntelliJ are required to have it recognize all dependencies.

Furthermore, we now discuss the status of features that were not new key ideas.

* [x] Language builds
* [ ] Meta-language bootstrapping
    * Bootstrapping requires implementation of the meta-languages in Spoofax 3, which we have not done yet.
* Meta-tools
    * Syntax specification
        * [x] SDF3
    * Parsing
        * [x] JSGLR1
        * [x] JSGLR2
            * [x] Incremental parsing (but incompatible with recovery)
    * Styling specification
        * [x] ESV (syntax-based)
    * Semantic analysis
        * [x] NaBL2
            * Only supported for Spoofax 2-based language definitions
        * [x] Statix
            * [ ] Statix signature generation based on SDF3 specification
        * [ ] FlowSpec
        * [x] Stratego
  * Transformation (compilation)
      * [x] Stratego 2
  * Testing
      * [x] SPT
          * Not all expectations have been ported over yet
* Editor services
    * [x] Syntax-based styling
    * [x] Inline error/warning/note messages
    * [ ] Code completion
    * [ ] Outline
* Platforms
    * [x] Command-line
    * [x] Eclipse
        * Concurrency/parallelism is mostly ignored. Therefore, things may run concurrently that are not suppose to which cause data races and crashes.
        * Several editor services and other conveniences are still missing or work in progress.
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
