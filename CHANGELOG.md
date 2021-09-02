# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]
### Added
- Show parsed AST and tokens debugging tasks/commands/menus generated for each language.

### Changed
- Restructure troubleshooting documentation.
- Update resource dependency to 0.11.5.
- Update common dependency to 0.9.3.
- Update pie dependency to 0.16.6.

### Fixed
- IndexOutOfBoundsException in SPT when selection reference `#0` is used.

## [0.11.0]
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

[Unreleased]: https://github.com/metaborg/spoofax-pie/compare/release-0.11.0...HEAD
[0.11.0]: https://github.com/metaborg/spoofax-pie/compare/release-0.10.0...release-0.11.0
