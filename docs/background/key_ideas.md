# Key ideas

To solve the problems highlighted in the motivation section, we intend to employ the following key ideas in Spoofax 3.

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
