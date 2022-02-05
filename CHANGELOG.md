# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

## [8.13.9] - 2022-02-05

### Fixed

- Fixed KafkaProducer for production environments.

## [8.13.8] - 2022-02-05

### Added

- Added TelegramConsumer component.

## [8.12.8] - 2022-02-05

### Added

- Added Telegram producer messages component.

### Removed

- Removed Telegram component

## [7.12.8] - 2022-02-04

### Fixed

- Fixed integration between mocked consumer and producer (Kafka) for better integration tests.

### Changed

- Now we have separated components for Kafka Consumer and MockKafkaConsumer.

### Added

- Added helper functions to do integration tests with Kafka
    - Helper function to fetch produced messages.
    - Helper function to fetch successfully consumed and processed messages (the ones that their handler function didn't
      raise exceptions)

## [6.12.7] - 2022-02-03

### Changed

- Now Kafka mocked client and kafka consumer are integrated this will make more ease to do integration tests.
- Now we have separated components for Kafka Producer and MockKafkaProducer.

## [5.12.7] - 2022-01-30

### Added

- Added Kafka `consumer` component.

## [5.11.7] - 2022-01-28

### Added

- Added `producer` component to `service` component, so you can access `producer` component inside endpoint controllers.

## [5.11.5] - 2022-01-25

### Added

- Now you can access the `:current-env` from the config component, case other components need this information.
- Producer component for Kafka messages.

## [5.10.4] - 2022-01-22

### Change

- Now telegram component expect a `datomic` entity instead of a more generic `database`.

## [4.10.4] - 2022-01-19

### Added

- Added `mocked-datomic` function so you can do unit test for datomic queries and insertions.

## [4.9.4] - 2021-12-28

### Fixed

- Fix Interceptor importing problem. Not finding the class.

## [4.9.3] - 2021-12-28

### Added

- Added callback query handler support to telegram bot component.

## [3.9.3] - 2021-12-27

### Added

- Added Datalevin database component. Compatible with Component framework by Stuart Sierra.

## [3.8.3] - 2021-12-27

### Fixed

- Remove dead code that was throwing exceptions, causing some false positives when consuming message commands.

### Change

- Decouple template message dir value from component code, now you can use the config.json file to define the value used
  by the component.

## [2.8.2] - 2021-12-27

### Changed

- More flexibility for telegram bot consumer command functions definitions.
- Use interceptor when handling telegram bot command messages.

## [1.8.2] - 2021-12-27

### Added

- Added Telegram Bot component. Compatible with Component framework by Stuart Sierra.

## [1.7.2] - 2021-12-04

### Added

- Added `uuid` test helper function to generate random Uuids to be use for mocked values in tests.

## [1.6.2] - 2021-10-24

### Added

- Added `routes` and `service` components.

### Fixed

- `local-date->str` function was not returning the date in the chosen string format.

## [1.5.1] - 2021-10-24

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

## [0.1.0] - 2021-09-05

### Added

- Add `loose-schema` function.

[Unreleased]: https://github.com/macielti/common-clj/compare/v8.13.8...HEAD

[8.13.9]: https://github.com/macielti/common-clj/compare/v8.13.8...v8.13.9

[8.13.8]: https://github.com/macielti/common-clj/compare/v8.12.8...v8.13.8

[8.12.8]: https://github.com/macielti/common-clj/compare/v7.12.8...v8.12.8

[7.12.8]: https://github.com/macielti/common-clj/compare/v6.12.7...v7.12.8

[6.12.7]: https://github.com/macielti/common-clj/compare/v5.12.7...v6.12.7

[5.12.7]: https://github.com/macielti/common-clj/compare/v5.11.7...v5.12.7

[5.11.7]: https://github.com/macielti/common-clj/compare/v5.11.5...v5.11.7

[5.11.5]: https://github.com/macielti/common-clj/compare/v5.10.4...v5.11.5

[5.10.4]: https://github.com/macielti/common-clj/compare/v4.10.4...v5.10.4

[4.10.4]: https://github.com/macielti/common-clj/compare/v4.9.4...v4.10.4

[4.9.4]: https://github.com/macielti/common-clj/compare/v4.9.3...v4.9.4

[4.9.3]: https://github.com/macielti/common-clj/compare/v3.9.3...v4.9.3

[3.9.3]: https://github.com/macielti/common-clj/compare/v3.8.3...v3.9.3

[3.8.3]: https://github.com/macielti/common-clj/compare/v2.8.2...v3.8.3

[2.8.2]: https://github.com/macielti/common-clj/compare/v1.8.2...v2.8.2

[1.8.2]: https://github.com/macielti/common-clj/compare/v1.7.2...v1.8.2

[1.7.2]: https://github.com/macielti/common-clj/compare/v1.6.2...v1.7.2

[1.6.2]: https://github.com/macielti/common-clj/compare/v1.5.1...v1.6.2

[1.5.1]: https://github.com/macielti/common-clj/compare/v0.5.1...v1.5.1

[0.5.1]: https://github.com/macielti/common-clj/compare/v0.4.1...v0.5.1

[0.4.1]: https://github.com/macielti/common-clj/compare/v0.3.1...v0.4.1

[0.3.1]: https://github.com/macielti/common-clj/compare/v0.2.1...v0.3.1

[0.2.1]: https://github.com/macielti/common-clj/compare/v0.1.1...v0.2.1

[0.1.1]: https://github.com/macielti/common-clj/compare/v0.1.0...v0.1.1

[0.1.0]: https://github.com/macielti/common-clj/compare/v0.1.0...v0.1.0
