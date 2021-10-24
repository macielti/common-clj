# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Added

- Added support to multiples environments on config component.

- Added a datomic component that we can use to transact on datomic databases. Compatible with Component framework by
  Stuart Sierra.

- Added `get-component-content` a component helper function used to get component core content. To be able to use this
  function you must follow some conventions, like assoc in the component map with the core content using the same
  component name as key. Compatible with Component framework by Stuart Sierra.

## [0.5.1] - 2021-10-23

### Added

- Added `str->keyword-kebab-case` that we can use to convert json encoded camelCased keys to clojure kebab case.

- Added a config component that we can use to load settings on our system map. Compatible with Component framework by
  Stuart Sierra.

## [0.4.1] - 2021-10-23

### Added

- Added `->cents` function were you can use to convert money amounts to cents.

## [0.3.1] - 2021-10-22

### Added

- Added `str->local-date` function were you can use to convert `String` to `LocalDate`.

- Added `local-date->str` function were you can use to convert `LocalDate` to `String`.

## [0.2.1] - 2021-09-06

### Added

- Applying `loose-schema` function to a schema with inner schemas, also make theirs inner schemas loose.

## [0.1.1] - 2021-09-05

### Fixed

- Properly update the change log doc.

## 0.1.0 - 2021-09-05

### Added

- Add `loose-schema` function.

[Unreleased]: https://github.com/macielti/common-clj/compare/0.1.1...HEAD

[0.5.1]: https://github.com/macielti/common-clj/compare/0.5.1...0.4.1

[0.4.1]: https://github.com/macielti/common-clj/compare/0.4.1...0.3.1

[0.3.1]: https://github.com/macielti/common-clj/compare/0.3.1...0.2.1

[0.2.1]: https://github.com/macielti/common-clj/compare/0.2.1...0.1.1

[0.1.1]: https://github.com/macielti/common-clj/compare/0.1.1...0.1.0
