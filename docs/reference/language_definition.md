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




## Configuration

The main entry point of a language definition is the `spoofaxc.cfg` (Spoofax compiler configuration) file, written in the CFG language.
The goal of this config file is to configure basic options, enable/disable features, point to main source files of meta-languages, to add/override behaviour, and to serve as an anchor on the filesystem.
The directory that the `spoofaxc.cfg` file is in is called the "root directory" of the language definition, and any relative paths are resolved relative to that directory.

The CFG language has domain-specific syntax for configuring language definitions. However, the syntax follows these conventions:

* Options are assigned a value with `$Option = $Expression`.
* Sections `$Section { ... }` may enable features and group options.
* Lists `$List [ ..., ... ]` indicate an option/section may be given 0-many times.
* Let bindings `let $Name = $Expression` can be used to give values a name that can be (re-)used in the rest of the configuration file.

In its most basic form, the `spoofaxc.cfg` file for a language named `Calc` looks as follows:

```cfg
name = "Calc"
```

which assigns the string `"Calc"` to the `name` option.
A more interesting example configures more options and enables syntax definition:

```cfg
name = "Calc"
java-class-id-prefix = java Calc
file-extension = "calc"

sdf3 {}
parser {
  default-start-symbol = sort Program
}
```

Here, `java Calc` is assigned to the `java-class-id-prefix` option.
The `sdf3 {}` section from the example is empty, but is used to *enable* the SDF3 meta-language.
The `parser` section enables generation of a parser, and also sets the default start symbol to use to `sort Program`.

### Literals

Literals are expressions that are usually directly assigned to options, or bound to a name with let bindings.
CFG has the following literals:

| Syntax | Example(s) | Type |
| - | - | - |
| `(true|false)` | `true` `false` | Boolean |
| `"(~[\"\$\n\r\\] | \\~[\n\r])*"` | `"foo"` `"bar"` | String |
| `(./|/)~[\n\r\,\;\]\)\}\ ]*` | `./relative/file` `/absolute/file` | Filesystem path |
| `$JavaIdChars` | `Java foo` | Java identifier |
| `$JavaQIdLit` | `Java foo.bar.Baz` | Qualified Java identifier |
| `task-def $JavaQIdLit` | `task-def foo.bar.Baz` | Qualified Java identifier that represents a task definition |
| `sort [a-zA-Z0-9\-\_\$]+` | `sort Start` | SDF3 sort identifier |
| `strategy [a-zA-Z0-9\-\_\$]+` | `strategy Start` | Stratego strategy identifier |

With the following syntax non-terminals:

| Name | Syntax |
| - | - |
| `JavaIdChars` | `[a-zA-Z\_\$][a-zA-Z0-9\_\$]*` |
| `JavaQIdLit` | `$JavaIdChars(\.$JavaIdChars)*` |

For Java, SDF3 sort, and Stratego strategy identifiers, the corresponding keywords of those languages are rejected as identifiers.

### Let bindings

Let bindings of the form `let $Name = $Expression` bind a name to an expression, for example:

```cfg
let showParsedAst = task-def mb.helloworld.task.HelloWorldShowParsedAst
let showParsedAstCommand = command-def {
  task-def = showParsedAst
  ...
}
editor-context-menu [
  menu "Debug" [
    command-action {
      command-def = showParsedAstCommand
      ...
    }
  ]
]
```

creates a binding from name `showParsedAst` to `task-def mb.helloworld.task.HelloWorldShowParsedAst`, which we then pass to the `task-def` option in `command-def`.
The command in turn is bound to `showParsedAstCommand`, assigned to the `command-def` option in a `command-action` section.

### Commands

Commands are sections that are also expressions, typically assigned to a name with a let binding, with the following form:

```cfg
let $Name = command-def {
  $CommandOption*
}
```

The following options are available in a command:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `task-def = $Expression` | yes | The task definition that the command will execute. | Qualified Java identifier that represents a task definition, or qualified Java identifier |
| `type = $Expression` | no | The fully qualified Java type we want this command to be generated as. Can be omitted to generate a type based on the name of the task definition. | Qualified Java identifier |
| `display-name = $Expression` | yes | The display name of the command. | String |
| `description = $Expression` | no | The optional description of the command. | String |
| `supported-execution-types = [($ExecutionType ,)*]` | no | The optional supported execution types of the command. Defaults to `[Once, Continuous]`. | n/a |
| `args-type = $Expression` | no | The fully qualified Java type of the argument class. Can be omitted if the argument class is a nested class named `Args` of the task definition. | Qualified Java identifier |
| `parameters = [ $Parameter* ]` | yes | The description of the parameters of the command | n/a |

The following `ExecutionType`s are supported:

* `Once` indicates a that this command supports being executed as a one-shot command.
* `Continuous` indicates that this command supports being executed every time the source file changes.

A `$Parameter` has the form `$Identifier = parameter { $ParameterOptions }` with the following options:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `type = $Expression` | yes | The fully qualified Java of the type of the parameter. This must match the type of the parameter inside the `args-type` of the command. | Qualified Java identifier |
| `required = $Expression` | no | Whether the parameter is required. Defaults to `true`. | Boolean |
| `converter-type = $Expression` | no | The argument converter for this parameter, which can convert a `String` value to the `type` of this parameter. Must implement the `ArgConverter` interface. | Qualified Java identifier |
| `argument-providers = [($ArgumentProvider ,)*]` | no | Argument providers for this parameter that attempt to automatically provide a fitting argument. When providing an argument fails, the next argument provider in the list will be attempted. If no arguments can be provided, and the argument is required, then the argument *must be* provided by the user that executes the command, or executing the command will fail. | n/a |

The following `ArgumentProvider`s are supported:

* `Value($Expression)` provides a default value given by the expression. The expression must match the type of the parameter, even though this is not currently checked.
* `Context($CommandContext)` attempts to infer the argument by context. The following `CommandContext`s are supported:
    * `Directory`: attempt to infer a `ResourcePath` to an existing directory. For example, when right-clicking a directory in an IDE to execute a command on that directory.
    * `File`: attempt to infer a `ResourcePath` to an existing file. For example, when right-clicking a file in an IDE to execute a command on that directory, or when executing a command in an editor for a file.
    * `ResourceKey`: attempt to infer a `ResourceKey` to a readable resource. This is more general than `File`, as we only ask for a resource that can be read, not one that belongs to a (local) filesystem. Use this when the command does not rely on the resource being in a filesystem.
    * `Region`: attempt to infer a `Region` in a source file. Inference succeeds when the context has a selection of size 1 or larger. For example, when executing a command in an editor that has a selection, the region will be that selection.
    * `Offset`: attempt to infer an `int` representing an offset in a source file. Inference succeeds when the context has a cursor offset (i.e., a selection of size 0 or larger). For example, when executing a command in an editor, the offset will be the offset to the cursor in the editor.
* `EnclosingContext($EnclosingCommandContext)` attempts to infer the argument by the enclosing context. The following `EnclosingCommandContext`s are supported:
    * `Project`: attempt to infer a `ResourcePath` to the enclosing project. For example, when executing a command in an IDE on a file, directory, or editor for a file, that belongs to a project. Or when executing a command in a CLI, the directory will be the current working directory.
    * `Directory`: attempt to infer a `ResourcePath` to the enclosing directory. For example, when executing a command in the context of a file, directory, or editor for a file, the directory will be the parent of that file/directory.

Here is an example of a command that shows the parsed AST by taking one file argument that is inferred from context:

```cfg
let showParsedAstCommand = command-def {
  type = java mb.helloworld.command.HelloWorldShowParsedAstCommand
  task-def = showParsedAst
  args-type = java mb.helloworld.task.HelloWorldShowParsedAst.Args
  display-name = "Show parsed AST"
  description = "Shows the parsed AST"
  supported-execution-types = [Once, Continuous]
  parameters = [
    file = parameter {
      type = java mb.resource.ResourceKey
      required = true
      argument-providers = [Context(ResourceKey)]
    }
  ]
}
```

### Menu item

Menu items take the form of:

* a `separator` representing a horizontal line in a menu used to separate groups of menu items.
* a `menu $Expression [ $MenuItem* ]` representing a (nested) menu with a display name defined by the expression which must be a string, and a list of nested menu items.
* a `command-action { $CommandActionOption* }` representing an action that executes a command when a user clicks on it.

A command action has the following options:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `command-def = $Expression` | yes | The command to execute. | Command or qualified Java identifier |
| `execution-type = $ExecutionType` | yes | How the command should be executed. | n/a |
| `required-resource-types = [($ResourceType ,)*]` | no | On which kinds of resources this menu item will be shown on resource context menus. Defaults to empty. If empty, it will not be hidden based on resources. | n/a |
| `required-enclosing-resource-types = [($EnclosingResourceType ,)*]` | no | On which kinds of enclosing resources this menu item will be shown on resource context menus. Defaults to empty. If empty, it will not be hidden based on enclosing resource. | n/a |
| `required-editor-file-types = [($EditorFileType ,)*]` | no | On which kinds of editors belonging to certain file types this menu item will be shown. Defaults to empty. If empty, it will not be hidden based on editor file types. | n/a |
| `required-editor-selection-types = [($EditorSelectionType ,)*]` | no | On which kinds of editor selection types this menu item will be shown. Defaults to empty. If empty, it will not be hidden based on editor selections. | n/a |
| `display-name = $Expression` | no | The display name of the command action. Defaults to the display name of the command | String |
| `description = $Expression` | no | The description of he command action. Defaults to the description of the command | String |

The following `ResourceType`s are supported:

* `Directory`: the menu item will only be shown when a directory is selected.
* `File`: the menu item will only be shown when a file is selected.

The following `EnclosingResourceType`s are supported:

* `Project`: the menu item will only be shown when the selected resource has an enclosing project.
* `Directory:`: the menu item will only be shown when the selected resource has an enclosing directory.

The following `EditorFileType`s are supported:

* `HierarchicalResource`: the menu item will only be shown when the editor belongs to a hierarchical resource. That is, a resource that belongs to a tree, such as a file on the local filesystem.
* `Resource`: the menu item will only be shown when the editor belongs to a readable resource.

The following `EditorSelectionType`s are supported:

* `Region`: the menu item will only be shown when a region with size >0 in the source file is selected.
* `Offset`: the menu item will only be shown in the context of an editor with a cursor.

### Menus

Menu items are assigned to 3 particular menus:

* `editor-context-menu [ $MenuItem* ]`: the context menu that gets shown in editors of the language, for example when right-clicking in an editor of the language in an IDE. Spoofax automatically creates a top-level submenu with the name of the language to host the editor context menu items. The `required-editor-file-types` and `required-editor-selection-types` options are used to filter menu items.
* `resource-context-menu [ $MenuItem* ]`: the context menu that gets shown in resource explorers, for example when right-clicking in the file browser in an IDE. Spoofax automatically creates a top-level submenu with the name of the language to host the editor context menu items. The `required-resource-types` and `required-enclosing-resource-types` options are used to filter menu items.
* `main-menu [ $MenuIitem* ]`: the main menu of the language, which is shown on the menu bar in IDEs. When no `main-menu` section is given, it defaults to the same menu as `editor-context-menu`.

For example, we can assign the command defined earlier to several menus:

```cfg
editor-context-menu [
  menu "Debug" [
    command-action {
      command-def = showParsedAstCommand
      execution-type = Once
    }
    command-action {
      command-def = showParsedAstCommand
      execution-type = Continuous
    }
  ]
]
resource-context-menu [
  menu "Debug" [
    command-action {
      command-def = showParsedAstCommand
      execution-type = Once
      required-resource-types = [File]
    }
  ]
]
```

### Options

### Sections

conventions and defaults
spoofaxc.lock

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

## Instantation

## Compilation

description of how a language definition is compiled into a language implementation, and what the compiled form looks like.

generate Java sources into:

build/generated/sources/language

build/generated/sources/adapter

generate Stratego sources into:

build/generated/sources/languageSpecification/stratego
