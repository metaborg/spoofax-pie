# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [0.19.8] - 2024-03-06
### Changed
- Command Line Interface now returns a non-zero error code when the underlying command fails.
- Command Line Interface executing tests now prints the number of passed and failed test cases.
- SPT `transform` expectations support commands using the enclosing file and/or enclosing project.

### Fixed
- Termination issues in Code Completion
- Serialization issues of meta-language configuration objects
- Region arguments to SPT `transform` expectations now correspond to the imploder attachments.
- Missing Java code bundling in SPT

## [0.19.7] - 2024-02-09
### Changed
- `releng` (devenv-release) requirement to `0.1.35`.
- `coronium` plugin requirement to `0.3.17`.
- Removed SnakeYAML dependency of Tego.
- Removed direct use of guava dependency.
- Explicitly bundling guava dependency in Eclipse plugin due to 3rd party dependency via dagger-compiler.
- Eclipse versions to `2022-06`.

### Fixed
- Missing Java code bundling in sdf3.ext.statix

## [0.19.6] - 2023-05-03
### Fixed
- Command-line options not getting default values due to wrong command context.
- Missing dependencies from Gradle `processResources` tasks to our tasks that provide resources.

### Changed
- Feedback names to not be printed by default in command-line runs. Can be printed with `--print-feedback-names`.


## [0.19.5] - 2023-05-01
### Fixed
- `strategolib.eclipse` not using Eclipse version `2021-03`.

### Changed
- Command-line runs use the current working directory as enclosing project/directory context.


## [0.19.4] - 2023-03-13
### Changed
- `releng` (devenv-release) requirement to `0.1.34`.
- Eclipse versions to `2021-03`.

### Added
- Adapter section to `spoofaxc.cfg` to extend several adapter classes of languages.


## [0.19.3] - 2022-11-25
### Fixed
- Missing class files in 0.19.2 release.

### Changed
- `releng` (devenv-release) requirement to `0.1.33`.

### Added
- Preliminary support for using existing Stratego concrete syntax extension parse tables.


## [0.19.2] - 2022-05-25
### Fixed
- New SDF3 and Statix source files not being detected.
- Default SDF3 layout definition not including horizontal tabs.

### Changed
- `releng` (devenv-release) requirement to `0.1.32`.


## [0.19.1] - 2022-05-24
### Fixed
- CFG conversion being executed even if static analysis returned errors, resulting in runtime exceptions. Fixes [#109](https://github.com/metaborg/spoofax-pie/issues/109), [#94](https://github.com/metaborg/spoofax-pie/issues/94).
- Value argument provider always causing runtime exceptions. Fixes [#102](https://github.com/metaborg/spoofax-pie/issues/102).
- Absolute file paths in places where relative ones were expected always causing runtime exceptions. Fixes [#96](https://github.com/metaborg/spoofax-pie/issues/96).
- Eclipse LWB plugin still generating the old `build/generated/sources/languageSpecification/java` directory.


## [0.19.0] - 2022-05-13
### Fixed
- ESV imports not resolving to modules in the main source directory.
- Updating the Eclipse LWB plugin causing failures when no clean is performed. The PIE store is now automatically cleaned when the version changes. Fixes [#39](https://github.com/metaborg/spoofax-pie/issues/39).
- UNIX JVMs embedded into Eclipse installations not working due to JVM not having the executable mode set. Fixes [#37](https://github.com/metaborg/spoofax-pie/issues/37).

### Removed
- `include-libspoofax2-exports` option in `esv` section of `spoofaxc.cfg`, this can now simply be replaced with a dependency to `org.metaborg:libspoofax2:*`.

### Changed
- `resource` requirement to `0.14.1`.
- `common` requirement to `0.11.0`.
- `pie` requirement to `0.21.0`.
- `releng` (devenv-release) requirement to `0.1.31`.
- `coronium` plugin requirement to `0.3.16`.
- Dependency syntax to be less verbose and more configurable.
- Dependency kinds into `Build` for dependencies that are needed when building the language, and `Run` for dependencies that are needed when running the language.
- Generated meta-language files are now generated in `./build/generated/sources/metalang/<metalang>` instead of `./build/generated/sources/languageSpecification/<metalang>`.
- Unarchived meta-language files are now unarchived into `./build/unarchive/<metalang>` instead of `./build/unarchive`.
- `<Name> = parameter { <Options> }` into `<Name> = { <Options> }`. The old syntax is still supported but deprecated, and will be removed in the future.
- `parse-table-generator` section in `sdf3` section is now part of `source = files { ... }` section.
- `sdf3-statix-signature-generation` section in `statix` section is now part of `source = files { ... }` section.
- `sdf3-statix-explication-generation` section in `stratego` section is now part of `source = files { ... }` section.
- `language-strategy-affix` option in `stratego` section is now part `source = files { ... }` section. Also, the property in the lockfile corresponding to this option has been changed from `stratego.languageStrategyAffix` to `stratego.source.files.languageStrategyAffix`.
- Default dependencies are now only added if no dependencies are specified in `spoofaxc.cfg`. When generating a new Spoofax 3 project, these default dependencies are added explicitly to the `spoofaxc.cfg` file. In the future, these default dependencies will be removed. It is recommended to add these dependencies to your `spoofax.cfg`:
```
build-dependencies [
  org.metaborg:strategolib:*
  org.metaborg:gpp:*
  org.metaborg:libspoofax2:*
  org.metaborg:libstatix:*
]
```

### Added
- Preliminary support for SDF3, ESV, and Statix compile-time dependencies. That is, exports of source directories of those meta-languages and imports through dependencies.
- Support for include directories for SDF3.
- `build-dependencies [ ... ]` section for quickly specifying build dependencies without having to specify the kind for every dependency.


## [0.18.0] - 2022-04-04
### Fixed
- Ensure that `strategolib.eclipse` and `gpp.eclipse` are published, as they are required by Eclipse plugins of languages.
- Exception in `DynamicLoad` when a classpath directory did not exist.

### Changed
- Update `org.jetbrains.intellij` Gradle plugin to `1.4.0`.
- `common` requirement to `0.10.3`.
- `resource` requirement to `0.14.0`.
- `pie` requirement to `0.20.0`.
- `releng` (devenv-release) requirement to `0.1.30`.

### Added
- Preliminary support for Stratego compile-time dependencies.


## [0.17.0] - 2022-03-09
### Fixed
- Literals on the left-hand side in SDF3 kernel syntax productions causing errors even though they are valid.

### Changed
- Improve "language/component management" and make it cross-platform.
- Merge `spoofax.compiler.dagger` into `spoofax.compiler`.
- Merge `spoofax.lwb.compiler.dagger` into `spoofax.lwb.compiler`.
- Rename `Spoofax3` -> `SpoofaxLwb` in `spoofax.lwb.compiler`.
- Update `org.jetbrains.intellij` Gradle plugin to `1.0`.
- `releng` (devenv-release) requirement to `0.1.29`.
- `common` requirement to `0.10.2`.
- `pie` requirement to `0.19.8`.


## [0.16.17] - 2021-12-13
### Changed
- `pie` requirement to `0.19.7`.


## [0.16.16] - 2021-12-11
### Changed
- `releng` (devenv-release) requirement to `0.1.28`. (skipped a few versions due to bugs)


## [0.16.15] - 2021-12-01
### Fixed
- imports to `libspoofax` in Stratego 2 not working due to unresolved import to `libstratego-aterm`. This precompiled Stratego 1 library is now passed in by default.

### Changed
- `releng` (devenv-release) requirement to `0.1.23`.


## [0.16.14] - 2021-12-01
### Changed
- `releng` (devenv-release) requirement to `0.1.22`.


## [0.16.13] - 2021-11-24
### Changed
- `releng` (devenv-release) requirement to `0.1.21`.


## [0.16.12] - 2021-11-23
### Fixed
- More wrong main files being used when only the main source directory was set for a meta-language in `spoofaxc.cfg`.
- Extract more messages in SPT `transform` expectations.
- Out-of-bounds messages in SPT. Messages outside the bounds of the test fragment are now moved to the expectation.

### Changed
- `common` requirement to `0.10.1`.
- `pie` requirement to `0.19.6`.


## [0.16.11] - 2021-11-19
### Fixed
- RV32IM not being parsed due to wrong start symbol.
- Wrong main files being used when only the main SDF3 source directory was set in `spoofaxc.cfg`.


## [0.16.10] - 2021-11-19
### Changed
- `releng` (devenv-release) requirement to `0.1.20`.
- Updated RV32IM syntax.


## [0.16.9] - 2021-11-18
### Fixed
- Eclipse editors of dynamically loaded languages lacking many editor services.
- Add `stratego.output-java-package` option to CFG, for configuring the Stratego generated Java files package.

### Changed
- `releng` (devenv-release) requirement to `0.1.19`.


## [0.16.8] - 2021-11-17
### Added
- Support for toggle comment in Eclipse plugins.
- Support for bracket matching in Eclipse plugins.


## [0.16.7] - 2021-11-17
### Fixed
- Many Stratego messages having no origin locations.


## [0.16.6] - 2021-11-16
### Fixed
- Messages with exceptions not showing their exception in Eclipse.
- SPT transform expectation halting the entire pipeline due to an exception when building the arguments for a command. It now creates an error in the SPT file.
- SPT transform expectation not working on commands that require an enclosing context. Fixed by making SPT test case resources implement `HierarchicalResource` that mock a directory with itself in it.
- SPT transform to term expectation not giving an error if a command returned no feedback at all.

### Changed
- `LanguageInstance#createCodeCompletionTask` to return an `Option` to signal that the language does not support code completion. This change will be rolled out to the other task creating functions of `LanguageInstance` in the future.
- `RawArgsBuilder` to throw `ArgumentBuilderException` instead of a generic `RuntimeException` to differentiate between other `RuntimeException`s.
- RV32IM execution task to be split into `ExecuteRiscV` which takes a supplier of RV32IM text, and produces the printed text or an error, and `ShowExecuteRiscV` which shows that as a command.
- SPT transform to ATerm expectation not checking whether the command feedback matches the expected term pattern. It now checks the "show text" feedback against the term pattern, which will only work if the pattern is a wildcard or a string.

### Added
- Made it possible to depend on the RV32IM language, giving access to its tasks. A dependency can be added with the following syntax in CFG: `depend-on-rv32im = true`. A more general language dependency system will be added in the future.


## [0.16.5] - 2021-11-12
### Changed
- `releng` (devenv-release) requirement to `0.1.18`.


## [0.16.4] - 2021-11-11
### Fixed
- Fix `InvalidAstShapeException` for incomplete CFG files (https://github.com/metaborg/spoofax-pie/issues/93).

### Changed
- `common` requirement to `0.10.0`.
- `pie` requirement to `0.19.5`.

### Added
- Documentation page on importing a project into the Eclipse LWB.


## [0.16.3] - 2021-11-10
### Fixed
- Fix "Resource ‘...’ does not exist." in Eclipse when outputting files from commands.
- Fix exception being thrown, halting the entire pipeline, when the SDF3 source directory does not exist. Now a result with the error is returned instead.
- Fix exception being thrown, halting the entire pipeline, when meta-language files are moved around. Now an error is logged instead.
- Importing language project into Eclipse causing many duplicate definition errors due to all files being copied into `bin`. The `bin` directory is now ignored as a source directory by default.
- Spoofax Eclipse plugins/features/repositories not having names or providers (https://github.com/metaborg/spoofax-pie/issues/85).

### Changed
- `resource` requirement to `0.13.2`.
- `pie` requirement to `0.19.4`.


## [0.16.2] - 2021-11-09
### Fixed
- Fix wrong separator being used on Windows in `pp.str2` Stratego import.

### Changed
- `resource` requirement to `0.13.1`.


## [0.16.1] - 2021-11-08
### Fixed
- Fix wrong pretty-printer import in Stratego when SDF3 main file is not `./start.sdf3`.


## [0.16.0] - 2021-11-05
### Fixed
- Evaluating .stxtest files outside of the Statix source directory (src by default) silently failing.
- Errors in CFG not appearing, or just displaying a single error on the first line.
- Source file configuration in `spoofaxc.cfg` to be relative to the respective source directory, instead of relative to the root directory.
- Missing source directories or files not giving errors in `spoofaxc.cfg`.
- Serialization failure when an Eclipse resource issue occurred due to `CoreException` not being serializable. Fixed by turning `CoreException` into `SerializableCoreException` which is serializable.
- No errors being printed when reading configuration failed in the LWB Gradle plugin.

### Changed
- Spoofax Eclipse LWB plugin to show an error dialog when building a language fails due to errors in language definition files.
- LWB compiler to make it possible to supply a prebuilt ESV output file for a language instead of always having to compile ESV from sources. This is done with the `esv { source = prebuilt { file = ./prebuilt/editor.esf.af } }` configuration in `spoofaxc.cfg`. The syntax for setting the main source directory, main file, and include directories has also changed. See the configuration reference on the documentation site for details.
- LWB compiler to make it possible to supply prebuilt SDF3 parse table files for a language instead of always having to compile SDF3 from sources. This is done with the `sdf3 { source = prebuilt { parse-table-aterm-file = ./prebuilt/sdf.tbl parse-table-persisted-file = ./prebuilt/sdf.bin } }` configuration in `spoofaxc.cfg`. The syntax for setting the main source directory and main file has also changed. See the configuration reference on the documentation site for details.
- LWB compiler to make it possible to supply prebuilt Statix spec ATerm files for a language instead of always having to compile Statix from sources. This is done with the `statix { source = prebuilt { spec-aterm-directory = ./prebuilt/resource/statix } }` configuration in `spoofaxc.cfg`. The syntax for setting the main source directory and main file has also changed. See the configuration reference on the documentation site for details.
- Moved most Stratego configuration into a `source = files {}` section in accordance with the other meta-languages. See the configuration reference on the documentation site for details.
- LWB compiler to be more incremental by adding output stampers to high-level tasks, ensuring they only get re-executed when a relevant part of the configuration changes.
- `common` requirement to `0.9.9`.
- `pie` requirement to `0.19.3`.
- `coronium` plugin requirements to `0.3.12`.
- `releng` (devenv-release) requirement to `0.1.17`.

### Added
- RV32IM language for targeting RISC-V.
- Semantic code completion.


## [0.15.3] - 2021-10-22
### Fixed
- Another instance of cancellation/interrupt during PIE execution leaving behind an inconsistent state in certain edge cases. This should resolve all instances of intermittent `NullPointerException`s. (https://github.com/metaborg/spoofax-pie/issues/81)


## [0.15.2] - 2021-10-21
### Added
- Tego runtime, along with exposing this runtime to languages that require it, in preparation for semantic code completion based on Statix (https://github.com/metaborg/spoofax-pie/pull/73).

### Fixed
- Undo in the Eclipse LWB applying to the first opened editor when multiple editors of the same language were opened (https://github.com/metaborg/spoofax-pie/issues/55).


## [0.15.1] - 2021-10-19
### Fixed
- No errors for certain kinds of SDF3 context-free productions that do require a constructor. All context-free productions except those of the form `A = B` and `A = {B ","}*`, excluding productions that have a `{reject}` or `{bracket}` annotation, now require a constructor or produce an error.
- No error for ill-formed SDF3 bracket productions. Bracket productions must be of the form `A = "(" B ")"` or produce an error.
- `IndexOutOfBoundsException` in SPT tests with `resolve` and `resolve to` expectations where there was no valid target for a selection.
- Cancellation/interrupt during PIE execution leaving behind an inconsistent state in certain edge cases. This solved some intermittent `NullPointerException`s, but not all (https://github.com/metaborg/spoofax-pie/issues/81).

### Changed
- `pie` requirement to `0.19.1`
- `releng` (devenv-release) requirement to 0.1.16.


## [0.15.0] - 2021-10-18
### Fixed
- Continuous commands not updating after a language was rebuilt in Eclipse.
- Not unobserving continuous command feedback while a language is being rebuilt in Eclipse, which would cause the feedback to always reappear after every change.
- Possible deadlock when closing Eclipse continuous command feedback editors.
- Read lock rules in Eclipse not being able to be acquired concurrently.
- Some changes to programs of languages with single-file analysis losing their messages.

### Changed
- `resource` requirement to `0.13.0`.
- `pie` requirement to `0.19.0`.


## [0.14.2] - 2021-10-13
### Fixed
- Laggy typing in Statix editors by downgrading the Eclipse LWB to Eclipse 2021-03.


## [0.14.1] - 2021-10-12
### Fixed
- Editors in the Eclipse LWB not updating after making changes to the editor that causes the underlying computation to interrupt.

### Added
- Command for showing the Statix scope graph AST of a file (https://github.com/metaborg/spoofax-pie/pull/80).

### Changed
- Stratego invocation to check for `InterruptedException` in the cause/suppressed chain of `InterpreterException` and rethrow that. For now, this is sneakily thrown so that `InterruptedException` does not have to be added to the  method signature of `StrategoRuntime.invoke` methods, but this should be added in the future. This sneakily thrown exception is catched and handled by PIE.
- `pie` requirement to `0.18.1`.


## [0.14.0] - 2021-10-11
### Added
- `default-statix-message-stacktrace-length`, `default-statix-message-term-depth`, `default-statix-test-log-level`, `default-statix-supress-cascading-errors` options to the `constraint-analyzer` section in `spoofaxc.cfg`.

### Changed
- `ClassLoaderResources` implementations to be factored into `mb.spoofax.resource.ClassLoaderResources` in `spoofax.resource`, and make generated implementations extend it.
- Use `EclipseClassLoaderToNativeResolver` in Eclipse to attempt to resolve class loader resources into Eclipse resources.
- ConstraintAnalyzer to pass unchanged inputs; determined by AST hashCode and equality, and recursive region equality; as cached to the constraint analyzer, allowing it to reuse some cached results. The analysis tasks of languages keep this cache by storing the `ConstraintAnalyzerContext` as an internal object. This internal object is cleared when the Statix specification of a (dynamically loaded) language is changed.
- `ConstraintAnalyzer.Result.ast` to `analyzedAst`.
- `ConstraintAnalyzer.SingleFileResult.ast` to `analyzedAst`.
- Clearing a language project to unload the dynamically loaded language associated with that language project.
- `resource` requirement to `0.12.0`.
- `common` requirement to `0.9.8`.
- `pie` requirement to `0.18.0`.
- `releng` (devenv-release) requirement to 0.1.15.

### Fixed
- `LAYOUT?-CF` not accepted in SDF3 (https://github.com/metaborg/spoofax-pie/issues/78). Fixed by marking `-CF`, `-LEX`, and `-VAR` sorts as kernel, allowing them to be used in kernel syntax context.
- `LAYOUT?-CF` in kernel production causing build errors due to a bug in the SDF3->Stratego signature generator.
- Constraint analysis not being cancellable. Cancelling an editor update or run command job in the Eclipse plugin now interrupts the thread, and the thread interrupt in turn cancels the constraint solver.


## [0.13.0] - 2021-10-01
### Fixed
- ValidationException on configuration change (https://github.com/metaborg/spoofax-pie/issues/70).
- CLI and Eclipse/IntelliJ plugins of languages not working due to missing `strategolib` dependency.
- Eclipse plugins of languages not starting due to out-of-date Eclipse version.
- Bracket production with constructor causing build errors (https://github.com/metaborg/spoofax-pie/issues/65).
- SDF Legacy constructs (optional, alteration, sequence) in context-free syntax not being exhaustively checked.
- Bracket production in lexical syntax being allowed.
- SPT parse to ATerm constructor names don't support underscores (https://github.com/metaborg/spoofax-pie/issues/67)
- Commands that fail give no feedback in the Eclipse LWB (https://github.com/metaborg/spoofax-pie/issues/68)

### Changed
- Drop support for Gradle 5.6.4, the minimum supported Gradle version is 6.8.
- `common` requirement to 0.9.7.
- `pie` requirement to 0.17.0.
- `releng` (devenv-release) requirement to 0.1.14.


## [0.12.1] - 2021-09-24
### Added
- Evaluate test command for Statix `.stxtest` files.

### Fixed
- SDF3 files in subdirectories cause the parenthesizer file to not import the correct signatures (https://github.com/metaborg/spoofax-pie/issues/61).
- Deadlocks in SPT *ToFragment expectations due to not closing PIE sessions (https://github.com/metaborg/spoofax-pie/pull/64).

### Changed
- `common` requirement to 0.9.6.
- `releng` (devenv-release) requirement to 0.1.13.


## [0.12.0] - 2021-09-22
### Fixed
- UI hang when invoking hover/reference resolution for the first time after a change in the Eclipse LWB (https://github.com/metaborg/spoofax-pie/issues/56). This was fixed by consistently using the `JsglrParseTaskDef#create(Recoverable)MultiAstSupplierFunction` override that takes a `sourceFilesFunction`, increasing incrementality due to the task identity being stable.
- Fix SDF3 issuing multiple kinds of parse tasks due to passing the main source directory as the root directory hint, instead of passing in the actual root directory.

### Changed
- The `JsglrParseTaskDef#create(Recoverable)MultiAstSupplierFunction` methods to accept a function that produces `? extends ListView<? extends ResourceKey>` instead of `ListView<ResourceKey>`, allowing functions that produce `ListView<ResourcePath>`.

### Removed
- The `JsglrParseTaskDef#create(Recoverable)MultiAstSupplierFunction` methods that take a `ResourceWalker` and `ResourceMatcher`.



## [0.11.13] - 2021-09-22
### Fixed
- Deadlock when closing editor while language build is running in the Eclipse LWB (https://github.com/metaborg/spoofax-pie/issues/60).
- Not being able to update to newer Eclipse LWB from update site due to change in required Eclipse version.


## [0.11.12] - 2021-09-20
### Fixed
- Deadlock when invoking hover/reference resolution in the Eclipse LWB (https://github.com/metaborg/spoofax-pie/issues/58).

### Changed
- Eclipse to 2021-09.
- Colors in Eclipse are inverted in dark mode themes using the same hack as https://github.com/metaborg/spoofax-eclipse/pull/19/.
- `coronium` requirement to 0.3.11.
- `pie` requirement to 0.16.8.
- `common` requirement to 0.9.5.


## [0.11.11] - 2021-09-17
### Fixed
- Stratego backend tasks triggering overlapping provided files, hidden dependencies, and visited multiple times errors in bottom-up builds when dynamic rules were added or removed.
- Unremovable directories on Windows due to Stratego leaking directory streams.
- Compile failures due to SDF3 module name not matching the relative file name. This now produces an error in the SDF3 file.
- Compile failures due to duplicate constructors in SDF3. Constructors with the same name were allowed if they were defined on a different sort, but Statix does not support this at the moment. Duplicate constructor names now result in an error.
- Compile failures due to constructor names starting with lowercase characters. This now produces an error in the SDF3 file.
- Module name does not agree with relative file path error in SDF3 always given on Windows due to difference in file separator characters.

### Changed
- `releng` (devenv-release) requirement to 0.1.12.


## [0.11.10] - 2021-09-15
### Fixed
- SDF3 label references (from layout constraints) that do not refer to a defined label causing compile errors. These undefined labels now show an error.

### Changed
- Missing imports in SDF3 now no longer give errors on everything due to improved cascaded error handling in the Statix constraint solver.
- `releng` (devenv-release) requirement to 0.1.11.


## [0.11.9] - 2021-09-13
### Fixed
- Injection explication failing on context-free productions without constructors that are not an injection, bracket, or rejection production. These productions now show an error.
- Signature generation failing on context-free productions with optionals, alternations, and sequences. These productions now show an error.
- Errors markers on directories never disappearing in the Eclipse LWB. Fixed for now by not adding error markers on directories.
- Cleaning a project in the Eclipse LWB does not remove all markers.

### Changed
- Lexical productions with constructors show a warning to remove the constructor.
- `releng` (devenv-release) requirement to 0.1.10.


## [0.11.8] - 2021-09-13
### Fixed
- Hidden dependency in MultiAstSupplierFunction.

### Changed
- PIE requirement to 0.16.7.
- `releng` (devenv-release) requirement to 0.1.9.

### Removed
- CFG, SDF3, and Statix hover tooltips, as they do not provide useful information at the moment.


## [0.11.7] - 2021-09-08
### Fixed
- SPT `parse succeeds` now fails in case of ambiguities and recovery. (https://github.com/metaborg/spoofax-pie/issues/41)

### Removed
- Temporarily removed SDF3 to syntactic completion generation to work around an incrementality issue in the Stratego compiler. Will be enabled again when the issue is resolved and completions are needed.


## [0.11.6] - 2021-09-06
### Added
- Several optional Eclipse preferences to tutorial.
- `.gitignore` file when generating a language project.
- Check for incompatible sort kinds in SDF3.

### Changed
- `releng` (devenv-release) requirement to 0.1.8.


## [0.11.5] - 2021-09-06
### Added
- SPT file to new project template.
- Short SPT tutorial.
- Short debugging command tutorial.

### Changed
- Split tutorial up into multiple parts. Transformation tutorial has been temporarily removed until it is rewritten.


## [0.11.4] - 2021-09-03
### Fixed
- Return generated commands as Java source files, ensuring they get compiled by the Java compiler.


## [0.11.3] - 2021-09-03
### Fixed
- Prefix generated commands with language's Java class prefix.

### Added
- Show scope graph debugging task/commands/menus generated for each language (that uses Statix).
- Show pre-analyze AST debugging task/commands/menus generated for each language (that uses Statix).


## [0.11.2] - 2021-09-03
### Removed
- `strategolib` import from template.


## [0.11.1] - 2021-09-02
### Added
- Show parsed AST and tokens debugging tasks/commands/menus generated for each language.
- Show analyzed AST debugging task/commands/menus generated for each language.

### Changed
- Restructure troubleshooting documentation.
- `resource` requirement to 0.11.5.
- `common` requirement to 0.9.3.
- `pie` requirement to 0.16.6.
- Menus with the same display name are merged into one.

### Fixed
- IndexOutOfBoundsException in SPT when selection reference `#0` is used.


## [0.11.0] - 2021-08-31
### Added
- Duplicate sort definition check in SDF3.
- `reference-resolution` and `hover` sections in language CFG file, promoted from subsections inside `editor-services` section.
- Ask for help guide in documentation.
- Report a bug guide in documentation.
- Troubleshooting guide in documentation.

### Changed
- Run commands in Eclipse plugins as jobs in order to not hang the IDE and to make them cancellable.
- Long SPT test suites are cancellable.
- Eclipse console always uses UTF-8 encoding.

### Removed
- `editor-services` section from language CFG file. `reference-resolution` and `hover` subsections are promoted to sections.


[Unreleased]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.8...HEAD
[0.19.8]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.7...release-0.19.8
[0.19.7]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.6...release-0.19.7
[0.19.6]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.5...release-0.19.6
[0.19.5]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.4...release-0.19.5
[0.19.4]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.3...release-0.19.4
[0.19.3]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.2...release-0.19.3
[0.19.2]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.1...release-0.19.2
[0.19.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.19.0...release-0.19.1
[0.19.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.18.0...release-0.19.0
[0.18.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.17.0...release-0.18.0
[0.17.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.17...release-0.17.0
[0.16.17]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.16...release-0.16.17
[0.16.16]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.15...release-0.16.16
[0.16.15]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.14...release-0.16.15
[0.16.14]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.13...release-0.16.14
[0.16.13]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.12...release-0.16.13
[0.16.12]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.11...release-0.16.12
[0.16.11]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.10...release-0.16.11
[0.16.10]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.9...release-0.16.10
[0.16.9]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.8...release-0.16.9
[0.16.8]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.7...release-0.16.8
[0.16.7]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.6...release-0.16.7
[0.16.6]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.5...release-0.16.6
[0.16.5]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.4...release-0.16.5
[0.16.4]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.3...release-0.16.4
[0.16.3]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.2...release-0.16.3
[0.16.2]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.1...release-0.16.2
[0.16.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.16.0...release-0.16.1
[0.16.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.15.3...release-0.16.0
[0.15.3]: https://github.com/metaborg/spoofax-pie/compare/release-0.15.2...release-0.15.3
[0.15.2]: https://github.com/metaborg/spoofax-pie/compare/release-0.15.1...release-0.15.2
[0.15.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.15.0...release-0.15.1
[0.15.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.14.2...release-0.15.0
[0.14.2]: https://github.com/metaborg/spoofax-pie/compare/release-0.14.1...release-0.14.2
[0.14.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.14.0...release-0.14.1
[0.14.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.13.0...release-0.14.0
[0.13.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.12.1...release-0.13.0
[0.12.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.12.0...release-0.12.1
[0.12.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.13...release-0.12.0
[0.11.13]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.12...release-0.11.13
[0.11.12]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.11...release-0.11.12
[0.11.11]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.10...release-0.11.11
[0.11.10]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.9...release-0.11.10
[0.11.9]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.8...release-0.11.9
[0.11.8]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.7...release-0.11.8
[0.11.7]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.6...release-0.11.7
[0.11.6]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.5...release-0.11.6
[0.11.5]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.4...release-0.11.5
[0.11.4]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.3...release-0.11.4
[0.11.3]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.2...release-0.11.3
[0.11.2]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.1...release-0.11.2
[0.11.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.0...release-0.11.1
[0.11.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.10.0...release-0.11.0
