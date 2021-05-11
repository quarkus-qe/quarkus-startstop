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
    }),
    FULL_MICROPROFILE(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // Needs fixing in the demo app?
            Pattern.compile(".*TestSecureController.java.*"),
            // Well, the RestClient demo probably should do some cleanup before shutdown...?
            Pattern.compile(".*Closing a class org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient.*"),
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
                    "quarkus.oidc-client.auth-server-url|" +
                    "quarkus.oidc-client.client-id|" +
                    "quarkus.oidc-client.token-path|" +
                    "quarkus.oidc-client.discovery-enabled|" +
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
            Pattern.compile(".*This application uses the MP Metrics API. The micrometer extension currently provides a compatibility layer that supports the MP Metrics API, but metric names and recorded values will be different. Note that the MP Metrics compatibility layer will move to a different extension in the future.*"),
            // kubernetes-client tries to configure client from service account
            Pattern.compile(".*Error reading service account token from.*"),
            // hibernate-orm issues this warning when default datasource is ambiguous
            // (no explicit configuration, none or multiple JDBC driver extensions)
            // Result of DevServices support https://github.com/quarkusio/quarkus/pull/14960
            Pattern.compile(".*Unable to determine a database type for default datasource.*"),
            // Maven 3.8.1 throw a warn msg related to a mirror default configuration
            Pattern.compile(".*org.apache.maven.settings.io.SettingsParseException: Unrecognised tag: 'blocked'.*"),
    }),
    // Quarkus is not being gratefully shutdown in Windows when running in Dev mode.
    // Reported by https://github.com/quarkusio/quarkus/issues/14647.
    WINDOWS_DEV_MODE_ERRORS(new Pattern[]{
            Pattern.compile(".*Re-run Maven using the -X switch to enable full debug logging.*"),
            Pattern.compile(".*For more information about the errors and possible solutions, please read the following articles.*"),
            Pattern.compile(".*Failed to run: Dev mode process did not complete successfully.*"),
            Pattern.compile(".*http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException.*"),
            Pattern.compile(".*To see the full stack trace of the errors, re-run Maven with the -e switch.*"),
            Pattern.compile("\\[ERROR\\] *"),
    });
    
    // Depending to the OS and also on the Quarkus extensions, the Native build might print some warnings about duplicate entries
    private static final Pattern COMMON_WARNING_DUPLICATE_ENTRY_NATIVE = Pattern.compile(".*Duplicate entry about.html entry.*");
    private static final Pattern COMMON_WARNING_DUPLICATE_ENTRIES_NATIVE = Pattern.compile(".*Dependencies with duplicate files detected.*");
    // When
    private static final Pattern WARNING_MISSING_OBJCOPY_NATIVE = Pattern.compile(".*objcopy executable not found in PATH. Debug symbols will not be separated from executable.*");
    private static final Pattern WARNING_MISSING_OBJCOPY_RESULT_NATIVE = Pattern.compile(".*That will result in a larger native image with debug symbols embedded in it.*");

    public final Pattern[] errs;

    WhitelistLogLines(Pattern[] errs) {
        this.errs = errs;
    }

    public final Pattern[] platformErrs() {
        switch (OS.current()) {
            case MAC:
                return new Pattern[] {
                        COMMON_WARNING_DUPLICATE_ENTRY_NATIVE,
                        COMMON_WARNING_DUPLICATE_ENTRIES_NATIVE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                };
            case WINDOWS:
                return new Pattern[] {
                        COMMON_WARNING_DUPLICATE_ENTRY_NATIVE,
                        COMMON_WARNING_DUPLICATE_ENTRIES_NATIVE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                        Pattern.compile(".*Uber JAR strategy is used for native image source JAR generation on Windows.*"),
                        // Randomly fails when vertx-cache temporary directory exists. Reported by https://github.com/quarkusio/quarkus/issues/16895
                        Pattern.compile(".*Unable to make the Vert.x cache directory.*"),
                        // Randomly prints some SLF4J traces. Reported by https://github.com/quarkusio/quarkus/issues/16896
                        Pattern.compile(".*SLF4J:.*"),
                        // When network failures, Windows uses translateErrorToIOException method to throw IO exceptions
                        // The problem is that the method name contains "Error" and hence it became an offending line.
                        // For example: this is happening with using Mongo extension (without a Mongo instance).
                        Pattern.compile(".*translateErrorToIOException.*"),
                };
            case LINUX:
            	return new Pattern[] {
                        COMMON_WARNING_DUPLICATE_ENTRY_NATIVE,
                        COMMON_WARNING_DUPLICATE_ENTRIES_NATIVE,
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
