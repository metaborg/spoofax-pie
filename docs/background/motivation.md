# Motivation

In this section we discuss the motivations for developing Spoofax 3.

## Architecture

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

## Language loading

Furthermore, Spoofax 2 *only support dynamic loading of languages*, where a language can be (re)loaded into the running environment.
This is very useful during language development, as it enables fast prototyping.
However, when we want to statically load the language, we still need to perform the dynamic loading ritual: somehow include the language archive in your application, and then load it at runtime.
This hurts startup time, is not supported on some platforms (e.g., Graal native image), and is tedious.
We should support both static and dynamic loading (where possible).

## Error tracing

Some errors are not being traced back to their source.
For example, many errors in configuration only show up during build time (in the console, which may be hidden) and are not traced back to the configuration file.
This confuses users, and may get stuck on simple things, which then require help from us.
Errors, warnings, and informational messages should be traced back to their source, and shown inline at the source in IDE environments, or shown as a list of messages with origin information on the command-line.
When there are errors, execution should continue in certain instances (e.g., parse error should recover and try to do analysis), but should not in others (e.g., error from static analysis should prevent execution since it could crash).

## Configuration

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

## Summary of Problems

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
