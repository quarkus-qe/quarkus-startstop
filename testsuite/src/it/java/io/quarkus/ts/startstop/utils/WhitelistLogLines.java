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
 * Whitelists errors in log files.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum WhitelistLogLines {
    JAX_RS_MINIMAL(new String[]{
            // Some artifacts names...
            "maven-error-diagnostics",
            "errorprone"
    }),
    FULL_MICROPROFILE(new String[]{
            // Some artifacts names...
            "maven-error-diagnostics",
            "errorprone",
            // Needs fixing in the demo app?
            "TestSecureController.java",
    }),
    GENERATED_SKELETON(new String[]{
            // It so happens that the dummy skeleton tries to find Mongo. This is expected.
            // See app-generated-skeleton/README.md for explanation of the scope.
            "The remote computer refused the network connection",
            // Harmless warning
            "The Agroal dependency is present but no JDBC datasources have been defined",
            // Due to our not exactly accurate application.properties, these expected warnings occur...
            "Unrecognized configuration key \"quarkus.oidc.auth-server-url\" was provided",
            "Unrecognized configuration key \"quarkus.oidc.client-id\" was provided",
            "Unrecognized configuration key \"quarkus.smallrye-jwt.enabled\" was provided",
            "Unrecognized configuration key \"quarkus.jaeger.service-name\" was provided",
            "Unrecognized configuration key \"quarkus.jaeger.sampler-param\" was provided",
            "Unrecognized configuration key \"quarkus.jaeger.endpoint\" was provided",
            "Unrecognized configuration key \"quarkus.jaeger.sampler-type\" was provided",
            // Hmm, weird, right? Deprecations should be fixed
            "`io.vertx.reactivex.core.Vertx` is deprecated",
            // Some artifacts names...
            "maven-error-diagnostics",
            "errorprone"
    });

    public final String[] errs;

    WhitelistLogLines(String[] errs) {
        this.errs = errs;
    }
}
