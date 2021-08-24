# Language definition

In essence, a language definition in Spoofax 3 consists of several source files along with configuration that specify and implement the various aspects of a language, such as its tokenizer, parser, styler, completer, checker, compiler, commands, and so forth.
The source files of a language definition are mostly written in high-level domain-specific meta-languages, with some parts being written in Java.
The Spoofax 3 compiler compiles language definitions into language implementations which are essentially standard Java libraries (e.g., JAR files) consisting of Java classes and bundled resources.
These classes implement the various aspects of a language, and may use bundled resources such as a parse table which is loaded and interpreted at runtime.

In this reference manual we explain the basic anatomy of a language definition, its configuration and file structure, Java classes and how they are instantiated, as well as how a language definition is compiled into a language implementation.


## Anatomy

configuration in CFG
syntax in SDF3
styling in ESV
static semantics in Statix
transformations in Stratego

tasks in java (future: PIE DSL)

commands, specified in configuration, generated for you

menu bindings

CLI bindings





## File structure


## Java classes

description of the Java classes, their instances, and how they are instantiated


basic classes:

ClassLoaderResources
ParseTable/ParserFactory/Parser
StylingRules/StylerFactory/Styler
StrategoRuntimeBuilderFactory
ConstraintAnalyzerFactory/ConstraintAnalyzer


components:

ResourcesComponent/ResourcesModule
Component/Module
Scope/Qualifier
how they are instantiated by Dagger


LanguageInstance:

tasks

command defs
auto command request

CLI commands

menu items


task definitions:

Tokenize
Style
Completion
Check/CheckMulti/CheckAggregator/CheckDeaggregator

Parse
Analyze/AnalyzeMulti
GetStrategoRuntimeProvider


commands:

## Instantiation

dependency injection

## Compilation

description of how a language definition is compiled into a language implementation, and what the compiled form looks like.

generate Java sources into:

build/generated/sources/language

build/generated/sources/adapter

generate Stratego sources into:

build/generated/sources/languageSpecification/stratego
