# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## [Unreleased]


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
- Coronium to 0.3.11.
- Pie to 0.16.8.
- Common to 0.9.5.


## [0.11.11] - 2021-09-17
### Fixed
- Stratego backend tasks triggering overlapping provided files, hidden dependencies, and visited multiple times errors in bottom-up builds when dynamic rules were added or removed.
- Unremovable directories on Windows due to Stratego leaking directory streams.
- Compile failures due to SDF3 module name not matching the relative file name. This now produces an error in the SDF3 file.
- Compile failures due to duplicate constructors in SDF3. Constructors with the same name were allowed if they were defined on a different sort, but Statix does not support this at the moment. Duplicate constructor names now result in an error.
- Compile failures due to constructor names starting with lowercase characters. This now produces an error in the SDF3 file.
- Module name does not agree with relative file path error in SDF3 always given on Windows due to difference in file separator characters.


### Changed
- releng (devenv-release) to version 0.1.12.


## [0.11.10] - 2021-09-15
### Fixed
- SDF3 label references (from layout constraints) that do not refer to a defined label causing compile errors. These undefined labels now show an error.

### Changed
- Missing imports in SDF3 now no longer give errors on everything due to improved cascaded error handling in the Statix constraint solver.
- releng (devenv-release) to version 0.1.11.


## [0.11.9] - 2021-09-13
### Fixed
- Injection explication failing on context-free productions without constructors that are not an injection, bracket, or rejection production. These productions now show an error.
- Signature generation failing on context-free productions with optionals, alternations, and sequences. These productions now show an error.
- Errors markers on directories never disappearing in the Eclipse LWB. Fixed for now by not adding error markers on directories.
- Cleaning a project in the Eclipse LWB does not remove all markers.

### Changed
- Lexical productions with constructors show a warning to remove the constructor.
- releng (devenv-release) to version 0.1.10.


## [0.11.8] - 2021-09-13
### Fixed
- Hidden dependency in MultiAstSupplierFunction.

### Changed
- PIE to version 0.16.7.
- releng (devenv-release) to version 0.1.9.

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
- Spoofax 2 devenv version to `0.1.8`.


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
- Update resource dependency to 0.11.5.
- Update common dependency to 0.9.3.
- Update pie dependency to 0.16.6.
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


[Unreleased]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.13...HEAD
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
