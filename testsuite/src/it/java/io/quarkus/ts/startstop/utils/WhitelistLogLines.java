package io.quarkus.ts.startstop.utils;

import java.util.regex.Pattern;

/**
 * Whitelists errors in log files.
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
                    "quarkus.devservices.enabled|" +
                    "quarkus.jaeger.enabled|" +
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
            Pattern.compile(".*SRGQL010000: Schema is null, or it has no operations. Not bootstrapping SmallRye GraphQL.*"),
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
            // We have disabled the Quarkus Registry Client (-DquarkusRegistryClient=false)
            Pattern.compile(".*The extension catalog will be narrowed to.*"),
            // comes with https://github.com/quarkusio/quarkus/pull/20182
            Pattern.compile(".*Hibernate ORM is disabled because no JPA entities were found.*"),
            Pattern.compile(".*Hibernate Reactive is disabled because no JPA entities were found.*"),
            Pattern.compile(".*Skipping registration of ReactiveSessionFactoryProducer and ReactiveSessionProducer because exactly one persistence unit is required for their registration.*"),
            // https://github.com/netty/netty/issues/11020
            Pattern.compile(".*Can not find \\{\\} in the classpath, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.*"),
            // comes with https://github.com/quarkusio/quarkus/pull/19969
            Pattern.compile(".*OIDC metadata discovery failed:.*Connection refused.*localhost/127.0.0.1:6661.*"),
            // Attempted to read Testcontainers configuration file at file:/home/runner/.testcontainers.properties but the file was not found.
            Pattern.compile(".*Attempted to read Testcontainers configuration file at.*"),
            // 2021-12-23 12:57:02,610 WARN  [org.apa.kaf.cli.NetworkClient] (smallrye-kafka-consumer-thread-0) [Consumer clientId=kafka-consumer-uppercase-in, groupId=code-with-quarkus] Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.
            Pattern.compile(".*org.apa.kaf.cli.NetworkClient.*"),
            Pattern.compile(".*org.apache.kafka.clients.NetworkClient.*"),
            Pattern.compile(".*SRMSG18216: No `group.id` set in the configuration, generate a random id:.*"),
            // Kafka codestart without dev service enabled is not super stable in reload scenario
            Pattern.compile(".*Message .* was not sent to Kafka topic 'words' - nacking message:.*"),
            Pattern.compile(".*Unable to write to Kafka from channel words-out.*"),
            Pattern.compile(".*io.smallrye.mutiny.subscription.MultiSubscriber.onError\\(MultiSubscriber.java.*"),
            // https://github.com/quarkusio/quarkus/issues/23382
            Pattern.compile(".*Unable to properly register the hierarchy of the following classes for reflection as they are not in the Jandex index.*"),
            // https://github.com/quarkusio/quarkus/issues/23387
            Pattern.compile(".*The configuration.*auto.offset.reset.* was supplied but isn't a known config.*"),
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
    private static final Pattern COMMON_SLF4J_API_DEPENDENCY_TREE = Pattern.compile(".*org.slf4j:slf4j-api:jar.*");
    private static final Pattern COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE = Pattern.compile(".*org.jboss.slf4j:slf4j-jboss-logmanager:jar.*");
    private static final Pattern COMMON_XML_APIS_DEPENDENCY_TREE = Pattern.compile(".*The artifact xml-apis:xml-apis:jar:2.0.2 has been relocated to xml-apis:xml-apis:.*");
    // When
    private static final Pattern WARNING_MISSING_OBJCOPY_NATIVE = Pattern.compile(".*objcopy executable not found in PATH.*");
    private static final Pattern WARNING_MISSING_OBJCOPY_RESULT_NATIVE = Pattern.compile(".*That also means that resulting native executable is larger as it embeds the debug symbols..*");

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
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        COMMON_XML_APIS_DEPENDENCY_TREE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                };
            case WINDOWS:
                return new Pattern[] {
                        COMMON_WARNING_DUPLICATE_ENTRY_NATIVE,
                        COMMON_WARNING_DUPLICATE_ENTRIES_NATIVE,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        COMMON_XML_APIS_DEPENDENCY_TREE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                        Pattern.compile(".*Uber JAR strategy is used for native image source JAR generation on Windows.*"),
                        // Randomly prints some SLF4J traces. Reported by https://github.com/quarkusio/quarkus/issues/16896
                        Pattern.compile(".*SLF4J:.*"),
                        // When network failures, Windows uses translateErrorToIOException method to throw IO exceptions
                        // The problem is that the method name contains "Error" and hence it became an offending line.
                        // For example: this is happening with using Mongo extension (without a Mongo instance).
                        Pattern.compile(".*translateErrorToIOException.*"),
                        // In Windows, randomly prints traces like:
                        // TRACE [io.qua.builder] (build-49) Starting step "io.quarkus.jaxb.deployment.JaxbProcessor..."
                        Pattern.compile(".*Starting step.*"),
                };
            case LINUX:
            	return new Pattern[] {
                        COMMON_WARNING_DUPLICATE_ENTRY_NATIVE,
                        COMMON_WARNING_DUPLICATE_ENTRIES_NATIVE,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        COMMON_XML_APIS_DEPENDENCY_TREE,
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
