# HTTP Client Component

Should be used to perform HTTP requests. It allows you to automatically expose metrics about the requests made by your
system. Also allows you to easily check the requests made during an integration test.

Under the hood, it uses the `clj-http` library to perform the requests.

## Metrics

The metrics exposed by this component are:

- A total count of the requests made along with the status code of the response for each endpoint.

The metrics are exposed using Prometheus. And you should add the prometheus component to your system map to expose them
and also add the Prometheus component as a dependency for the HTTP Client component.

## Examples of usage:

In this first example we are performing a get request for the Google homepage.
The `:endpoint-id` property is used only to label the request in the metrics. It is not mandatory.

```clojure
(component.http-client/request! {:url         "https://google.com.br"
                                 :method      :get
                                 :endpoint-id :xp-fetch-google-homepage} http-client)
```