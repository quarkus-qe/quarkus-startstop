package io.quarkus.ts.startstop.utils;

import static io.quarkus.ts.startstop.utils.OpenTelemetryCollector.GET_HELLO_INVOCATION_TRACED;
import static io.quarkus.ts.startstop.utils.OpenTelemetryCollector.GET_HELLO_TRACES_URL;

/**
 * Available endpoitns and expected content.
 */
public enum URLContent {
    JAKARTA_REST_MINIMAL(new String[][]{
            new String[]{"http://localhost:8080/data/hello", "Hello World"},
            new String[]{"http://localhost:8080", "Hello from a simple Jakarta REST app."}
    }),
    FULL_MICROPROFILE(new String[][]{
            new String[]{"http://localhost:8080/q/health/ready", "\"UP\""},
            new String[]{"http://localhost:8080", "Hello from a full MicroProfile suite"},
            new String[]{"http://localhost:8080/data/hello", "Hello World"},
            new String[]{"http://localhost:8080/data/config/injected", "Config value as Injected by CDI Injected value"},
            new String[]{"http://localhost:8080/data/config/lookup", "Config value from ConfigProvider lookup value"},
            new String[]{"http://localhost:8080/data/resilience", "Fallback answer due to timeout"},
            new String[]{"http://localhost:8080/data/metric/timed", "Request is used in statistics, check with the Metrics call."},
            new String[]{"http://localhost:8080/q/metrics", "timed_request_seconds_count{class=\"com.example.quarkus.metric.MetricController\",exception=\"none\",method=\"timedRequest\"}"},
            new String[]{"http://localhost:8080/data/metric/increment", "1"}, // check counter incremented exactly once
            new String[]{"http://localhost:8080/q/metrics", "endpoint_counter_total 1.0"}, // counter metric must match
            new String[]{"http://localhost:8080/q/metrics", "counter_gauge"}, // check gauge for the counter exists
            new String[]{"http://localhost:8080/data/serialization/json/complex-dto", "Vive la résistance!"},
            new String[]{"http://localhost:8080/q/openapi", "/resilience"},
            new String[]{"http://localhost:8080/data/client/test/parameterValue=xxx", "Processed parameter value 'parameterValue=xxx'"},
            new String[]{GET_HELLO_TRACES_URL, GET_HELLO_INVOCATION_TRACED}
    }),
    GENERATED_SKELETON(new String[][]{
            new String[]{"http://localhost:8080/", "Congratulation! You are on landing page."},
            new String[]{"http://localhost:8080/hello", "Bye RESTEasy"},
            new String[]{"http://localhost:8080/hello-added", "Hello added"},
            new String[]{"http://localhost:8080/hello", "Bye from RESTEasy Reactive"},
    });

    public final String[][] urlContent;

    URLContent(String[][] urlContent) {
        this.urlContent = urlContent;
    }
}
