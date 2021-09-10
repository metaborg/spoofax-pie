# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## [Unreleased]
### Fixed
- Hidden dependency in MultiAstSupplierFunction.

### Changed
- PIE to version 0.16.7.
- releng/devenv to version 0.1.9.

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


[Unreleased]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.7...HEAD
[0.11.7]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.6...release-0.11.7
[0.11.6]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.5...release-0.11.6
[0.11.5]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.4...release-0.11.5
[0.11.4]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.3...release-0.11.4
[0.11.3]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.2...release-0.11.3
[0.11.2]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.1...release-0.11.2
[0.11.1]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.0...release-0.11.1
[0.11.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.10.0...release-0.11.0
