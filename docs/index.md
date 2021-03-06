# Introduction

Spoofax 3 is a _modular_ and _incremental_ textual language workbench running on the JVM: a collection of tools and Java libraries that enable the development of textual languages, embeddable into batch compilers, code editors and IDEs, or custom applications.
It is a reimplementation of [Spoofax 2](http://spoofax.org), with the goal of being more modular, flexible, and correctly incremental.

Currently, Spoofax 3 is experimental and still a work-in-progress.
Therefore, it does not have a stable API, lacks documentation and test coverage, and has not yet been applied to real-world use cases.
If you are looking for a more mature alternative, see [Spoofax 2](http://spoofax.org), which Spoofax 3 is based on.


## Motivation

We first discuss the motivations for developing Spoofax 3.

### Architecture

The main motivation for developing Spoofax 3 is the monolithic, inflexible, and non-incremental architecture of Spoofax 2:

* It has an *inflexible fixed-function pipeline*, where every file of your language is parsed, analyzed, and transformed.
  This works fine, and can even be incremental when the files of your language can be separately compiled.
  However, this is often not the case.
  Languages should be able to define their own incremental pipelines with minimal effort.
  Those pipelines should be modular and flexible, enabling usage in a wide range applications such as command-line interfaces, build systems, code editors, and IDEs.

* It is monolithic for *language users* (i.e., the users of your programming language that you have developed with Spoofax), as every language developed with Spoofax 2 depends on Spoofax Core, which in turn depends on all meta-components: JSGLR1 and 2, NaBL+TS index and task engine, NaBL2 & Statix solver, dynsem interpreter, Stratego runtime, config parsing libraries, etc.
  A language should only require the meta-components that it uses.

* It is monolithic for *meta-component developers* (e.g., the developers of the language workbench, or researchers experimenting with new meta-tools or meta-languages).
  New meta-components need to be tightly integrated into Spoofax Core, requiring time-consuming changes and introducing increased coupling.
  We should develop meta-components in separation, and loosely couple/integrate them (as much as possible).

* The *build of Spoofax 2* itself is monolithic and non-incremental, as all its components are compiled in one huge non-incremental build, massively increasing iteration time during development.
  The build must be incremental, and components should be separated where possible to reduce coupling, decreasing iteration times.

### Language loading

Furthermore, Spoofax 2 *only support dynamic loading of languages*, where a language can be (re)loaded into the running environment.
This is very useful during language development, as it enables fast prototyping.
However, when we want to statically load the language, we still need to perform the dynamic loading ritual: somehow include the language archive in your application, and then load it at runtime.
This hurts startup time, is not supported on some platforms (e.g., Graal native image), and is tedious.
We should support both static and dynamic loading (where possible).

### Error tracing

Some errors are not being traced back to their source.
For example, many errors in configuration only show up during build time (in the console, which may be hidden) and are not traced back to the configuration file.
This confuses users, and may get stuck on simple things, which then require help from us.
Errors, warnings, and informational messages should be traced back to their source, and shown inline at the source in IDE environments, or shown as a list of messages with origin information on the command-line.
When there are errors, execution should continue in certain instances (e.g., parse error should recover and try to do analysis), but should not in others (e.g., error from static analysis should prevent execution since it could crash).

### Configuration

Another issue is the scattered configuration in language specifications, which is spread over many different places:

* `metaborg.yaml`
* `editor/*.esv`
* `dynsem.properties`
* In meta-languages files. For example, template options in SDF3.
* `pom.xml`
* `.mvn/extensions.xml`

Finding the right configuration option in these files, and keeping them in sync, is tedious.
Furthermore, while most configuration is documented on our website, looking that up still causes a cognitive gap.
We should consolidate configuration that belongs together, and not have any duplicate configuration that needs to be kept in sync.
Configuration should be supported with editor services such as inline errors and code completion, if possible.

Moreover, some parts of a language specification are configured by convention, and these conventions cannot be changed.
For example, the main SDF3 file is always assumed to be `syntax/<language-name>.sdf3`.
When the language name is changed, but we forget to change the name of this main file, no parse table is built.
Configuration conventions should be changeable, and defaults should be persisted to ensure that renamings do not break things.

### Summary of Problems

To summarize, Spoofax 2 suffers from the following problems that form the motivation for Spoofax 3:

* Monolithic, inflexible, and non-incremental architecture causing:
    * Inflexible and slow language processing due to non-incremental fixed-function pipeline
    * Coupling in Spoofax Core: every language depends on Spoofax Core, and Spoofax Core depends on all meta-components
    * Slow iteration times when developing Spoofax 2 due to its monolithic and non-incremental build
    * Tedious to use languages due to dynamic language loading
* Confusing (end-)user experience due to:
    * Bad error traceability
    * Scattered configuration
    * Non-incremental configuration (restarts required to update configuration)


## Key ideas

To solve these problems, we intend to employ the following key ideas in Spoofax 3.

To reduce coupling, Spoofax 3's "Spoofax Core" does not depend on any meta-components.
Instead, a language implementation depends directly on the meta-components that it requires.
For example, the Tiger language implementation depends directly on the JSGLR2 parser, the NaBL2 constraint solver, and the Stratego runtime.

To make language pipelines flexible, modular, and incremental, we use an incremental, modular, and expressive build system as the basis for creating pipelines: [PIE](https://github.com/metaborg/pie).
Language processing steps such as parsing, styling text, analyzing, checking (to provide inline error messages), running (parts of) a compiler, etc. become PIE task definitions.
Tasks, which are instances of these task definitions, can depend on each other, and depend on resources such as files.
The PIE runtime efficiently and incrementally executes tasks.
Furthermore, task definitions can be shared and used by other language implementations, making language implementations modular.

To reduce the tedium of dynamic language loading, we instead choose to do static language loading as the default.
A language implementation is just a JAR file that can be put on the classpath and used as a regular Java library.
For example, to use the JSGLR2 parser of the Tiger language, we just depend on the Tiger language implementation as we would depend on a regular Java library, create an instance of the `TigerParser` class, and then use that to parse a string into an AST.

We still want to automatically provide integrations with the command-line, build systems such as Gradle, and IDEs such as Eclipse and IntelliJ.
Therefore, every language implementation must implement the `LanguageInstance` interface.
Spoofax 3 then provides libraries which take a `LanguageInstance` object, and integrate it with a platform.
For example, `spoofax.cli` takes a `LanguageInstance` object and provides a command-line application, and `spoofax.eclipse` does the same for an Eclipse plugin.

Because language implementations are just regular Java libraries, they now require some Java boilerplate.
However, we do not want language developers to write this Java boilerplate for standard cases.
Therefore, we employ a Spoofax 3 compiler that generates this Java boilerplate.
If the language developer is not happy with the implementation, or wants to customize parts, they can manually implement or extend Java classes where needed.
It is also possible to not use the Spoofax 3 compiler at all, and manually implement all parts.

To enable quick language prototyping, we still support dynamic language loading in environments that support them (e.g., Eclipse and IntelliJ), by dynamically loading the language implementation JAR when changed.
For example, when prototyping the Tiger language in Eclipse, if the syntax definition is changed we run the Spoofax 3 compiler to (incrementally) create a new parse table and Java classes, and dynamically (re)load the JAR.

To improve the user experience, we use a configuration DSL to configure language specifications and implementations.
Thereby configuration is centralized, has domain-specific checking, and editor services such as inline errors and code completion.
We also allow changing of defaults (conventions), and persist them to enable renaming.

To improve error traceability, errors are reported inline where possible.
Errors are traced through PIE pipelines and support origin tracking to easily support error traceability and inline errors for all language implementations.

TODO: better builders:
non-Stratego commands
incremental commands
separate commands from how they are executed
support command parameters/arguments
continuous execution

TODO: modular and incremental development of Spoofax 3 itself with Gradle

## Current Status

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

## Anatomy of a Spoofax 3 language implementation

In this subsection, we give a high-level overview of what a Spoofax 3 language implementation is, dive into details, and explain how such a implementation can be manually written or completely generated from a high-level language specification

### Overview

In essence, a language implementation in Spoofax 3 is nothing more than a standard Java library (e.g., a JAR file) with Java classes implementing or delegating to the various functionalities of the language such as parsing and transformations, as well as bundled resources such as a parse table which is loaded and interpreted at runtime.

Therefore, Spoofax 3 language implementations are very easy to use in the Java ecosystem by just distributing the JAR file of the language, or by publishing/consuming it as a library with a build system such as Gradle.
Furthermore, since no classloading or class generation is used, GraalVM native image can be used to ahead-of-time compile your language implementation into native code which does not require a JVM at all, and significantly reduces the startup time of your language.

Diving deeper, a language implementation is actually split into three parts: a __language project__ that contains the base functionality of the language, an __adapter project__ that adapts the language project to the interface of Spoofax, and __platform projects__ that plug the adapter project into various other platforms such as a command-line interface (CLI) and Eclipse plugin (TODO: more details on supported platforms in a separate section).
We will first explain these projects and why this separation was chosen.

TODO: diagram?

### Language Project

A language project contains the _base functionality_ of a language, such as a parser, syntax highlighter, analyzer, and compiler for the language.
Such a project is _unstructured_: it does not have to adhere to any interface or data format. Therefore, it may use any tooling, libraries, and data structures to implement the base functionality.
This facilitates integration of existing tools and minimal dependencies.

A language project is just a Java library and can thus be used in a standalone fashion.
However, there is no glue between base functionality, requiring manual implementation of a parse-analyze-compile pipeline for example.
Furthermore, because the project is unstructured, we cannot provide any integration with other platforms such as a CLI and Eclipse plugin.
Therefore, using a language project as a standalone library is a bit of a niche use case for when minimal dependencies or full control is absolutely necessary.
Because it is such a niche use case, the default in Spoofax 3 is to merge it together with the adapter project.

In essence, an adapter project adapts a language project to Spoofax 3.
To understand why, we first explain the high-level architecture of Spoofax 3.

### Spoofax 3 architecture overview

Spoofax 3 provides a general interface for language implementations: `LanguageInstance`, which is used by platforms to automatically plug languages into their platform.
For example, `LanguageInstance` has functionality for _syntax highlighting_, which when given a resource of the language, returns a syntax highlighting for that resource.
(TODO: more details on the functionality in `LanguageInstance` in a separate section)

Furthermore, Spoofax 3 uses [PIE](https://github.com/metaborg/pie); a framework for building incremental pipelines, build systems, and compilers; to incrementalize the language implementation.
Instead of directly computing the syntax highlighting for a resource, we create a _task_ that returns the syntax highlighting when demanded, with PIE taking care of whether it should recompute the syntax highlighting because the resource (or the syntax highlighting implementation) changed, or if it can just be returned from a cache.
(TODO: more details on PIE in a separate section)

A platform such as Eclipse or IntelliJ can then take a `LanguageInstance` implementation, demand the syntax highlighting task, and show the result it in the editor for your language.
Therefore, any language that implements `LanguageInstance` can get syntax highlighting in Eclipse, IntelliJ, and any other supported platforms for free, with PIE taking care of coarse-grained incrementalization.

To receive the benefits of Spoofax 3, the adapter project must thus be implemented for your language.

### Adapter Project

An adapter project implements Spoofax 3's `LanguageInstance` using the language project. This requires glue code between the unstructured language project and the _structured_ `LanguageInstance` interface.
For example, you would need to convert the data structure that the syntax highlighter of your language returns, to one that Spoofax 3 understands: the `Styling` class.
Furthermore, because Spoofax 3 uses PIE, we also need to implement a PIE _task definition_ that implements the (re)computing of syntax highlighting, as well as mark all dependencies that should cause the syntax highlighting to be recomputed.

We also need to be able to instantiate your implementation of `LanguageInstance`.
In case this is non-trivial, the recommended practice is to use dependency injection to achieve proper separation of concerns.
A dependency injection framework such as [Dagger](https://dagger.dev/) is recommended (we use it extensively in Spoofax 3) because it catches dependency injection errors at compile-time, and does not require runtime class loading or generation.

This may sound like you would need to write a lot of boilerplate.
However, we provide a compiler that generates all this boilerplate for you.
It is only necessary to write this boilerplate if you are integrating existing tooling.
Even then, the compiler can generate some of the boilerplate for you.
More details on the compiler can be found in the developing language implementations section.

### Platform Projects

TODO: every language-platform combination is a separate project to support ahead-of-time compilation, static loading, and customization of the platform project.
CLI: can be ahead-of-time compiled with GraalVM native image to create a native Windows/macOS/Linux CLI for your language
Eclipse/IntelliJ: statically loaded plugin that can be deployed with Eclipse/IntelliJ, and can be fully customized.

### Developing Language Implementations

So far we have talked about what a language implementation is, but not yet how one is developed, which we will dive into now.

Language implementations are by default fully generated from a high-level language specification using the Spoofax 3 compiler, thereby supporting iterative language development with low boilerplate. However, it is possible implement parts of or even the entire language/adapter project by hand, facilitating the integration of existing tools and languages.

TODO: dynamic loading
