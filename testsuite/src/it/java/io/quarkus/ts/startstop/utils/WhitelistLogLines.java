package io.quarkus.ts.startstop.utils;

import java.util.regex.Pattern;

/**
 * Whitelists errors in log files.
 */
public enum WhitelistLogLines {
    JAKARTA_REST_MINIMAL(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // https://github.com/quarkusio/quarkus/pull/28810
            Pattern.compile(".*Stream is closed, ignoring and trying to continue.*"),
            // To be able to run on Quarkus < 3.2 which does not support analytics
            Pattern.compile(".*Unrecognized configuration key \"quarkus.analytics.disabled\" was provided.*"),
            // arquillian-bom has wrong sha1 and md5sum, discussed with jfang who uploaded it, there is nothing he can do about it
            Pattern.compile(".*Checksum validation failed, expected 2811ba27a71a8bda0602161ffe2f6e1429da8068 but is 36257165a0945753efb3f9d473d86c6f4c6c6f6e.*"),
            Pattern.compile(".*Could not validate integrity of download from https://repo.maven.apache.org/maven2/org/jboss/arquillian/arquillian-bom/1.7.0.Final/arquillian-bom-1.7.0.Final.pom.*"),
            Pattern.compile(".*org.eclipse.aether.util.concurrency.RunnableErrorForwarder.*"),
            // https://github.com/quarkusio/quarkus/issues/34626
            Pattern.compile("\\[Quarkus build analytics\\] Analytics remote config not received."),
            // netty 4 which doesn't have the relevant native config in the lib. See https://github.com/netty/netty/pull/13596
            Pattern.compile(".*Warning: Please re-evaluate whether any experimental option is required, and either remove or unlock it\\..*"),
            Pattern.compile(".*Warning: The option '-H:ReflectionConfigurationResources=META-INF/native-image/io\\.netty/netty-transport/reflection-config\\.json' is experimental and must be enabled via.*"),
    }),
    FULL_MICROPROFILE(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // Well, the RestClient demo probably should do some cleanup before shutdown...?
            Pattern.compile(".*Closing a class org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient.*"),
            // https://github.com/quarkusio/quarkus/pull/28810
            Pattern.compile(".*Stream is closed, ignoring and trying to continue.*"),
            // GH Action runners are slow, graceful shutdown is not guaranteed on Quarkus
            // RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you.
            Pattern.compile(".*RESTEASY004687: Closing a class.*CleanupAction.*"),
            // https://github.com/quarkusio/quarkus/issues/34626
            Pattern.compile("\\[Quarkus build analytics\\] Analytics remote config not received."),
            // netty 4 which doesn't have the relevant native config in the lib. See https://github.com/netty/netty/pull/13596
            Pattern.compile(".*Warning: Please re-evaluate whether any experimental option is required, and either remove or unlock it\\..*"),
            Pattern.compile(".*Warning: The option '-H:ReflectionConfigurationResources=META-INF/native-image/io\\.netty/netty-transport/reflection-config\\.json' is experimental and must be enabled via.*"),
            // TODO: remove next line when https://github.com/quarkusio/quarkus/issues/41351 gets fixed
            Pattern.compile(".*Error Occurred After Shutdown.*java.lang.NullPointerException.*Cannot invoke.*io.smallrye.context.SmallRyeContextManager.defaultThreadContext.*"),
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
                    ")[ \\\\\"]*was provided.*"),
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            Pattern.compile(".*error_prone_annotations.*"),
            Pattern.compile(".*No WebJars or mvnpm jars were found in the project.*"),
            // kubernetes-client tries to configure client from service account
            Pattern.compile(".*Error reading service account token from.*"),
            // We have disabled the Quarkus Registry Client (-DquarkusRegistryClient=false)
            Pattern.compile(".*The extension catalog will be narrowed to.*"),
            // comes with https://github.com/quarkusio/quarkus/pull/20182
            Pattern.compile(".*Hibernate ORM is disabled because no JPA entities were found.*"),
            Pattern.compile(".*Hibernate Reactive is disabled because no JPA entities were found.*"),
            // comes with https://github.com/quarkusio/quarkus/pull/19969 https://github.com/quarkusio/quarkus/pull/26868 https://github.com/quarkusio/quarkus/pull/27811
            Pattern.compile(".*OIDC Server is not available.*"),
            Pattern.compile(".*localhost:6661.*"),
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
            // https://github.com/quarkusio/quarkus/pull/28810
            Pattern.compile(".*Stream is closed, ignoring and trying to continue.*"),
            // https://github.com/quarkusio/quarkus/pull/28654
            Pattern.compile(".*Using legacy gRPC support, with separate new HTTP server instance. Switch to single HTTP server instance usage with quarkus.grpc.server.use-separate-server=false property.*"),
            // Full stack trace switch enabled for dev mode command
            Pattern.compile(".*Error stacktraces are turned on.*"),
            // https://github.com/quarkusio/quarkus/issues/33825
            Pattern.compile(".*Detected bean archive with bean discovery mode of 'all', this is not portable in CDI Lite and is treated as 'annotated' in Quarkus! Path to beans.xml: io.smallrye:smallrye-graphql-cdi.*"),
            // arquillian-bom has wrong sha1 and md5sum, discussed with jfang who uploaded it, there is nothing he can do about it
            Pattern.compile(".*Checksum validation failed, expected 2811ba27a71a8bda0602161ffe2f6e1429da8068 but is 36257165a0945753efb3f9d473d86c6f4c6c6f6e.*"),
            Pattern.compile(".*Could not validate integrity of download from https://repo.maven.apache.org/maven2/org/jboss/arquillian/arquillian-bom/1.7.0.Final/arquillian-bom-1.7.0.Final.pom.*"),
            Pattern.compile(".*org.eclipse.aether.util.concurrency.RunnableErrorForwarder.*"),
            // To be able to run on Quarkus < 3.2 which does not support analytics
            Pattern.compile(".*Unrecognized configuration key \"quarkus.analytics.disabled\" was provided.*"),
            // https://github.com/quarkusio/quarkus/issues/34626
            Pattern.compile("\\[Quarkus build analytics\\] Analytics remote config not received."),
            // https://github.com/quarkusio/quarkus/issues/36775
            Pattern.compile("(?i:.*quarkus.mongodb.native.dns.*config property is deprecated.*)"),
            // https://github.com/quarkusio/quarkus/issues/37532
            Pattern.compile(".*Annotation processing is enabled because one or more processors were found.*"),
            // https://github.com/quarkusio/quarkus/issues/38711
            Pattern.compile(".*SplitPackageProcessor.*Following packages were detected in multiple archives.*"),
            // TODO: remove next line when 3.7.3 is released, see https://github.com/quarkusio/quarkus/pull/38710
            Pattern.compile(".*This instance of GraphiQLHandler has been created with a deprecated method.*")
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
            // https://github.com/quarkusio/quarkus/issues/34626
            Pattern.compile("\\[Quarkus build analytics\\] Analytics remote config not received."),
            // GH Actions runners are sometimes slow
            Pattern.compile("\\[io.ver.cor.imp.BlockedThreadChecker\\] (vertx-blocked-thread-checker) Thread.*has been blocked for.*"),
    });
    
    // Depending to the OS and also on the Quarkus extensions, the Native build might print some warnings about duplicate entries
    private static final Pattern COMMON_SLF4J_API_DEPENDENCY_TREE = Pattern.compile(".*org.slf4j:slf4j-api:jar.*");
    private static final Pattern COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE = Pattern.compile(".*org.jboss.slf4j:slf4j-jboss-logmanager:jar.*");
    private static final Pattern WARNING_MISSING_OBJCOPY_NATIVE = Pattern.compile(".*objcopy executable not found in PATH.*");
    private static final Pattern WARNING_MISSING_OBJCOPY_RESULT_NATIVE = Pattern.compile(".*That also means that resulting native executable is larger as it embeds the debug symbols..*");
    private static final Pattern XML_APIS_RELOCATED = Pattern.compile(".*The artifact xml-apis:xml-apis:jar:2.0.2 has been relocated to xml-apis:xml-apis:jar:1.0.b2.*");
    private static final Pattern WARNING_SYS_MODULE_PATH = Pattern.compile(".*\\[WARNING\\] system modules path not set in conjunction with -source \\d+");

    public final Pattern[] errs;

    WhitelistLogLines(Pattern[] errs) {
        this.errs = errs;
    }

    public final Pattern[] platformErrs() {
        switch (OS.current()) {
            case MAC:
                return new Pattern[] {
                        XML_APIS_RELOCATED,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                        // https://github.com/netty/netty/issues/11020
                        Pattern.compile(".*Can not find io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider in the classpath, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.*"),
                        WARNING_SYS_MODULE_PATH
                };
            case WINDOWS:
                return new Pattern[] {
                        XML_APIS_RELOCATED,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        WARNING_MISSING_OBJCOPY_NATIVE,
                        WARNING_MISSING_OBJCOPY_RESULT_NATIVE,
                        // Randomly prints some SLF4J traces. Reported by https://github.com/quarkusio/quarkus/issues/16896
                        Pattern.compile(".*SLF4J:.*"),
                        // TODO https://github.com/quarkusio/quarkus/issues/36053
                        Pattern.compile(".*Unknown module: org.graalvm.nativeimage.*"),
                        // TODO https://github.com/quarkusio/quarkus/issues/36813
                        Pattern.compile(".*Unrecognized configuration file .*application.yml found.*"),
                        WARNING_SYS_MODULE_PATH
                };
            case LINUX:
            	return new Pattern[] {
                        XML_APIS_RELOCATED,
                        COMMON_SLF4J_API_DEPENDENCY_TREE,
                        COMMON_SLF4J_JBOSS_LOGMANAGER_DEPENDENCY_TREE,
                        // TODO https://github.com/quarkusio/quarkus/issues/36053
                        Pattern.compile(".*Unknown module: org.graalvm.nativeimage.*"),
                        WARNING_SYS_MODULE_PATH
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
