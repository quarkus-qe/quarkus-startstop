package io.quarkus.ts.startstop.utils;

/**
 * Available endpoitns and expected content.
 */
public enum URLContent {
    JAKARTA_REST_MINIMAL(new String[][]{
            new String[]{"http://localhost:8080", "Hello from a simple Jakarta REST app."},
            new String[]{"http://localhost:8080/data/hello", "Hello World"}
    }),
    FULL_MICROPROFILE(new String[][]{
            new String[]{"http://localhost:8080", "Hello from a full MicroProfile suite"},
            new String[]{"http://localhost:8080/data/hello", "Hello World"},
            new String[]{"http://localhost:8080/data/config/injected", "Config value as Injected by CDI Injected value"},
            new String[]{"http://localhost:8080/data/config/lookup", "Config value from ConfigProvider lookup value"},
            new String[]{"http://localhost:8080/data/resilience", "Fallback answer due to timeout"},
            new String[]{"http://localhost:8080/q/health", "\"UP\""},
            new String[]{"http://localhost:8080/data/metric/timed", "Request is used in statistics, check with the Metrics call."},
            new String[]{"http://localhost:8080/q/metrics", "Controller_timed_request_seconds_count"},
            new String[]{"http://localhost:8080/data/secured/test", "Jessie specific value"},
            new String[]{"http://localhost:8080/q/openapi", "/resilience"},
            new String[]{"http://localhost:8080/data/client/test/parameterValue=xxx", "Processed parameter value 'parameterValue=xxx'"}
    }),
    GENERATED_SKELETON(new String[][]{
            new String[]{"http://localhost:8080/", "Congratulation! You are on landing page."},
            new String[]{"http://localhost:8080/hello", "Bye RESTEasy"},
            new String[]{"http://localhost:8080/hello-added", "Hello added"},
            new String[]{"http://localhost:8080/hello", "Bye from Quarkus REST"},
    });

    public final String[][] urlContent;

    URLContent(String[][] urlContent) {
        this.urlContent = urlContent;
    }
}
