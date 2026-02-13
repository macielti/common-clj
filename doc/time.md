# Recommendations while dealing with time representations

You should use `java.time.Instant` as the official internal representation of a point in time (with date and time).

While defining `Datalevin` schemas the instant type should be represented by `s/Inst`.

The schema that should be used to represent an instant in time at the wire layer should be
`common-clj.schema.extensions/InstantWire`.

To convert a wire instant to the internal representation, you should use: `common-clj.time.parser.core/wire->instant`.

To externalize an instant to wire you should use: `common-clj.time.parser.core/instant->wire`.

To convert an instant to legacy `java.util/Date` you should use: `common-clj.time.parser.core/instant->legacy-date`.

To create an instant representing `now` you should use:  `common-clj.time.parser.core/instant-now`.

To represent Calendar Dates (example: `'2024-09-07'`) you should use: `common-clj.schema.extensions/CalendarDateWire`.
