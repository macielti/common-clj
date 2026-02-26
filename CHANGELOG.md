# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## 46.1.3 - 2026-02-25

### Changed

- Bumped dev tooling dependencies: logback-classic 1.5.32, lein-clojure-lsp 2.0.14,
  common-test-clj 7.0.2, service-component 7.4.4.

## 46.1.2 - 2026-02-25

### Added

- Added `PositiveInt` schema for validating positive integers (greater than 0). But not accepting BigInt values.
- Added `NonNegativeInt` schema for validating non-negative integers (greater than or equal to 0). But not accepting BigInt values.

## 46.1.1 - 2026-02-14

### Added

- Added regex variation for WireInstant schema.

## 46.0.1 - 2026-02-14

### Changed

- Removed `danlentz/clj-uuid` dependency, due to incompatibility with graalvm native image generation process.

## 46.0.0 - 2026-02-14

### Changed

- Better namespace organization.
- Removing deprecated functions.

## 45.1.1 - 2026-02-13

### Added

- Added function to convert from `java.util.Date` to `java.time.Instant`:
  `common-clj.time.parser.core/legacy-date->instant`.

## 45.0.1 - 2026-02-13

### Fixed

- Fix the schema output expected for `common-clj.time.core/instant-now`.

## 45.0.0 - 2026-02-13

### Changed/Added

- Set the standard way to deal with time representation.

## 44.2.0 - 2026-02-02

### Added

- Added function to generate custom format schemas representing string patterns: `custom-string-pattern`

## 44.1.0 - 2026-01-31

### Added

- Added function to generate custom format schemas representing stringfied Instants: `custom-formated-wire-instant`

## 43.74.77 - 2025-12-26

### Changed

- Replaced `ring` dependency with `ring/ring-codec` version `1.3.0` to resolve a dependency conflict.

## 43.74.76 - 2025-12-25

### Changed

- Updated project dependencies to their latest versions.
- Refactored integration tests for improved compatibility and to align with updated Pedestal test patterns.

## 43.74.75 - 2025-01-25

### Changed

- Refactored `common-clj.traceability.core` to be more flexible allowing more options to manipulate the
  `correlation-id`.

## [42.74.74] - 2025-01-08

### Removed

- Removed SQS Consumer and Producer components.
- Removed AWS Auth component.

## [41.74.74] - 2024-11-26

### Fixed

- Removed require to `io.pedestal.log`.

## [41.74.73] - 2024-11-23

### Fixed

- More support to different formats for `LocalDateTimeWire` schema.

## [41.73.73] - 2024-11-23

### Fixed

- More flexible `LocalDateTimeWire` schema.

## [41.73.72] - 2024-11-23

### Added

- Added `LocalDateTimeWire` and `UuidWire` schema extensions.

## [41.72.72] - 2024-11-19

### Removed

- Removed Prometheus component.

## [40.72.72] - 2024-11-17

### Changed

- Properly use bindings feature from Pedestal interceptors for CID handling.

## [39.72.72] - 2024-11-17

### Added

- Added `:http-request-in-handle-timing-v2` (summary) Prometheus component metric to measure the time spent while
  handling http requests.

### Removed

- Removed `:http-request-in-handle-timing` Prometheus component metric to measure the time spent while handling http
  requests.

## [38.72.72] - 2024-11-16

### Changed

- Bump dependencies.

## [38.72.71] - 2024-11-16

### Added

- Added `:http-request-in-handle-timing` Prometheus component metric to measure the time spent while handling http
  requests.

## [38.71.71] - 2024-11-10

### Removed

- Removed HTTP Client component.

## [37.71.71] - 2024-11-10

### Changed

- Replaced clj-http with clj-http-lite for better compatibility with GraalVM.

## [37.71.70] - 2024-11-08

### Removed

- Removed deprecated code.

## [36.71.70] - 2024-11-07

### Removed

- Removed deprecated code.

### Added

- Bump dependencies.

## [35.71.70] - 2024-11-07

### Removed

- Removed timbre logging library.

## [34.71.70] - 2024-11-07

### Changed

- Bump Clojure version.

## [34.70.70] - 2024-11-05

### Remove

- Removed all components from Stuart Sierra's component library.
- Removed `porteiro` related code.

## [33.70.70] - 2024-11-05

### Removed

- Removed Kafka, New Relic and Rate Limiter deprecated components related code.

## [32.70.70] - 2024-11-05

### Added

- Bump dependencies.

## [32.69.70] - 2024-11-03

### Removed

- Removed all code related with RabbitMQ and PostgreSQL components.
- Removed all code related with test containers.

## [31.69.70] - 2024-11-02

### Removed

- Removed all code related with Datomic and Datalevin components.

## [30.69.70] - 2024-11-02

### Fixed

- Remove resources files from project release files.

## [30.68.70] - 2024-11-02

### Changed

- Use PostgreSQL component (Integrant) for Porteiro source code instead of Datomic.

## [30.67.70] - 2024-11-01

### Added

- New `PostgreSQL` component (Integrant).

## [30.66.70] - 2024-10-10

### Added

- Added roles management for customer authentication features.

## [30.65.70] - 2024-10-10

### Added

- Added `src/common_clj/porteiro` namespace to hold customer authentication features.

## [30.64.70] - 2024-10-02

### Added

- Added `common-clj.io.interceptors.customer` namespace to hold customer identification specific interceptors.

## [30.63.70] - 2024-09-22

### Changed

- Instead of logging (at INFO level) each number of consumers threads up  `AWS SQS Consumer component (Integrant)`
  validation, now we are logging that info at DEBUG level.

## [30.63.69] - 2024-09-20

### Changed

- Now you can control the number of parallel consumer threads for each queue on `AWS SQS Consumer` component
  (Integrant).

## [29.63.69] - 2024-09-15

### Added

- Now you can control the timeout in ms for the `AWS SQS Consumer` component (Integrant) while handling each message.

## [29.62.69] - 2024-09-14

### Changed

- Applying more parallelism improvements to `AWS SQS Consumer component (Integrant)`.

## [29.62.68] - 2024-09-14

### Changed

- Parallelism improvements to `AWS SQS Consumer component (Integrant)`.

## [29.62.67] - 2024-09-14

### Fixed

- Fixed bug on `AWS SQS Consumer component (Integrant)`.

## [29.62.66] - 2024-09-14

### Added

- Now the AWS SQS Consumer component (Integrant) is able to consume messages applying some level of parallelism.

## [29.61.66] - 2024-09-13

### Changed

- Instead of logging (at INFO level) each message consumed by `AWS SQS Consumer component (Integrant)`, now we are
  logging that info at DEBUG level.

## [29.61.65] - 2024-09-13

### Fixed

- Fixed bug on `AWS SQS Producer component (Integrant)`. The component was trying to create queues on AWS SQS servers
  while executing tests.

## [29.61.64] - 2024-09-13

### Fixed

- Fixed expected schema param type for `queues` on `common-clj.integrant-components.sqs-consumer/create-sqs-queues!`.

## [29.61.63] - 2024-09-13

### Fixed

- Fix bug on `AWS SQS Producer component (Integrant)`. The component wasn't sending the params for the message produce
  operation properly.
- Removed calls to `#p`. This should be used only on debugging process.

### Added

- Try to create AWS SQS queues while starting `AWS SQS Producer component (Integrant)`.

## [29.61.62] - 2024-09-11

### Changed

- Minor improvement on Datomic (Integrant) component.

## [29.61.61] - 2024-09-11

### Added

- Retry policy for Datomic (Integrant) component to make it more resilient to database connection problems while staring
  the component.

## [29.60.61] - 2024-09-09

### Added

- Added AWS Auth (Integrant) component to define aws credentials globally.

### Changed

- AWS SQS Consumer component (Integrant) and AWS SQS Producer component (Integrant) now use the globally defined aws
  credentials from AWS Auth (Integrant) component.

## [29.59.61] - 2024-09-07

### Fixed

- Improvements on `common-clj.integrant-components.datomic/mocked-datomic` to avoid point to the same in-memory database
  path between tests.

### Added

- Added `common-clj.integrant-components.datomic/transact-and-lookup-entity!` function to make easier to transact and
  lookup entities in the database.
- Added full automatic test (unit) coverage for `common-clj.integrant-components.datomic` namespace.
- Added full automatic test (unit, integration) coverage for `common-clj.integrant-components.routes` namespace.

## [29.58.61] - 2024-09-06

### Fixed

- Fix bug on `AWS SQS Consumer component (Integrant)`. The component wasn't consuming messages properly (Test and Prod).

## [29.58.60] - 2024-09-06

### Fixed

- Fix bug on `AWS SQS Producer component (Integrant)`. The component wasn't sending the messages passing the right
  params.

## [29.58.59] - 2024-09-05

### Fixed

- Fix bug on `Cronjob component (Integrant)`. The component wasn't able to access the defined tasks properly.

## [29.58.58] - 2024-09-05

### Added

- Bump `org.clojure/core.async` version to `"1.6.681"`

## [29.58.57] - 2024-09-05

### Fixed

- Fix bug on `AWS SQS Consumer component (Integrant)` that was trying to create a queue on AWS (prod) while executing
  tests. Also reduce the that sleep between each message consumption cycle for test env.
- Use long pooling to fetch messages from AWS SQS.

## [29.57.57] - 2024-09-04

### Added

- Add Config component (Integrant).
- Add Routes component (Integrant).
- Add Service component (Integrant).
- Add AWS SQS Consumer component (Integrant).
- Add AWS SQS Producer component (Integrant).
- Add Prometheus component (Integrant).
- Add New Relic component (Integrant).
- Add HTTP Client component (Integrant).
- Add Cronjob component (Integrant).
- Add Datomic component (Integrant).

### Changed

- Deprecated Kafka Consumer component (stuartsierra/component) related code.
- Deprecated Kafka Producer component (stuartsierra/component) related code.
- Deprecated RabbitMQ Consumer component (stuartsierra/component) related code.
- Deprecated RabbitMQ Producer component (stuartsierra/component) related code.
- Deprecated Routes component (stuartsierra/component) related code.
- Deprecated Service component (stuartsierra/component) related code.
- Deprecated Prometheus component (stuartsierra/component) related code.
- Deprecated New Relic component (stuartsierra/component) related code.
- Deprecated HTTP Client component (stuartsierra/component) related code.
- Deprecated CronJob component (stuartsierra/component) related code.
- Deprecated Datomic component (stuartsierra/component) related code.

## [29.56.57] - 2024-08-27

### Changed

- Replace `com.datomic/datomic-free` and `com.datomic/local` by `com.datomic/peer`.

### Removed

- Removed `DatomicLocal` Component in favor of main `Datomic` component.

## [28.56.57] - 2024-08-25

### Added

- Added log to show the result of datomic database creation.

## [28.56.56] - 2024-08-22

### Added

- Added `com.datomic/peer` dependency to the project.

## [28.56.55] - 2024-08-22

### Added

- Add support to BigDecimal on `common-clj.test.helper.schema/generate`.
- Add Datomic component support to CronJob component.

### Changed

- Changed the way that the Datomic connection is retrieved from the Datomic component.

## [27.56.55] - 2024-08-17

### Added

- Add support to PATH HTTP Request method for HTTP Client component.

## [27.55.55] - 2024-08-17

### Added

- Make Http Client component expose metrics via Prometheus component.

## [27.55.54] - 2024-08-16

### Fixed

- Fixed bug on `New Relic` component. The component wasn't sending the logs payload properly.

## [27.55.53] - 2024-08-14

### Added

- Implement Correlation ID for RabbitMQ consumer and producer components.
- Minor improvements to CID implementation.
- Also added CID support to scheduled jobs (CronJob component).

## [26.55.53] - 2024-08-11

### Added

- Minor improvements on New Relic component.

## [26.54.53] - 2024-08-10

### Added

- Implemented New Relic component to send logs to New Relic.

## [26.53.53] - 2024-08-08

### Added

- Implemented schema generator (`common-clj.test.helper.schema/generate`) to help with fixture definition on tests.

## [26.52.53] - 2024-08-02

### Added

- Implemented `Containers` component. Now you can use it to start and stop docker containers in your integration tests.

## [25.52.53] - 2024-07-28

### Added

- Just some housekeeping adding support to component dependency for prometheus and http-client.

## [25.52.52] - 2024-07-25

### Added

- Add `LocalDateWire` schema extension.

## [25.52.51] - 2024-07-25

### Added

- Add misc function to remove namespace from keywords in a map.

## [25.52.50] - 2024-07-21

### Added

- Add CronJob component.

## [25.51.50] - 2024-07-21

### Changed

- Migrate from [prismatic/plumbing](https://clojars.org/prismatic/plumbing) to [medley](https://clojars.org/medley).

## [25.51.49] - 2024-07-12

### Changed

- Updating project dependencies.

## [25.51.48] - 2024-06-06

### Added

- Add support to user data from Telegram consumer component updates.

## [25.50.48] - 2024-06-05

### Added

- Add support to Datalevin on Telegram consumer component.

## [25.49.48] - 2024-02-18

### Changed

- Changed implementation of PostgreSQL component to use connection pooling.

## [24.49.48] - 2023-11-30

### Fixed

- Fixed bug on `TelegramConsumer` component.

## [24.49.47] - 2023-11-28

### Added

- Add schema model for `TelegramProducer` component.

## [24.48.47] - 2023-11-27

### Added

- Add TelegramProducer component as dependency for TelegramConsumer component.

## [24.47.47] - 2023-11-26

### Added

- Add Telegram producer component in order to make easier to write integration tests for telegram bots.

## [24.46.47] - 2023-11-25

### Changed

- Refactors to `TelegramConsumer` in order to define command interceptors in a way that allows a more reusable and
  extendable code.

## [23.46.47] - 2023-11-24

### Added

- Add handler for Telegram bot callback queries. Now the data from the callback query is treated as a bot command.

## [23.45.47] - 2023-11-20

### Fixed

- Fixed logs not showing up on terminal

## [23.45.46] - 2023-11-18

### Added

- Add `RateLimiter` component, and now you can use it with interceptors in oder to apply rate limit on service
  endpoints.

## [23.44.46] - 2023-11-14

### Added

- Add `Prometheus` component to `TelegramConsumer` possible dependencies.

## [23.43.46] - 2023-11-14

### Added

- Add `Prometheus` component in order to expose metrics to agentless monitoring for Prometheus in Grafana Cloud.

## [23.42.46] - 2023-11-12

### Fixed

- Fixed problem with `DatomicLocal` component while running integration tests, the database was not being cleaned after
  each test execution.

## [23.42.45] - 2023-11-11

### Fixed

- Removed call to `#p`. This should be used only on debugging process.

## [23.42.44] - 2023-11-11

### Added

- Add `common-clj.keyword.core/un-namespaced` function in oder to be able to convert namespaced keywords.
- Add `common-clj.schema.core/un-namespaced` function in oder to be able to convert namespaced schemas.

## [23.41.44] - 2023-11-10

### Added

- Add `common-clj.time.core/date->local-date` function in oder to be able to convert `Date` to `LocalDate`

## [23.40.43] - 2023-11-07

### Fixed

- Fixed `TelegramConsumer` to stop sending Telegram Bot messages for not found commands if the message received was not
  a command.

## [23.40.42] - 2023-11-06

### Added

- Add support to Telegram channels for `TelegramConsumer` component.

## [23.40.41] - 2023-11-06

### Fixed

- Fixed TelegramConsumer component bug while inferring message type

### Added

- Add http-client functions in order to fetch files sent via Telegram bot command messages.

## [23.40.40] - 2023-11-05

### Removed

- Removed `TelegramWebhookConsumer` component, now you should use `TelegramConsumer`.

### Changed

- Refactors to `TelegramConsumer` in order to be able to scale new features more easily.

## [22.40.40] - 2023-11-03

### Added

- Added `DatomicLocal` component.

## [22.39.40] - 2023-10-29

### Added

- Add `:jobs` component placeholder for telegram component. Now you can depend on the `jobs` component from telegram.

## [22.38.40] - 2023-09-16

### Added

- Add functions to `common-clj.time.core` in order to deal with `LocalDateTime`.

## [22.35.40] - 2023-09-04

### Added

- Add a lower threshold for Google's Recaptcha V3 interceptor, also log the result of the validations.

## [22.35.38] - 2023-09-03

### Added

- Add a `get-connection` function for PostgreSQL component.

## [21.34.38] - 2023-09-02

### Added

- Split PostgreSQL component into PostgreSQL and MockPostgreSQL in order make easier to write integration tests.

## [20.33.38] - 2023-09-02

### Added

- Implemented PostgreSQL utilities for writing tests.

## [20.32.37] - 2023-08-27

### Added

- Implemented integration of PostgreSQL component with RabbitMQ consumer and Service component.

## [20.31.36] - 2023-08-22

### Added

- Implemented PostgreSQL component.

## [20.30.36] - 2023-08-12

### Added

- Implemented integration of RabbitMQ consumer component with Dead Letter Queue
  service [Wraith King](https://github.com/macielti/wraith-king).
- Added schema validation for message payloads for RabbitMQ consumer component.

## [19.30.36] - 2023-08-01

### Added

- Added schema validation for consumers config map for RabbitMQ consumer component.

## [19.29.36] - 2023-07-30

### Added

- Added RabbitMQ producer and consumer components.

## [19.28.36] - 2023-07-27

### Fixed

- Fixed Datalevin component. There was a problem with the way that we fetch the database-uri configuration.

### Added

- Added configuration assert checks for Datalevin component, check if the `database-uri` config is properly provided.

## [19.28.35] - 2023-07-27

### Added

- Added Datalevin component along with interceptors.

## [19.27.35] - 2023-07-02

### Fixed

- Upgrade `org.apache.kafka/kafka-clients` dependency version from `2.8.0` to `3.4.0`.
- Upgrade `nubank/state-flow` dependency version from `5.14.3` to `5.14.4`.
- Upgrade `telegrambot-lib` dependency version from `1.4.0` to `2.5.0`.
- Upgrade `prismatic/schema-generators` dependency version from `0.1.4` to `0.1.5`.
- Fix some problems with datalevin and had to set some jvm options.

## [19.27.34] - 2023-06-28

### Fixed

- Upgrade `datalevin` dependency version from `0.6.22` to `0.8.16`.

## [19.27.33] - 2022-12-18

### Added

- Added integration test for Telegram Bot update consumption via web hook endpoint.
- Added `component.telegram.consumer/consume-update-via-webhook` function, so you can easily mock Telegram Bot command
  messages update consumption via webhook http endpoint.
- Added a function to generate telegram bot handler http request hooker
  endpoint `fn` `component.telegram.consumer/telegram-bot-webhook-endpoint-handler-fn`.

## [19.27.32] - 2022-12-07

### Added

- Added `TelegramWebhookConsumer` to have one more way of integrating with telegram bot api.

## [19.26.32] - 2022-11-29

### Fixed

- Fixed Telegram consumer for callback query update consumption. Before the fix the consumer wasn't able to retrieve
  the `chat-id` from the update.

## [19.26.30] - 2022-11-26

### Fixed

- `TelegramConsumer` was breaking while consuming updates for edited messages. Now it's fixed.

## [19.25.22] - 2022-11-22

### Added

- Now Telegram Consumer component logs the exception when it faces errors while consuming a command.
- Added Http Client to list of components that we can use on Telegram command handlers (consumers).

### Removed

- Removed Telegram Producer component, now you should use `(morse-api/send-text token chat-id message-text)` to send
  telegram messages.

## [18.24.21] - 2022-10-27

### Fixed

- Now `TelegramConsumer` is more resilient to errors wire consuming updates from Telegram API. Before that the Thread
  was dying when facing a error, but now the Thread is treating and logging the error.

### Added

- Added `MockTelegramConsumer` component along with some helpers functions so you can easily write integration tests for
  telegram bots.

## [18.23.20] - 2022-10-16

### Added

- Added `:http-client` component, so we can have more flexibility on integration tests assertions. Like having the
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
  integration tests easier to write.

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
