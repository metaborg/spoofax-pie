# Anatomy of a language implementation

In this section we give a high-level overview of what a Spoofax 3 language implementation is, dive into details, and explain how such a implementation can be manually written or completely generated from a high-level language specification

## Overview

In essence, a language implementation in Spoofax 3 is nothing more than a standard Java library (e.g., a JAR file) with Java classes implementing or delegating to the various functionalities of the language such as parsing and transformations, as well as bundled resources such as a parse table which is loaded and interpreted at runtime.

Therefore, Spoofax 3 language implementations are very easy to use in the Java ecosystem by just distributing the JAR file of the language, or by publishing/consuming it as a library with a build system such as Gradle.
Furthermore, since no classloading or class generation is used, GraalVM native image can be used to ahead-of-time compile your language implementation into native code which does not require a JVM at all, and significantly reduces the startup time of your language.

Diving deeper, a language implementation is actually split into three parts: a __language project__ that contains the base functionality of the language, an __adapter project__ that adapts the language project to the interface of Spoofax, and __platform projects__ that plug the adapter project into various other platforms such as a command-line interface (CLI) and Eclipse plugin (TODO: more details on supported platforms in a separate section).
We will first explain these projects and why this separation was chosen.

TODO: diagram?

## Language Project

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

## Spoofax 3 architecture overview

Spoofax 3 provides a general interface for language implementations: `LanguageInstance`, which is used by platforms to automatically plug languages into their platform.
For example, `LanguageInstance` has functionality for _syntax highlighting_, which when given a resource of the language, returns a syntax highlighting for that resource.
(TODO: more details on the functionality in `LanguageInstance` in a separate section)

Furthermore, Spoofax 3 uses [PIE](https://github.com/metaborg/pie); a framework for building incremental pipelines, build systems, and compilers; to incrementalize the language implementation.
Instead of directly computing the syntax highlighting for a resource, we create a _task_ that returns the syntax highlighting when demanded, with PIE taking care of whether it should recompute the syntax highlighting because the resource (or the syntax highlighting implementation) changed, or if it can just be returned from a cache.
(TODO: more details on PIE in a separate section)

A platform such as Eclipse or IntelliJ can then take a `LanguageInstance` implementation, demand the syntax highlighting task, and show the result it in the editor for your language.
Therefore, any language that implements `LanguageInstance` can get syntax highlighting in Eclipse, IntelliJ, and any other supported platforms for free, with PIE taking care of coarse-grained incrementalization.

To receive the benefits of Spoofax 3, the adapter project must thus be implemented for your language.

## Adapter Project

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

## Platform Projects

TODO: every language-platform combination is a separate project to support ahead-of-time compilation, static loading, and customization of the platform project.
CLI: can be ahead-of-time compiled with GraalVM native image to create a native Windows/macOS/Linux CLI for your language
Eclipse/IntelliJ: statically loaded plugin that can be deployed with Eclipse/IntelliJ, and can be fully customized.

## Developing Language Implementations

So far we have talked about what a language implementation is, but not yet how one is developed, which we will dive into now.

Language implementations are by default fully generated from a high-level language specification using the Spoofax 3 compiler, thereby supporting iterative language development with low boilerplate. However, it is possible implement parts of or even the entire language/adapter project by hand, facilitating the integration of existing tools and languages.

TODO: dynamic loading
