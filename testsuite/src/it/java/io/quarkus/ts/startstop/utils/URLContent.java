/*
 * Copyright (c) 2020 Contributors to the Quarkus StartStop project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.quarkus.ts.startstop.utils;

/**
 * Available endpoitns and expected content.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum URLContent {
    JAX_RS_MINIMAL(new String[][]{
            new String[]{"http://localhost:8080", "Hello from a simple JAX-RS app."},
            new String[]{"http://localhost:8080/data/hello", "Hello World"}
    }),
    FULL_MICROPROFILE(new String[][]{
            new String[]{"http://localhost:8080", "Hello from a full MicroProfile suite"},
            new String[]{"http://localhost:8080/data/hello", "Hello World"},
            new String[]{"http://localhost:8080/data/config/injected", "Config value as Injected by CDI Injected value"},
            new String[]{"http://localhost:8080/data/config/lookup", "Config value from ConfigProvider lookup value"},
            new String[]{"http://localhost:8080/data/resilience", "Fallback answer due to timeout"},
            new String[]{"http://localhost:8080/health", "\"UP\""},
            new String[]{"http://localhost:8080/data/metric/timed", "Request is used in statistics, check with the Metrics call."},
            new String[]{"http://localhost:8080/metrics", "ontroller_timed_request_seconds_count"},
            new String[]{"http://localhost:8080/data/secured/test", "Jessie specific value"},
            new String[]{"http://localhost:8080/openapi", "/resilience"},
            new String[]{"http://localhost:8080/data/client/test/parameterValue=xxx", "Processed parameter value 'parameterValue=xxx'"}
    }),
    GENERATED_SKELETON(new String[][]{
            new String[]{"http://localhost:8080", "Congratulations"},
            new String[]{"http://localhost:8080/bye", "bye"}
    });

    public final String[][] urlContent;

    URLContent(String[][] urlContent) {
        this.urlContent = urlContent;
    }
}
