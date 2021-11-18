# Configuration

The main entry point of a language definition is the `spoofaxc.cfg` (Spoofax compiler configuration) file, written in the CFG language.
The goal of this config file is to configure basic options, enable/disable features, point to main source files of meta-languages, to add/override behaviour, and to serve as an anchor on the filesystem.
The directory that the `spoofaxc.cfg` file is in is called the "root directory" of the language definition, and any relative paths are resolved relative to that directory.

The CFG language has domain-specific syntax for configuring language definitions. However, the syntax follows these conventions:

* Options are assigned a value with `$Option = $Expression`. Unless specified otherwise, options may only be given once.
* Sections `$Section { ... }` may enable features and group options.
* Lists `$List [ ..., ... ]` indicate an option/section may be given 0-many times.
* Let bindings `let $Name = $Expression` can be used to give values a name that can be (re-)used in the rest of the configuration file.

If something in the documentation is unclear, the [CFG language definition can be found here](https://github.com/metaborg/spoofax-pie/tree/develop/lwb/metalang/cfg/cfg.spoofax2).

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

## Literals

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

## Let bindings

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

## Top-level options

The following top-level options exist:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `group = $Expression` | no | Group identifier of the language, used as the `group`/`groupId` in the Java ecosystem. Defaults to `org.metaborg`. | String |
| `id = $Expression` | no | Artifact identifier of the language, used as the `name`/`artifactId` in the Java ecosystem. Defaults to the name of the language uncapitalized. | String |
| `name = $Expression` | yes | Name of the language. | String |
| `version = $Expression` | no | Version of the language, used as the `version` in the Java ecosystem. Defaults to `0.1.0`. | String |
| `file-extension = $Expression` | no | File extension of the language. May be given multiple times. Defaults to the name of the language transformed to fit in 3 characters. | String |
| `java-package-id-prefix = $Expression` | no | The prefix to add before all package identifiers in Java source files. Defaults to `mb.$Name` where `$Name` is transformed to be a valid package identifier.  | Qualified Java identifier |
| `java-class-id-prefix = $Expression` | no | The prefix to add before all Java classes. Defaults to the name of the language transformed to be a valid class identifier. | Java identifier |
| `source-directory = $Expression` | no | Path relative to the root directory that has the sources of the language definition. Defaults to `src`. | Path |
| `build-directory = $Expression` | no | Path relative to the root directory that has the generated sources and build outputs when building the language definition. Defaults to `build`. | Path |

## Commands

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
  * `HierarchicalResource`: attempt to infer a `ResourcePath` to a hierarchical resource. A hierarchical resource is a resource that belongs to a (tree) hierarchy, such as a file or directory on the local filesystem. Use this when the command relies on the resource being in a filesystem, but does not care whether it is a directory or a file.
  * `ReadableResource`: attempt to infer a `ResourceKey` to a readable resource. This is more general than `File`, as we only ask for a resource that can be read, not one that belongs to a (local) filesystem. Use this when the command does not rely on the resource being in a filesystem.
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

## Menu items

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

* `HierarchicalResource`: the menu item will only be shown when the editor belongs to a hierarchical resource. That is, a resource that belongs to a tree, such as a file or directory on the local filesystem.
* `ReadableResource`: the menu item will only be shown when the editor belongs to a readable resource.

The following `EditorSelectionType`s are supported:

* `Region`: the menu item will only be shown when a region with size >0 in the source file is selected.
* `Offset`: the menu item will only be shown in the context of an editor with a cursor.

## Menus

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

## Language feature sections

### Parser

The `parser { $ParserOption* }` section enables generation of a parser, and groups options.
The `sdf3` section must be enabled when the `parser` section is enabled.
The following `ParserOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `default-start-symbol = $Expression` | yes | The start symbol to use when no specific one is provided. | SDF3 sort identifier |
| `variant = $ParserVariant` | no | The parser variant to use. Defaults to `jsglr1`. | n/a |

The following `ParserVariant`s are supported:

* `jsglr1`: uses the [JSGLR1](https://github.com/metaborg/jsglr/tree/master/org.spoofax.jsglr) parser.
* `jsglr2 { $Jsglr2Option* }`: uses the [JSGLR2](https://github.com/metaborg/jsglr/tree/master/org.spoofax.jsglr2) parser. The following `Jsglr2Option`s are supported:
  * `preset = $Jsglr2Preset`: sets the [JSGLR2 preset](https://github.com/metaborg/jsglr/blob/master/org.spoofax.jsglr2/src/main/java/org/spoofax/jsglr2/JSGLR2Variant.java#L120) to use. The following `Jsglr2Preset`s are supported:
    * `Standard`
    * `Elkhound`
    * `Recovery`
    * `RecoveryElkhound`
    * `DataDependent`
    * `LayoutSensitive`
    * `Composite`
    * `Incremental`
    * `IncrementalRecovery`

### Comment symbols

The `comment-symbols { $CommentSymbolOption* }` section enables specification of line and block comment characters, which are required for the "toggle comment" editor service.
The following `CommentSymbolOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `line = $Expression` | no | Adds a line comment symbol. Can be given multiple times to list multiple line comment symbols. The first one will be used to comment a line with the "toggle comment" editor service. | String |
| `block = $Expression * $Expression` | no | Adds block comment symbols, with an opening and close symbol. Current "toggle comment" editor services do not use block comment symbols yet. | String |

### Bracket symbols

The `bracket-symbols { $BracketSymbolOption* }` section enables specification of bracket symbols (e.g., square brackets, curly brackets, parentheses, etc.), which are required for the "bracket matching" editor service.
The following `BracketSymbolOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `bracket = $Expression * $Expression` | no | Adds bracket symbols, with an opening and closing symbol. Can be given multiple times to list multiple bracket symbols. | Character |

### Styler

The `styler { $StylerOption* }` section enables generation of a styler, and groups options.
The `esv` section must be enabled when the `styler` section is enabled.
Currently, no `StylerOption`s are supported.

### Constraint analyzer

The `constraint-analyzer { $ConstraintAnalyzerOption* }` section enables generation of a constraint analyzer, and groups options.
The `statix` section must be enabled when the `constraint-analyzer` section is enabled.
The following `ConstraintAnalyzerOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `multi-file = $Expression` | no | Whether multi-file analysis is enabled. Defaults to `false`. | Boolean |
| `stratego-strategy = $Expression` | no | The stratego strategy entry-point that handles communication with the constraint-solver. Defaults to `editor-analyze`. | Stratego strategy identifier |
| `default-statix-message-stacktrace-length = $Expression` | no | The default Statix message stacktrace length to use. Default is implementation-defined. Does nothing if Statix is not enabled. | Unsigned integer |
| `default-statix-message-term-depth = $Expression` | no | The default Statix message term depth to use. Default is implementation-defined. Does nothing if Statix is not enabled. | Unsigned integer |
| `default-statix-test-log-level = $Expression` | no | The default Statix test log level to use. Default is implementation-defined. Does nothing if Statix is not enabled. | String |
| `default-statix-supress-cascading-errors = $Expression` | no | Whether to suppress cascading errors by default. Default is implementation-defined. Does nothing if Statix is not enabled. | Boolean |

### Multi-language analyzer

The `multilang-analyzer { $MultilangAnalyzerOption* }` section enables generation of a multi-language analyzer, and groups options.
The `constraint-analyzer` and `statix` sections must be enabled when the `multilang-analyzer` section is enabled.
Currently, no `MultilangAnalyzerOption`s are supported.

### Stratego runtime

The `stratego-runtime { $StrategoRuntimeOption* }` section enables generation of a stratego runtime, and groups options.
The `stratego` section must be enabled when the `stratego-runtime` section is enabled.
Currently, no `StrategoRuntimeOption`s are supported.

### Completer

The `completer { $CompleterOption* }` section enables generation of a code completer, and groups options.
The `constraint-analyzer` and `statix` sections must be enabled when the `completer` section is enabled.
Currently, no `CompleterOption`s are supported.

### Reference resolution

The `reference-resolution { $ReferenceResolutionOption* }` section enables generation of the reference resolver editor service, and groups options.
The following `ReferenceResolutionOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `variant = $ReferenceResolutionVariant` | yes | The reference resolution variant to use. | n/a |

The following `ReferenceResolutionVariant`s are supported:

* Stratego-based: `stratego { strategy = strategy $Strategy }` where `Strategy` is a Stratego strategy, typically `editor-resolve`.

### Hover tooltips

The `hover { $HoverOption* }` section enables generation of the hover text editor service, and groups options.
The following `HoverOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `variant = $HoverVariant` | yes | The reference resolution variant to use. | n/a |

The following `HoverVariant`s are supported:

* Stratego-based: `stratego { strategy = strategy $Strategy }` where `Strategy` is a Stratego strategy, typically `editor-hover`.

## Meta-language sections

### SDF3

The `sdf3 { $Sdf3Option* }` section enables syntax definition with [SDF3](https://www.spoofax.dev/references/syntax/).
The `parser` section must be enabled when the `sdf3` section is enabled.
The following `Sdf3Option`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `source = $Sdf3Source` | no | The source of the SDF3 definition. Defaults to a `files` source with the top-level `source-directory` option as its main source directory, and `./start.sdf3` as its main file relative to the main source directory. | n/a |
| `parse-table-generator { $ParseTableGeneratorOption* }` | no | Parse table generator options. | n/a |

The following `$Sdf3Source`s are supported:

* Files: `files { $Sdf3FilesOption* }`
* Prebuilt: `prebuilt { $Sdf3PrebuiltOption }`

The following `Sdf3FilesOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `main-source-directory = $Expression` | no | The directory relative to the root directory that contains the main SDF3 file. Defaults to the value of the top-level `source-directory` option. | Path |
| `main-file = $Expression` | no | The main SDF3 file relative to the `main-source-directory`. Defaults to `./start.sdf3`. | Path |

The following `$Sdf3PrebuiltOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `parse-table-aterm-file = $Expression` | yes | The prebuilt SDF3 parse table ATerm file to use (usually called `sdf.tbl`) relative to the root directory | Path |
| `parse-table-persisted-file = $Expression` | yes | The prebuilt SDF3 parse table persisted file to use (usually called `sdf.bin`) relative to the root directory | Path |

The following `ParseTableGeneratorOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `dynamic = $Expression` | no | Whether the generated parse table is dynamic. Defaults to `false`. | Boolean |
| `data-dependent = $Expression` | no | Whether the generated parse table is data-dependent. Defaults to `false`. | Boolean |
| `layout-sensitive = $Expression` | no | Whether the generated parse table is layout-sensitive. Defaults to `false`. | Boolean |
| `solve-deep-conflicts = $Expression` | no | Whether the parse table generator solves deep priority conflicts. Defaults to `true`. | Boolean |
| `check-overlap = $Expression` | no | Whether the parse table generator checks for overlap. Defaults to `false`. | Boolean |
| `check-priorities = $Expression` | no | Whether the parse table generator checks priorities. Defaults to `false`. | Boolean |

### ESV

The `esv { $EsvOption* }` section enables syntax-based styling definition with [ESV](https://www.spoofax.dev/references/editor-services/esv/).
The `styler` section must be enabled when the `esv` section is enabled.
The following `EsvOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `source = $EsvSource` | no | The source of the ESV definition. Defaults to a `files` source with the top-level `source-directory` option as its main source directory, and `./main.esv` as its main file relative to the main source directory. | n/a |

The following `$EsvSource`s are supported:

* Files: `files { $EsvFilesOption* }`
* Prebuilt: `prebuilt { $EsvPrebuiltOption }`

The following `EsvFilesOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `main-source-directory = $Expression` | no | The directory relative to the root directory that contains the main ESV file. Defaults to the value of the top-level `source-directory` option. | Path |
| `main-file = $Expression` | no | The main ESV file relative to the `main-source-directory`. Defaults to `./main.esv`. | Path |
| `include-directory = $Expression` | no | Adds an include directory from which to resolve ESV imports. May be given multiple times. | Path |

The following `$EsvPrebuiltOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `file = $Expression` | yes | The prebuilt ESV file to use relative to the root directory | Path |

### Statix

The `statix { $StatixOption* }` section enables static semantics definition with [Statix](https://www.spoofax.dev/references/statix/).
The `constraint-anaylzer` section must be enabled when the `statix` section is enabled.
The following `StatixOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `source = $StatixSource` | no | The source of the Statix definition. Defaults to a `files` source with the top-level `source-directory` option as its main source directory, and `./main.stx` as its main file relative to the main source directory. | n/a |

The following `$StatixSource`s are supported:

* Files: `files { $StatixFilesOption* }`
* Prebuilt: `prebuilt { $StatixPrebuiltOption }`

The following `StatixFilesOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `main-source-directory = $Expression` | no | The directory relative to the root directory that contains the main Statix file. Defaults to the value of the top-level `source-directory` option. | Path |
| `main-file = $Expression` | no | The main Statix file relative to the `main-source-directory`. Defaults to `./main.stx`. | Path |
| `include-directory = $Expression` | no | Adds an include directory from which to resolve Statix imports. May be given multiple times. | Path |
| `sdf3-statix-signature-generation = $Expression` | no | Whether SDF3 to Statix signature generation is enabled. When enabled, `stratego { sdf3-statix-explication-generation = true }` must also be enabled. Defaults to `false`. | Boolean |

The following `$StatixPrebuiltOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `spec-aterm-directory = $Expression` | yes | The prebuilt Statix spec ATerm directory to use relative to the root directory | Path |

### Stratego

The `stratego { $StrategoOption* }` section enables definition of transformations with [Stratego](https://www.spoofax.dev/references/stratego/).
The `stratego-runtime` section must be enabled when the `stratego` section is enabled.
The following `StrategoOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `source = $StrategoSource` | no | The source of the Statix definition. Defaults to a `files` source with the top-level `source-directory` option as its main source directory, and `./main.str2` as its main file relative to the main source directory. | n/a |
| `sdf3-statix-explication-generation = $Expression` | no | Whether SDF3 to Statix injection explication/implication generation is enabled. When enabled, `statix { sdf3-statix-signature-generation = true }` must also be enabled. Defaults to `false`. | Boolean |
| `language-strategy-affix = $Expression` | no | The affix that is used to make certain generated strategies unique to the language. This is used both as a prefix and suffix. Defaults to name of the language transformed to a Stratego strategy identifier. | Stratego strategy identifier |

The following `$StrategoSource`s are supported:

* Files: `files { $StrategoFilesOption* }`

The following `$StrategoFilesOption`s are supported:

| Syntax | Required? | Description | Type |
| - | - | - | - |
| `main-source-directory = $Expression` | no | The directory relative to the root directory that contains the main Stratego file. Defaults to the value of the top-level `source-directory` option. | Path |
| `main-file = $Expression` | no | The main Stratego file relative to the `main-source-directory`. Defaults to `./main.str2`. | Path |
| `include-directory = $Expression` | no | Adds an include directory from which to resolve Stratego imports. May be given multiple times. | Path |


## spoofaxc.lock

The `spoofaxc.lock` file, which resides next to the `spoofaxc.cfg` file, contains values for several options that have defaults derived from other options, in order to keep these derived values stable even when the options they are derived from are changed.
For example, when no `java-class-id-prefix` option is set in `spoofaxc.cfg`, it will be derived from the `name` option with some changes to make it compatible as a Java identifier, and is stored under `shared.defaultClassPrefix` in the `spoofaxc.lock` file.
When you change the `name` of your language, the stored value will be used, keeping the class prefix the same, making it possible to rename the language without having to rename all class files.
Therefore, the `spoofaxc.lock` file should be checked in to source control, in order to have reproducible builds.

If you **do** want to re-derive a default from other options, remove the option from the `spoofaxc.lock` file and rebuild the language.
The value will be re-derived and stored in `spoofaxc.lock`, after which you need to check it into source control again.
