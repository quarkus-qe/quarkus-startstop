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

import java.util.regex.Pattern;

/**
 * Whitelists errors in log files.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum WhitelistLogLines {
    JAX_RS_MINIMAL(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core/src/com/oracle/svm/core/jdk/VarHandleFeature.java#L199
            Pattern.compile(".*GR-10238.*")
    }),
    FULL_MICROPROFILE(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // Needs fixing in the demo app?
            Pattern.compile(".*TestSecureController.java.*"),
            // Well, the RestClient demo probably should do some cleanup before shutdown...?
            Pattern.compile(".*Closing a class org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient.*"),
            // https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core/src/com/oracle/svm/core/jdk/VarHandleFeature.java#L199
            Pattern.compile(".*GR-10238.*")
    }),
    GENERATED_SKELETON(new Pattern[]{
            // It so happens that the dummy skeleton tries to find Mongo. This is expected.
            // See app-generated-skeleton/README.md for explanation of the scope.
            Pattern.compile(".*The remote computer refused the network connection.*"),
            // Harmless warning
            Pattern.compile(".*The Agroal dependency is present but no JDBC datasources have been defined.*"),
            // Due to our not exactly accurate application.properties, these expected warnings occur...
            Pattern.compile(".*Unrecognized configuration key[ \\\\\"]*(" +
                    "quarkus.oidc.auth-server-url|" +
                    "quarkus.oidc.client-id|" +
                    "quarkus.smallrye-jwt.enabled|" +
                    "quarkus.jaeger.service-name|" +
                    "quarkus.jaeger.sampler-param|" +
                    "quarkus.jaeger.endpoint|" +
                    "quarkus.jaeger.sampler-type" +
                    ")[ \\\\\"]*was provided.*"),
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            Pattern.compile(".*google-cloud-errorreporting-bom.*"),
            // When GraalVM is used; unrelated to the test
            Pattern.compile(".*forcing TieredStopAtLevel to full optimization because JVMCI is enabled.*"),
            Pattern.compile(".*error_prone_annotations.*"),
            Pattern.compile(".*SRGQL010000: Schema is null, or it has no operations. Not bootstrapping SmallRye GraphQL*"),
            Pattern.compile(".*No WebJars were found in the project.*"),
            // https://github.com/quarkusio/quarkus/issues/14442
            Pattern.compile(".*Producing values from constructors and fields is no longer supported and will be removed in a future release: io.quarkus.deployment.annotations.BuildProducer io.quarkus.reactivemessaging.http.deployment.ReactiveHttpProcessor.routeProducer*"),
    });

    public final Pattern[] errs;

    WhitelistLogLines(Pattern[] errs) {
        this.errs = errs;
    }

    public final Pattern[] platformErrs() {
        switch (OS.current()) {
            case MAC:
                return new Pattern[] {
                        Pattern.compile(".*objcopy executable not found in PATH. Debug symbols will not be separated from executable.*"),
                        Pattern.compile(".*That will result in a larger native image with debug symbols embedded in it.*"),
                };
        }
        return new Pattern[] {};
    }

    enum OS {
        MAC, LINUX, WINDOWS, UNKNOWN;
        public static OS current() {
            if (isMac()) {
                return MAC;
            } else if (isWindows()) {
                return WINDOWS;
            } else if (isLinux()) {
                return LINUX;
            } else {
                return UNKNOWN;
            }
        }
    }
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    private static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }
}
