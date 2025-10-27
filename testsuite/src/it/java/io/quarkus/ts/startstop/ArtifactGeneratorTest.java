package io.quarkus.ts.startstop;

import static io.quarkus.ts.startstop.utils.Commands.adjustPrettyPrintForJsonLogging;
import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.confAppPropsForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.confIndexPageForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.copyFileForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.dropEntityAnnotations;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getGeneratorCommand;
import static io.quarkus.ts.startstop.utils.Commands.getOpenedFDs;
import static io.quarkus.ts.startstop.utils.Commands.getRSSkB;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.removeRepositoriesAndPluginRepositories;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.SKIP;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.checkThreshold;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.parseStartStopTimestamps;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.Commands;
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.Logs;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.TestFlags;
import io.quarkus.ts.startstop.utils.URLContent;
import io.quarkus.ts.startstop.utils.WebpageTester;

/**
 * Tests for quarkus-maven-plugin generator
 */
@Tag("generator")
public class ArtifactGeneratorTest {

    private static final Logger LOGGER = Logger.getLogger(ArtifactGeneratorTest.class.getName());

    public static final String[] supportedExtensionsSubsetSetA = new String[]{
            "agroal",
            "config-yaml",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mysql",
            "jdbc-postgresql",
            "jsonb",
            "jsonp",
            "kafka-client",
            "narayana-jta",
            "oidc",
            "opentelemetry",
            "quarkus-quartz", // "quartz" is ambiguous with org.apache.camel.quarkus:camel-quarkus-quartz
            "reactive-pg-client",
            "resteasy-client",
            "resteasy-client-jaxb",
            "resteasy",
            "resteasy-jackson",
            "resteasy-jaxb",
            "resteasy-jsonb",
            "scheduler",
            "spring-boot-properties",
            "smallrye-reactive-streams-operators",
            "spring-data-jpa",
            "spring-di",
            "spring-security",
//            "spring-web",  // spring-web-codestart uses quarkus-resteasy-reactive-jackson, see https://github.com/quarkusio/quarkus/pull/24890
            "undertow",
            "vertx",
            "quarkus-reactive-routes",
            "grpc",
            "infinispan-client",
            "cache",
            "micrometer",
            "micrometer-registry-prometheus",
            "quarkus-openshift-client",
            "quarkus-smallrye-graphql-client",
            "qpid-jms",
    };

    public static final String[] supportedExtensionsSubsetSetB = new String[]{
            "agroal",
            "quarkus-avro",
            "config-yaml",
            "container-image-openshift",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
//            "hibernate-orm-rest-data-panache",  see https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.22
            "hibernate-validator",
            "resteasy-client",
            "resteasy-client-jackson",
            "jackson",
            "jaxb",
            "jdbc-mariadb",
            "jdbc-mssql",
            "kubernetes-config",
            "mailer",
            "mutiny",
            "oidc-client",
            "resteasy-client-oidc-filter",
            "reactive-mysql-client",
            "resteasy-multipart",
            "resteasy-qute",
            "smallrye-context-propagation",
            "smallrye-fault-tolerance",
            "smallrye-health",
            "smallrye-jwt",
            "smallrye-jwt-build",
            "smallrye-openapi",
            "opentelemetry",
            "messaging",
            // https://github.com/quarkusio/quarkus/issues/23383
//            "smallrye-reactive-messaging-amqp",
            "messaging-kafka",
            "spring-data-jpa",
//            "spring-data-rest",  see https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.22
            "spring-di",
            "spring-security",
//            "spring-web",  // spring-web-codestart uses quarkus-resteasy-reactive-jackson, see https://github.com/quarkusio/quarkus/pull/24890
            "spring-cloud-config-client",
            "spring-scheduled",
            "spring-cache",
            "websockets",
            "websockets-client",
            "quarkus-smallrye-graphql",
            "quarkus-hibernate-search-orm-elasticsearch",
            "quarkus-elasticsearch-rest-client",
    };

    public static final String[] supportedReactiveExtensionsSubsetSetA = new String[]{
            "quarkus-rest-client-jaxrs",
            "quarkus-rest",
            "quarkus-rest-jackson",
            "quarkus-rest-qute",
            "quarkus-rest-client",
            "quarkus-rest-client-jackson",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mysql",
            "jdbc-postgresql",
            "jsonb",
            "jsonp",
            "kafka-client",
            "kubernetes-config",
            "mailer",
            "narayana-jta",
            "oidc",
            "opentelemetry",
            "quarkus-quartz",
            "reactive-pg-client",
            "scheduler",
            "spring-boot-properties",
            "smallrye-reactive-streams-operators",
            "spring-data-jpa",
            "spring-di",
            "spring-security",
            "spring-web",
            "undertow",
            "vertx",
            "quarkus-reactive-routes",
            "grpc",
            "infinispan-client",
            "cache",
            "micrometer",
            "micrometer-registry-prometheus",
            "quarkus-openshift-client",
            "quarkus-smallrye-graphql",
    };

    public static final String[] supportedReactiveExtensionsSubsetSetB = new String[]{
            "quarkus-rest-client-jaxrs",
            "quarkus-rest",
            "quarkus-rest-jsonb",
            "quarkus-rest-client",
            "agroal",
            "quarkus-avro",
            "config-yaml",
            "container-image-openshift",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-orm-rest-data-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mariadb",
            "jdbc-mssql",
            "mutiny",
            "oidc-client",
            "quarkus-rest-client-oidc-filter",
            "quarkus-qute",
            "reactive-mysql-client",
            "smallrye-context-propagation",
            "smallrye-fault-tolerance",
            "smallrye-health",
            "smallrye-jwt",
            "smallrye-jwt-build",
            "smallrye-openapi",
            "opentelemetry",
            "messaging",
            // https://github.com/quarkusio/quarkus/issues/23383
//            "smallrye-reactive-messaging-amqp",
            "messaging-kafka",
            "spring-data-jpa",
            "spring-data-rest",
            "spring-di",
            "spring-security",
            "spring-web",
            "spring-cloud-config-client",
            "spring-scheduled",
            "spring-cache",
            "websockets",
            "websockets-client",
            "quarkus-smallrye-graphql-client",
    };

    public static final String[] langchain4jExtensions = new String[]{
            // First, add some non-AI extensions, which are "required" by application.properties
            "quarkus-rest",
            "hibernate-orm-panache",
            "jdbc-postgresql",
            "quarkus-smallrye-graphql",
            "opentelemetry",
            // the above is explained there: https://github.com/quarkus-qe/quarkus-startstop/pull/594#discussion_r2354539902
            "langchain4j-core",
            "langchain4j-openai",
            "langchain4j-ollama",
            "langchain4j-parsers-base",
            "langchain4j-mcp",
            "mcp-server-sse",
            "mcp-server-stdio",
    };

    public static final File QUARKUS_CONFIG = Paths.get(System.getProperty("user.home"), ".quarkus", "config.yaml").toFile();
    private static final String QUARKUS_REGISTRY_ID = "testingregistry";

    public void testRuntime(TestInfo testInfo, String[] extensions, Set<TestFlags> flags) throws Exception {
        Process pA = null;
        File buildLogA = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        File appBaseDir = new File(getArtifactGeneBaseDir(), mn);
        File appDir = new File(appBaseDir, Apps.GENERATED_SKELETON.dir);
        String logsDir = appBaseDir.getAbsolutePath() + File.separator + Apps.GENERATED_SKELETON.dir + "-logs";
        List<String> generatorCmd = getGeneratorCommand(MvnCmds.GENERATOR.mvnCmds[0], extensions);
        List<String> runCmd = getRunCommand(MvnCmds.DEV.mvnCmds[0]);
        URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;
        if (flags.contains(TestFlags.WARM_UP)) {
            LOGGER.info(mn + ": Warming up setup: " + String.join(" ", generatorCmd));
        } else {
            LOGGER.info(mn + ": Testing setup: " + String.join(" ", generatorCmd));
        }
        LOGGER.info("Running inside " + appDir.getAbsolutePath());
        try {
            // Cleanup
            cleanDirOrFile(appBaseDir.getAbsolutePath());
            Files.createDirectories(Paths.get(logsDir));

            // Prepare quarkus config
            Files.createDirectories(Paths.get(appBaseDir.getAbsolutePath(), ".quarkus"));
            prepareConfig(appBaseDir);

            // Build
            buildLogA = new File(logsDir + File.separator + (flags.contains(TestFlags.WARM_UP) ? "warmup-artifact-build.log" : "artifact-build.log"));
            ExecutorService buildService = Executors.newFixedThreadPool(1);
            buildService.submit(new Commands.ProcessRunner(appBaseDir, buildLogA, generatorCmd, 20));
            appendln(whatIDidReport, "# " + cn + ", " + mn + ", warmup run: " + flags.contains(TestFlags.WARM_UP));
            appendln(whatIDidReport, (new Date()).toString());
            appendln(whatIDidReport, appBaseDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", generatorCmd));
            long buildStarts = System.currentTimeMillis();
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);
            long buildEnds = System.currentTimeMillis();

            assertTrue(buildLogA.exists());
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, buildLogA);

            // Config, see app-generated-skeleton/README.md
            confAppPropsForSkeleton(appDir.getAbsolutePath());
            confIndexPageForSkeleton(appDir.getAbsolutePath());
            adjustPrettyPrintForJsonLogging(appDir.getAbsolutePath());
            dropEntityAnnotations(appDir.getAbsolutePath());
            if (StringUtils.isBlank(System.getProperty("gh.actions"))) {
                LOGGER.info("Removing repositories and pluginRepositories from pom.xml ...");
                removeRepositoriesAndPluginRepositories(appDir + File.separator + "pom.xml");
            }

            // Run
            LOGGER.info("Running... " + runCmd);
            runLogA = new File(logsDir + File.separator + (flags.contains(TestFlags.WARM_UP) ? "warmup-dev-run.log" : "dev-run.log"));
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", runCmd));
            pA = runCommand(runCmd, appDir, runLogA);
            // Test web pages
            // The reason for a seemingly large timeout of 20 minutes is that dev mode will be downloading the Internet on the first fresh run.
            long timeoutS = (flags.contains(TestFlags.WARM_UP) ? 20 * 60 : 120);
            long timeToFirstOKRequest = WebpageTester.testWeb(skeletonApp.urlContent[0][0], timeoutS,
                    skeletonApp.urlContent[0][1], !flags.contains(TestFlags.WARM_UP));

            if (flags.contains(TestFlags.WARM_UP)) {
                LOGGER.info("Terminating warmup and scanning logs...");
                pA.getInputStream().available();
                processStopper(pA, false);
                LOGGER.info("Gonna wait for ports closed after warmup...");
                // Release ports
                assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                        "Main port is still open after warmup");
                checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, runLogA);
                return;
            }

            LOGGER.info("Testing reload...");
            // modify existing class
            Path srcFile = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
                    "org" + File.separator + "my" + File.separator + "group" + File.separator +"GreetingResource.java");
            appendlnSection(whatIDidReport, "Reloading class: " + srcFile.toAbsolutePath());
            try (Stream<String> src = Files.lines(srcFile)) {
                Files.write(srcFile, src.map(l -> l.replaceAll("Hello", "Bye")).collect(Collectors.toList()));
            }

            // test modified class and measure time
            long timeToReloadedOKRequest = WebpageTester.testWeb(
                    flags.contains(TestFlags.RESTEASY_REACTIVE)?skeletonApp.urlContent[3][0]:skeletonApp.urlContent[1][0], 60,
                    flags.contains(TestFlags.RESTEASY_REACTIVE)?skeletonApp.urlContent[3][1]:skeletonApp.urlContent[1][1], true);

            // add new class
            Path addedFile = Paths
                    .get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
                            "org" + File.separator + "my" + File.separator + "group" + File.separator + "AddedController.java");
            appendlnSection(whatIDidReport, "Adding class: " + addedFile.toAbsolutePath());
            copyFileForSkeleton("AddedController.java", addedFile);

            // test added class
            WebpageTester.testWeb(skeletonApp.urlContent[2][0], 60, skeletonApp.urlContent[2][1], false);

            LOGGER.info("Terminate and scan logs...");
            pA.getInputStream().available();

            long rssKb = getRSSkB(pA.pid());
            long openedFiles = getOpenedFDs(pA.pid());

            processStopper(pA, false);

            LOGGER.info("Gonna wait for ports closed...");
            // Release ports
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open");
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, runLogA);

            float[] startedStopped = parseStartStopTimestamps(runLogA);

            Path measurementsLog = Paths.get(getLogsDir(cn, mn).toString(), "measurements.csv");
            LogBuilder.Log log = new LogBuilder()
                    .app(Apps.GENERATED_SKELETON)
                    .mode(MvnCmds.GENERATOR)
                    .buildTimeMs(buildEnds - buildStarts)
                    .timeToFirstOKRequestMs(timeToFirstOKRequest)
                    .timeToReloadedOKRequest(timeToReloadedOKRequest)
                    .startedInMs((long) (startedStopped[0] * 1000))
                    .stoppedInMs((long) (startedStopped[1] * 1000))
                    .rssKb(rssKb)
                    .openedFiles(openedFiles)
                    .build();
            Logs.logMeasurements(log, measurementsLog);
            appendln(whatIDidReport, "Measurements:");
            appendln(whatIDidReport, log.headerMarkdown + "\n" + log.lineMarkdown);
            checkThreshold(Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, SKIP, timeToFirstOKRequest, timeToReloadedOKRequest);
        } finally {
            // Make sure processes are down even if there was an exception / failure
            if (pA != null) {
                processStopper(pA, true);
            }
            // Archive logs no matter what
            archiveLog(cn, mn, buildLogA);
            if (runLogA != null) {
                // If build failed it is actually expected to have no runtime log.
                archiveLog(cn, mn, runLogA);
            }
            writeReport(cn, mn, whatIDidReport.toString());
            cleanDirOrFile(appBaseDir.getAbsolutePath());
        }
    }

    @Test
    public void manyExtensionsSetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.of(TestFlags.WARM_UP));
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.noneOf(TestFlags.class));
    }

    @Test
    public void manyExtensionsSetB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.of(TestFlags.WARM_UP));
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.noneOf(TestFlags.class));
    }

    @Test
    public void manyReactiveExtensionsSetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetA, EnumSet.of(TestFlags.WARM_UP, TestFlags.RESTEASY_REACTIVE));
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetA, EnumSet.of(TestFlags.RESTEASY_REACTIVE));
    }

    @Test
    public void manyReactiveExtensionsSetB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetB, EnumSet.of(TestFlags.WARM_UP, TestFlags.RESTEASY_REACTIVE));
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetB, EnumSet.of(TestFlags.RESTEASY_REACTIVE));
    }

    @Test
    public void langchain4jExtensions(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, langchain4jExtensions, EnumSet.of(TestFlags.WARM_UP, TestFlags.RESTEASY_REACTIVE));
        testRuntime(testInfo, langchain4jExtensions, EnumSet.of(TestFlags.RESTEASY_REACTIVE));
    }

    protected String getOfferingString() {
        return null;
    }

    /**
     * Load data from ~/.quarkus/config.yaml and update them
     * This method logic is same as
     * https://github.com/quarkus-qe/quarkus-test-suite/blob/e37dddcbbb255997c65a48dc2340650c06e3d3d7/quarkus-cli/src/test/java/io/quarkus/ts/quarkus/cli/offering/QuarkusCliOfferingUtils.java#L83
     *
     * @param offering offering value e.g. ibm, redhat
     * @throws IOException
     */
    private void prepareConfig(File appBaseDir) throws IOException {
        if (getOfferingString() == null) {
            return;
        }
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        Map<String, Object> data;
        try (InputStream inputStream = new FileInputStream(QUARKUS_CONFIG)) {
            data = yaml.load(inputStream);
        }

        updateRegistryConfig((List<Object>) data.get("registries"), getOfferingString());

        File quarkusConfig = Paths.get(appBaseDir.getAbsolutePath(), ".quarkus", "config.yaml").toFile();

        try (Writer writer = new FileWriter(quarkusConfig)) {
            LOGGER.info("Quarkus config in use is: located at " + quarkusConfig.getAbsolutePath()
                    + " and content of config is:\n" + data);
            yaml.dump(data, writer);
        }
    }

    /**
     * Iterate over registries and add offering value to registry with name of {@link #QUARKUS_REGISTRY_ID}
     *
     * @param registries list of all set registries
     * @param offering offering value e.g. ibm, redhat
     */
    private static void updateRegistryConfig(List<Object> registries, String offering) {
        for (Object item : registries) {
            if (item instanceof Map) {
                Map<String, Object> registryMap = (Map<String, Object>) item;
                if (registryMap.containsKey(QUARKUS_REGISTRY_ID)) {
                    // Get the testing registry and set the offering
                    Map<String, Object> details = (Map<String, Object>) registryMap.get(QUARKUS_REGISTRY_ID);
                    details.put("offering", offering);
                    return;
                }
            }
        }
        Assertions.fail(QUARKUS_REGISTRY_ID + " registry is not present in quarkus config");
    }
}
