# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Added

- Added :http-client component, so we can have more flexibility on integration tests assertions. Like having the
  possibility of assert that an HTTP request was made to a service/URI.

## [18.22.19] - 2022-09-23

### Fixed

- Adapt from Kafka records to clj message for consumed messages kafka consumer helper
  function (`common-clj.component.kafka.consumer/consumed-messages`).

## [17.22.19] - 2022-09-21

### Added

- Added `http` component responsible for service authentication, so we can expect authenticated http requests between
  microservices.

## [17.21.19] - 2022-09-18

### Fixed

- Just cleaning and refactoring code. Also finishing some unfinished work.

## [17.21.18] - 2022-09-17

### Added

- Google reCAPTCHA v3 interceptor. Now you can easily add server side Google reCAPTCHA v3 validations, just add
  the `common-clj.io.interceptors.auth/recaptcha-validation-interceptor` to the list of interceptors while handling
  requests for http endpoints and add the `:recaptcha-secret-token` to the config file. Tested
  with [reCAPTCHA v3](https://developers.google.com/recaptcha/docs/v3#programmatically_invoke_the_challenge).

## [17.20.18] - 2022-09-14

### Changed

- Now we have the API as public by default by setting the `::http/allowed-origins` from the system map
  as `(constantly true)` like `"*"`.

## [16.20.18] - 2022-08-25

### Added

- Added implementation of correlation-id metadata for tracking Kafka message production and consumption through
  microservices.

## [16.19.18] - 2022-08-14

### Added

- Added implementation of `X-Correlation-Id` header for tracking http requests made between microservices. To use it
  wrap the http-handler function with `common-clj.traceability/with-correlation-id`.

## [16.18.18] - 2022-08-03

### Added

- Added the `:dead-letter-queue-service-integration-enabled` (`true`|`false`) config option to toggle the integration
  with a DLQ service (aka [Wraith-King](https://github.com/macielti/wraith-king)).

## [15.18.18] - 2022-07-23

### Fixed

- `MockKafkaConsumer` component throws an exception if not set up correctly. Make debug process easier while testing.

## [15.18.17] - 2022-07-22

### Added

- Added `common-clj.time.parser.core/date->wire` function to convert Date object to ISO-8601 string.
- Added `common-clj.time.parser.core/wire->date` function to convert ISO-8601 string into Date object.
- Added `common-clj.test.helper.time/valid-iso-8601?` function to validate iso-8601 strings.

### Fixed

- Fixed `detail` expected param type for `common-clj.error.core/http-friendly-exception` function.

## [15.17.16] - 2022-07-21

### Fixed

- Fixed `common-clj.money.converter/->cents` function expected output type from BigInteger to BigInt.

## [15.17.15] - 2022-07-16

### Changed

- Added integration with DLQ service.
- Now MockConsumer component depends on MockProducer, to fetch produced messages in the test environment.
- Now MockProducer component doesn't depend on MockConsumer anymore.

### Fixed

- Fixed infinite loop for Kafka messages that throws exceptions while being consumed (Mocked components).

## [14.17.14] - 2022-07-16

### Added

- Added `common-clj.io.interceptors.datomic/resource-existence-check-interceptor` Datomic interceptor.

## [14.16.14] - 2022-07-15

### Fixed

- The mock consumer only tries to consume specified topics defined by the config file. That is the expected behavior.

## [14.16.13] - 2022-07-14

### Added

- Added prismatic schemas for Kafka message producer input and consumer output (when we internalize Kafka consumer
  message).

### Changed

- Kafka consumer now pass only message content to topic handler instead of entire Kafka record with topic and message.
- Changed `common-clj.kafka.consumer/kafka-record->clj-message` to support more flexible Kafka message content in the
  future.
- Add schema validation for Kafka message payload while consuming messages.

## [13.16.12] - 2022-07-09

### Added

- Added `common-clj.auth.core/->token` function that we can use to convert Clojure maps containing authentication
  related information to a JWToken.

### Changed

- The `common-clj.test.helper/uuid` function was moved to another place due to better code organization, instead use the
  one from `common-clj.test.helper.core/uuid`.

## [12.15.12] - 2022-03-01

### Fixed

- Fixed response schema inconsistency for error handler interceptor response (for 500 status code errors)

## [11.15.12] - 2022-02-28

### Added

- Added the interceptor `schema-body-in-interceptor` that make so much simpler to set up schema validation for the json
  body content entering the service endpoint.
- Added a function `http-friendly-exception` to easily throw exceptions that are compatible with the global error
  catcher exception, returning a http response that follows best practices for REST API error handling.

## [10.15.12] - 2022-02-26

### Added

- Add support to EDN files for Config component.

## [9.14.12] - 2022-02-26

### Fixed

- Now Kafka mocked producer and consumer encodes messages using `ProducerRecord` the same way as in production
  environment.

## [9.14.10] - 2022-02-23

### Added

- Added time util function `now-datetime` that enables us to mock the usage of the java.util.Date class. Make
  integration tests more easy to write.

## [9.13.10] - 2022-02-09

### Fixed

- Using `stop-and-reset-pool!` instead of `shutdown-pool-gracefully!` (this is a private function).

## [9.13.9] - 2022-02-05

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

[Unreleased]: https://github.com/macielti/common-clj/compare/v18.22.19...HEAD

[18.22.19]: https://github.com/macielti/common-clj/compare/v17.22.19...v18.22.19

[17.22.19]: https://github.com/macielti/common-clj/compare/v17.21.19...v17.22.19

[17.21.19]: https://github.com/macielti/common-clj/compare/v17.21.18...v17.21.19

[17.21.18]: https://github.com/macielti/common-clj/compare/v17.20.18...v17.21.18

[17.20.18]: https://github.com/macielti/common-clj/compare/v16.20.18...v17.20.18

[16.20.18]: https://github.com/macielti/common-clj/compare/v16.19.18...v16.20.18

[16.19.18]: https://github.com/macielti/common-clj/compare/v16.18.18...v16.19.18

[16.18.18]: https://github.com/macielti/common-clj/compare/v15.18.18...v16.18.18

[15.18.18]: https://github.com/macielti/common-clj/compare/v15.18.17...v15.18.18

[15.18.17]: https://github.com/macielti/common-clj/compare/v15.17.16...v15.18.17

[15.17.16]: https://github.com/macielti/common-clj/compare/v15.17.15...v15.17.16

[15.17.15]: https://github.com/macielti/common-clj/compare/v14.17.14...v15.17.15

[14.17.14]: https://github.com/macielti/common-clj/compare/v14.16.14...v14.17.14

[14.16.14]: https://github.com/macielti/common-clj/compare/v14.16.13...v14.16.14

[14.16.13]: https://github.com/macielti/common-clj/compare/v13.16.12...v14.16.13

[13.16.12]: https://github.com/macielti/common-clj/compare/v12.15.12...v13.16.12

[12.15.12]: https://github.com/macielti/common-clj/compare/v11.15.12...v12.15.12

[11.15.12]: https://github.com/macielti/common-clj/compare/v10.15.12...v11.15.12

[10.15.12]: https://github.com/macielti/common-clj/compare/v9.14.12...v10.15.12

[9.14.12]: https://github.com/macielti/common-clj/compare/v9.14.10...v9.14.12

[9.14.10]: https://github.com/macielti/common-clj/compare/v9.13.10...v9.14.10

[9.13.10]: https://github.com/macielti/common-clj/compare/v9.13.9...v9.13.10

[9.13.9]: https://github.com/macielti/common-clj/compare/v8.13.8...v9.13.9

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
