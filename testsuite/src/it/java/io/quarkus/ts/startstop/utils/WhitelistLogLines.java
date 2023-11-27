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
            // https://github.com/quarkusio/quarkus/issues/27307
            Pattern.compile(".*Unknown module: org.graalvm.nativeimage.llvm specified to --add-exports.*"),
            // https://github.com/quarkusio/quarkus/issues/28799 (should be removed once 2.14.0.Final is out)
            Commons.STREAM_ERROR,
            Commons.CLOSED_STREAM,
            Commons.NETTY_HANDLERS,
    }),
    FULL_MICROPROFILE(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // Well, the RestClient demo probably should do some cleanup before shutdown...?
            Pattern.compile(".*Closing a class org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient.*"),
            // https://github.com/quarkusio/quarkus/issues/27307
            Pattern.compile(".*Unknown module: org.graalvm.nativeimage.llvm specified to --add-exports.*"),
            Commons.STREAM_ERROR,
            Commons.CLOSED_STREAM,
            Commons.NETTY_HANDLERS,
    }),
    GENERATED_SKELETON(new Pattern[]{
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
            Pattern.compile(".*error_prone_annotations.*"),
            Pattern.compile(".*No WebJars were found in the project.*"),
            // kubernetes-client tries to configure client from service account
            Pattern.compile(".*Error reading service account token from.*"),
            // We have disabled the Quarkus Registry Client (-DquarkusRegistryClient=false)
            Pattern.compile(".*The extension catalog will be narrowed to.*"),
            // comes with https://github.com/quarkusio/quarkus/pull/20182
            Pattern.compile(".*Hibernate ORM is disabled because no JPA entities were found.*"),
            Pattern.compile(".*Hibernate Reactive is disabled because no JPA entities were found.*"),
            // https://github.com/netty/netty/issues/11020
            Pattern.compile(".*Can not find io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider in the classpath, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.*"),
            // comes with https://github.com/quarkusio/quarkus/pull/19969 https://github.com/quarkusio/quarkus/pull/26868 https://github.com/quarkusio/quarkus/pull/27811
            Pattern.compile(".*OIDC Server is not available.*Connection refused.*localhost/127.0.0.1:6661.*"),
            // Attempted to read Testcontainers configuration file at file:/home/runner/.testcontainers.properties but the file was not found.
            Pattern.compile(".*Attempted to read Testcontainers configuration file at.*"),
            // 2021-12-23 12:57:02,610 WARN  [org.apa.kaf.cli.NetworkClient] (smallrye-kafka-consumer-thread-0) [Consumer clientId=kafka-consumer-uppercase-in, groupId=code-with-quarkus] Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.
            Pattern.compile(".*org.apa.kaf.cli.NetworkClient.*"),
            Pattern.compile(".*org.apache.kafka.clients.NetworkClient.*"),
            Pattern.compile(".*SRMSG18216: No `group.id` set in the configuration, generate a random id:.*"),
            // Kafka codestart without dev service enabled is not super stable in reload scenario
            Pattern.compile(".*SRMSG18212: Message.*was not sent to Kafka topic.*nacking message.*"),
            Pattern.compile(".*SRMSG18206: Unable to write to Kafka from channel.*"),
            Pattern.compile(".*io.smallrye.mutiny.subscription.MultiSubscriber.onError\\(MultiSubscriber.java.*"),
            // https://github.com/quarkusio/quarkus/issues/27307
            Pattern.compile(".*Unknown module: org.graalvm.nativeimage.llvm specified to --add-exports.*"),
            // https://github.com/quarkusio/quarkus/issues/28799 (should be removed once 2.14.0.Final is out)
            Commons.STREAM_ERROR,
            Commons.CLOSED_STREAM,
            Commons.NETTY_HANDLERS,
            // org.slf4j warning about cached artifacts from a remote repository ID that is unavailable
            Pattern.compile(".*present in the local repository, but cached from a remote repository ID that is unavailable in current build context, verifying that is downloadable.*"),
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
    private static final Pattern COMMON_SLF4J_API_DEPENDENCY_TREE = Pattern.compile(".*org.slf4j:slf4j-api:jar.*");
    private static final Pattern COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE = Pattern.compile(".*org.jboss.slf4j:slf4j-jboss-logmanager:jar.*");
    private static final Pattern WARNING_MISSING_OBJCOPY_NATIVE = Pattern.compile(".*objcopy executable not found in PATH.*");
    private static final Pattern WARNING_MISSING_OBJCOPY_RESULT_NATIVE = Pattern.compile(".*That also means that resulting native executable is larger as it embeds the debug symbols..*");
    private static final Pattern XML_APIS_RELOCATED = Pattern.compile(".*The artifact xml-apis:xml-apis:jar:2.0.2 has been relocated to xml-apis:xml-apis:jar:1.0.b2.*");

    public final Pattern[] errs;

    WhitelistLogLines(Pattern[] errs) {
        this.errs = errs;
    }

    public final Pattern[] platformErrs() {
        switch (OS.current()) {
            case MAC:
                return new Pattern[]{
                        XML_APIS_RELOCATED,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                };
            case WINDOWS:
                return new Pattern[]{
                        XML_APIS_RELOCATED,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                        // Randomly prints some SLF4J traces. Reported by https://github.com/quarkusio/quarkus/issues/16896
                        Pattern.compile(".*SLF4J:.*"),
                };
            case LINUX:
                return new Pattern[]{
                        XML_APIS_RELOCATED,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                };
        }
        return new Pattern[]{};
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
