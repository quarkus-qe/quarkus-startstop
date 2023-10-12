package io.quarkus.ts.startstop.utils;

import static io.quarkus.ts.startstop.StartStopTest.BASE_DIR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Commands {
    private static final Logger LOGGER = Logger.getLogger(Commands.class.getName());

    public static final boolean isThisWindows = System.getProperty("os.name").matches(".*[Ww]indows.*");
    public static final boolean isThisLinux = System.getProperty("os.name").matches(".*[Ll]inux.*");
    private static final Pattern numPattern = Pattern.compile("[ \t]*[0-9]+[ \t]*");
    private static final Pattern quarkusVersionPattern = Pattern.compile("[ \t]*<quarkus.version>([^<]*)</quarkus.version>.*");
    private static final Pattern trailingSlash = Pattern.compile("/+$");
    private static final String QUARKUS_PLATFORM_GROUP_ID = "com.redhat.quarkus.platform";
    private static final String QUARKUS_UPSTREAM_GROUP_ID = "io.quarkus.platform";
    private static final String QUARKUS_CORE_GROUP_ID = "io.quarkus";
    private static final String REDHAT_VERSION_TAG = "-redhat-";
    private static final String QUARKUS_MAIN_VERSION = "999-SNAPSHOT";

    public static String mvnw() {
        return Commands.isThisWindows ? "mvnw.cmd" : "./mvnw";
    }

    public static String getArtifactGeneBaseDir() {
        for (String p : new String[]{"ARTIFACT_GENERATOR_WORKSPACE", "artifact.generator.workspace"}) {
            String env = System.getenv().get(p);
            if (StringUtils.isNotBlank(env)) {
                return env;
            }
            String sys = System.getProperty(p);
            if (StringUtils.isNotBlank(sys)) {
                return sys;
            }
        }
        return System.getProperty("java.io.tmpdir");
    }

    public static String getLocalMavenRepoDir() {
        for (String p : new String[]{"TESTS_MAVEN_REPO_LOCAL", "tests.maven.repo.local"}) {
            String env = System.getenv().get(p);
            if (StringUtils.isNotBlank(env)) {
                return env;
            }
            String sys = System.getProperty(p);
            if (StringUtils.isNotBlank(sys)) {
                return sys;
            }
        }
        return System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
    }

    /**
     * Get system properties starting with `quarkus.native` prefix, for example quarkus.native.builder-image
     * @return List of `-Dquarkus.native.xyz=foo` strings
     */
    public static List<String> getQuarkusNativeProperties() {
        List<String> quarkusNativeProperties = System.getProperties().entrySet().stream()
                .filter(e -> e.getKey().toString().contains("quarkus.native"))
                .map(e -> "-D" + e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        return quarkusNativeProperties;
    }

    public static String getQuarkusPlatformVersion() {
        for (String p : new String[]{"QUARKUS_PLATFORM_VERSION", "quarkus.platform.version"}) {
            String env = System.getenv().get(p);
            if (StringUtils.isNotBlank(env)) {
                return env;
            }
            String sys = System.getProperty(p);
            if (StringUtils.isNotBlank(sys)) {
                return sys;
            }
        }
        LOGGER.warn("Failed to detect quarkus.platform.version/QUARKUS_PLATFORM_VERSION, defaulting to getQuarkusVersion().");
        return getQuarkusVersion();
    }

    public static String getQuarkusVersion() {
        for (String p : new String[]{"QUARKUS_VERSION", "quarkus.version"}) {
            String env = System.getenv().get(p);
            if (StringUtils.isNotBlank(env)) {
                return env;
            }
            String sys = System.getProperty(p);
            if (StringUtils.isNotBlank(sys)) {
                return sys;
            }
        }
        String failure = "Failed to determine quarkus.version. Check pom.xm, check env and sys vars QUARKUS_VERSION";
        try (Scanner sc = new Scanner(new File(BASE_DIR + File.separator + "pom.xml"), StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Matcher m = quarkusVersionPattern.matcher(line);
                if (m.matches()) {
                    return m.group(1);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(failure);
        }
        throw new IllegalArgumentException(failure);
    }

    public static String getQuarkusGroupId() {
        if (getQuarkusVersion().contains(REDHAT_VERSION_TAG)) {
            return QUARKUS_PLATFORM_GROUP_ID;
        } else if (getQuarkusVersion().contains(QUARKUS_MAIN_VERSION)) {
            return QUARKUS_CORE_GROUP_ID;
        }

        return QUARKUS_UPSTREAM_GROUP_ID;
    }

    public static String getBaseDir() {
        String baseDir = System.getenv().get("basedir");
        if ( baseDir == null ) {
            baseDir = System.getProperty("basedir");
        }
        if (baseDir == null) {
            // IDE PWD env variable
            baseDir = System.getenv().get("PWD");
        }

        if (StringUtils.isBlank(baseDir)) {
            throw new IllegalArgumentException("Unable to determine project.basedir.");
        }
        return new File(baseDir).getParent();
    }

    public static String getCodeQuarkusURL() {
        return getCodeQuarkusURL("https://code.quarkus.io");
    }

    public static String getCodeQuarkusURL(String fallbackURL) {
        String url = null;
        for (String p : new String[]{"CODE_QUARKUS_URL", "code.quarkus.url"}) {
            String env = System.getenv().get(p);
            if (StringUtils.isNotBlank(env)) {
                url = env;
                break;
            }
            String sys = System.getProperty(p);
            if (StringUtils.isNotBlank(sys)) {
                url = sys;
                break;
            }
        }
        if (url == null) {
            url = fallbackURL;
            LOGGER.warn("Failed to detect code.quarkus.url/CODE_QUARKUS_URL env/sys props, defaulting to " + url);
            return url;
        }
        Matcher m = trailingSlash.matcher(url);
        if (m.find()) {
            url = m.replaceAll("");
        }
        return url;
    }

    public static void cleanTarget(Apps app) {
        String target = BASE_DIR + File.separator + app.dir + File.separator + "target";
        String logs = BASE_DIR + File.separator + app.dir + File.separator + "logs";
        cleanDirOrFile(target, logs);
    }

    public static void cleanDirOrFile(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    LOGGER.warn("Failed to delete path " + path, e);
                }
            } else {
                LOGGER.warn("Path " + path + " doesn't exist");
            }
        }
    }

    public static List<String> getRunCommand(String[] baseCommand) {
        List<String> runCmd = new ArrayList<>();
        if (isThisWindows) {
            runCmd.add("cmd");
            runCmd.add("/C");
        }
        runCmd.addAll(Arrays.asList(baseCommand));

        return Collections.unmodifiableList(runCmd);
    }

    public static List<String> getBuildCommand(String[] baseCommand) {
        List<String> buildCmd = new ArrayList<>();
        if (isThisWindows) {
            buildCmd.add("cmd");
            buildCmd.add("/C");
        }
        buildCmd.addAll(Arrays.asList(baseCommand));
        buildCmd.add("-Dmaven.repo.local=" + getLocalMavenRepoDir());

        return Collections.unmodifiableList(buildCmd);
    }

    public static List<String> getBuildCommand(String[] baseCommand, String repoDir) {
        List<String> buildCmd = new ArrayList<>();
        if (isThisWindows) {
            buildCmd.add("cmd");
            buildCmd.add("/C");
        }
        buildCmd.addAll(Arrays.asList(baseCommand));
        buildCmd.add("-Dmaven.repo.local=" + repoDir);
        buildCmd.add("--settings=" + BASE_DIR + File.separator + Apps.GENERATED_SKELETON.dir + File.separator + "settings.xml");

        return Collections.unmodifiableList(buildCmd);
    }

    public static List<String> getGeneratorCommand(Set<TestFlags> flags, String[] baseCommand, String[] extensions, String repoDir) {
        List<String> generatorCmd = new ArrayList<>();
        if (isThisWindows) {
            generatorCmd.add("cmd");
            generatorCmd.add("/C");
        }
        generatorCmd.addAll(Arrays.asList(baseCommand));
        if (flags.contains(TestFlags.PRODUCT_BOM)) {
            generatorCmd.add("-DplatformArtifactId=quarkus-bom");
            generatorCmd.add("-DplatformGroupId=com.redhat.quarkus.platform");
            generatorCmd.add("-DplatformVersion=" + getQuarkusPlatformVersion());
        } else if (flags.contains(TestFlags.QUARKUS_BOM)) {
            generatorCmd.add("-DplatformArtifactId=quarkus-bom");
            generatorCmd.add("-DplatformVersion=" + getQuarkusVersion());
            if (getQuarkusVersion().equals("999-SNAPSHOT")) {
                generatorCmd.add("-DplatformGroupId=io.quarkus");
            }
        }
        generatorCmd.add("-Dextensions=" + String.join(",", extensions));
        generatorCmd.add("-Dmaven.repo.local=" + repoDir);
        generatorCmd.add("--settings=" + BASE_DIR + File.separator + Apps.GENERATED_SKELETON.dir + File.separator + "settings.xml");

        return Collections.unmodifiableList(generatorCmd);
    }

    public static List<String> getGeneratorCommand(String[] baseCommand, String[] extensions) {
        List<String> generatorCmd = new ArrayList<>();
        if (isThisWindows) {
            generatorCmd.add("cmd");
            generatorCmd.add("/C");
        }
        generatorCmd.addAll(Arrays.asList(baseCommand));
        if (getQuarkusVersion().equals("999-SNAPSHOT")) {
            generatorCmd.add("-DplatformGroupId=io.quarkus");
        }
        generatorCmd.add("-DplatformVersion=" + getQuarkusVersion());
        generatorCmd.add("-Dextensions=" + String.join(",", extensions));
        generatorCmd.add("-Dmaven.repo.local=" + getLocalMavenRepoDir());

        return Collections.unmodifiableList(generatorCmd);
    }

    /**
     * Download a zip file with an example project
     *
     * @param extensions         collection of extension codes, @See {@link io.quarkus.ts.startstop.utils.CodeQuarkusExtensions}
     * @param destinationZipFile path where the zip file will be written
     * @return the actual URL used for audit and logging purposes
     * @throws IOException
     */
    public static String download(Collection<CodeQuarkusExtensions> extensions, String destinationZipFile) throws IOException {
        String downloadURL = getCodeQuarkusURL() + "/api/download?" +
                extensions.stream().map(x -> "e=" + x.id).collect(Collectors.joining("&"));
        return download(downloadURL, destinationZipFile);
    }

    // possible URL length concern when many extensions are selected, you have two options:
    // 1. Use the POST method /api/download when the call is made from code to get the zip directly
    // 2. Use the POST method /api/project when the url is needed to be used in a browser, it will return the path to use to download the project.
    //     An error 400 is returned if the number of extensions is not sensible. The '/api/project' is optimizing the returned url to make
    //     it as short as possible.

    /**
     * Download a zip file with an example project
     *
     * @param extensions         collection of extension codes, @See {@link io.quarkus.ts.startstop.utils.CodeQuarkusExtensions}
     * @param destinationZipFile path where the zip file will be written
     * @param javaVersion        Java version used in generated project (11 or 17)
     * @return the actual URL used for audit and logging purposes
     * @throws IOException
     */
    public static String download(Collection<CodeQuarkusExtensions> extensions, String destinationZipFile, int javaVersion) throws IOException {
        String downloadURL = getCodeQuarkusURL() + "/api/download?" +
                extensions.stream().map(x -> "e=" + x.id).collect(Collectors.joining("&")) +
                "&j=" + javaVersion;
        return download(downloadURL, destinationZipFile);
    }

    private static String download(String downloadURL, String destinationZipFile) throws IOException {
        disableSslVerification();
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(
                URI.create(downloadURL).toURL().openStream());
                FileChannel fileChannel = new FileOutputStream(destinationZipFile).getChannel()) {
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
        return downloadURL;
    }

    public static File unzip(String zipFilePath, String destinationDir) throws InterruptedException, IOException {
        ProcessBuilder pb;
        if (isThisWindows) {
            pb = new ProcessBuilder("powershell", "-c", "Expand-Archive", "-Path", zipFilePath, "-DestinationPath", destinationDir, "-Force");
        } else {
            pb = new ProcessBuilder("unzip", "-o", zipFilePath, "-d", destinationDir);
        }
        Map<String, String> env = pb.environment();
        env.put("PATH", System.getenv("PATH"));
        pb.directory(new File(destinationDir));
        pb.redirectErrorStream(true);
        File unzipLog = new File(zipFilePath + ".log");
        unzipLog.delete();
        pb.redirectOutput(ProcessBuilder.Redirect.to(unzipLog));
        Process p = pb.start();
        p.waitFor(3, TimeUnit.MINUTES);
        return unzipLog;
    }

    public static void removeRepositoriesAndPluginRepositories(String pomFilePath) throws Exception {
        File pomFile = new File(pomFilePath);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile);
        NodeList repositories = doc.getElementsByTagName("repositories");
        if (repositories.getLength() == 1) {
            Node node = repositories.item(0);
            node.getParentNode().removeChild(node);
        }
        NodeList pluginRepositories = doc.getElementsByTagName("pluginRepositories");
        if (pluginRepositories.getLength() == 1) {
            Node node = pluginRepositories.item(0);
            node.getParentNode().removeChild(node);
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(pomFile));
    }

    public static boolean waitForTcpClosed(String host, int port, long loopTimeoutS) throws InterruptedException, UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        long now = System.currentTimeMillis();
        long startTime = now;
        InetSocketAddress socketAddr = new InetSocketAddress(address, port);
        while (now - startTime < 1000 * loopTimeoutS) {
            try (Socket socket = new Socket()) {
                // If it let's you write something there, it is still ready.
                socket.connect(socketAddr, 1000);
                socket.setSendBufferSize(1);
                socket.getOutputStream().write(1);
                socket.shutdownInput();
                socket.shutdownOutput();
                LOGGER.info("Socket still available: " + host + ":" + port);
            } catch (IOException e) {
                // Exception thrown - socket is likely closed.
                return true;
            }
            Thread.sleep(1000);
            now = System.currentTimeMillis();
        }
        return false;
    }

    // TODO we should get rid of it once Quarkus progresses with walid config per extension in generated examples
    public static void confAppPropsForSkeleton(String appDir) throws IOException {
        // Config, see app-generated-skeleton/README.md
        final String appRelativePath =
                "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties";
        copyFileForSkeleton("application.properties", Paths.get(appDir + File.separator + appRelativePath));
    }

    public static void copyFileForSkeleton(String skeletonFileRelativePath, Path destPath)
            throws IOException {
        final String srcPath = BASE_DIR + File.separator + Apps.GENERATED_SKELETON.dir + File.separator + skeletonFileRelativePath;
        Files.copy(Paths.get(srcPath), destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void adjustPrettyPrintForJsonLogging(String appDir) throws IOException {
        Path appProps = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties");
        Path appYaml = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.yml");

        adjustFileContent(appProps, "quarkus.log.console.json.pretty-print=true", "quarkus.log.console.json.pretty-print=false");
        adjustFileContent(appYaml, "pretty-print: true", "pretty-print: false");
    }

    private static void adjustFileContent(Path path, String regex, String replacement) throws IOException {
        if (Files.exists(path)) {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            content = content.replaceAll(regex, replacement);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void disableDevServices(String appDir) throws IOException {
        Path appProps = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties");

        String disabledDevServices = "quarkus.devservices.enabled=false\n";

        if (Files.exists(appProps)) {
            String content = new String(Files.readAllBytes(appProps), StandardCharsets.UTF_8);
            content = content + "\n" + disabledDevServices;
            Files.write(appProps, content.getBytes(StandardCharsets.UTF_8));
        } else {
            Files.write(appProps, disabledDevServices.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void dropEntityAnnotations(String appDir) throws IOException {
        Path srcMainJava = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java");

        Files.walk(srcMainJava)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        adjustFileContent(file, "@Entity", "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static int parsePort(String url) {
        return Integer.parseInt(url.split(":")[2].split("/")[0]);
    }

    public static Process runCommand(List<String> command, File directory, File logFile) {
        ProcessBuilder pa = new ProcessBuilder(command);
        Map<String, String> envA = pa.environment();
        envA.put("PATH", System.getenv("PATH"));
        pa.directory(directory);
        pa.redirectErrorStream(true);
        pa.redirectOutput(ProcessBuilder.Redirect.to(logFile));
        Process pA = null;
        try {
            pA = pa.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pA;
    }

    public static void pidKiller(long pid, boolean force) {
        try {
            if (isThisWindows) {
                if (!force) {
                    Process p = Runtime.getRuntime().exec(new String[]{
                            BASE_DIR + File.separator + "testsuite" + File.separator + "src" + File.separator + "it" + File.separator + "resources" + File.separator +
                            "CtrlC.exe ", Long.toString(pid)});
                    p.waitFor(1, TimeUnit.MINUTES);
                }
                Runtime.getRuntime().exec(new String[]{"cmd", "/C", "taskkill", "/PID", Long.toString(pid), "/F", "/T"});
            } else {
                Runtime.getRuntime().exec(new String[]{"kill", force ? "-9" : "-15", Long.toString(pid)});
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static long getRSSkB(long pid) throws IOException, InterruptedException {
        ProcessBuilder pa;
        if (isThisWindows) {
            // Note that PeakWorkingSetSize might be better, but we would need to change it on Linux too...
            // https://docs.microsoft.com/en-us/windows/win32/cimwin32prov/win32-process
            pa = new ProcessBuilder("wmic", "process", "where", "processid=" + pid, "get", "WorkingSetSize");
        } else {
            pa = new ProcessBuilder("ps", "-p", Long.toString(pid), "-o", "rss=");
        }
        Map<String, String> envA = pa.environment();
        envA.put("PATH", System.getenv("PATH"));
        pa.redirectErrorStream(true);
        Process p = pa.start();
        try (BufferedReader processOutputReader =
                new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String l;
            while ((l = processOutputReader.readLine()) != null) {
                if (numPattern.matcher(l).matches()) {
                    if (isThisWindows) {
                        // Qualifiers: DisplayName ("Working Set Size"), Units ("bytes")
                        return Long.parseLong(l.trim()) / 1024L;
                    } else {
                        return Long.parseLong(l.trim());
                    }
                }
            }
            p.waitFor();
        }
        return -1L;
    }

    public static long getOpenedFDs(long pid) throws IOException, InterruptedException {
        ProcessBuilder pa;
        long count = 0;
        if (isThisWindows) {
            pa = new ProcessBuilder("wmic", "process", "where", "processid=" + pid, "get", "HandleCount");
        } else {
            pa = new ProcessBuilder("lsof", "-F0n", "-p", Long.toString(pid));
        }
        Map<String, String> envA = pa.environment();
        envA.put("PATH", System.getenv("PATH"));
        pa.redirectErrorStream(true);
        Process p = pa.start();
        try (BufferedReader processOutputReader =
                new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            if (isThisWindows) {
                String l;
                // TODO: We just get a magical number with all FDs... Is it O.K.?
                while ((l = processOutputReader.readLine()) != null) {
                    if (numPattern.matcher(l).matches()) {
                        return Long.parseLong(l.trim());
                    }
                }
            } else {
                // TODO: For the time being we count apples and oranges; we might want to distinguish .so and .jar ?
                while (processOutputReader.readLine() != null) {
                    count++;
                }
            }
            p.waitFor();
        }
        return count;
    }

    public static void dropCaches() throws IOException, UnsupportedOperationException, InterruptedException {
        if (isThisLinux) {
            // sync && sudo sh -c "echo 3 > /proc/sys/vm/drop_caches"
            ProcessBuilder syncProcess = new ProcessBuilder("sync");
            ProcessBuilder dropProcess = new ProcessBuilder("sudo", "-n", "sh", "-c", "echo 3 > /proc/sys/vm/drop_caches");

            Process proc = syncProcess.redirectErrorStream(true).start();
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                throw new UnsupportedOperationException("Execution of 'sync' command failed.");
            }

            proc = dropProcess.redirectErrorStream(true).start();
            exitCode = proc.waitFor();
            if (exitCode != 0) {
                throw new UnsupportedOperationException("Execution of 'sudo sh -c \"echo 3 > /proc/sys/vm/drop_caches\"' command " +
                        "failed with exit code " + exitCode + ". Is sudo enabled for the current user?");
            }
        } else {
            throw new UnsupportedOperationException("Caches can be dropped only on Linux machines");
        }
    }

    /*
    TODO: CPU cycles used

    Pros: good data
    Cons: dependency on perf tool; will not translate to Windows data

    karm@local:~/workspaceRH/fooBar$ perf stat java -jar target/fooBar-1.0.0-SNAPSHOT-runner.jar
    2020-02-25 16:07:00,870 INFO  [io.quarkus] (main) fooBar 1.0.0-SNAPSHOT (running on Quarkus 999-SNAPSHOT) started in 0.776s.
    2020-02-25 16:07:00,873 INFO  [io.quarkus] (main) Profile prod activated.
    2020-02-25 16:07:00,873 INFO  [io.quarkus] (main) Installed features: [amazon-lambda, cdi, resteasy]
    2020-02-25 16:07:03,360 INFO  [io.quarkus] (main) fooBar stopped in 0.018s

    Performance counter stats for 'java -jar target/fooBar-1.0.0-SNAPSHOT-runner.jar':

       1688.910052      task-clock:u (msec)       #    0.486 CPUs utilized
                 0      context-switches:u        #    0.000 K/sec
                 0      cpu-migrations:u          #    0.000 K/sec
            12,865      page-faults:u             #    0.008 M/sec
     4,274,799,448      cycles:u                  #    2.531 GHz
     4,325,761,598      instructions:u            #    1.01  insn per cycle
       919,713,769      branches:u                #  544.561 M/sec
        29,310,015      branch-misses:u           #    3.19% of all branches

       3.473028811 seconds time elapsed
     */

    public static void processStopper(Process p, boolean force) throws InterruptedException, IOException {
        p.children().forEach(child -> {
            if (child.supportsNormalTermination()) {
                child.destroy();
            }
            pidKiller(child.pid(), force);
        });
        if (p.supportsNormalTermination()) {
            p.destroy();
            p.waitFor(3, TimeUnit.MINUTES);
        }
        pidKiller(p.pid(), force);
    }

    public static boolean disableCleanup(){

        if (System.getProperty("disableCleanup") != null) {
            return true;
        }

        return false;
    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static class ProcessRunner implements Runnable {
        final File directory;
        final File log;
        final List<String> command;
        final long timeoutMinutes;

        public ProcessRunner(File directory, File log, List<String> command, long timeoutMinutes) {
            this.directory = directory;
            this.log = log;
            this.command = command;
            this.timeoutMinutes = timeoutMinutes;
        }

        @Override
        public void run() {
            LOGGER.debugv("Running {0} in {1}", command, directory);
            ProcessBuilder pb = new ProcessBuilder(command);
            Map<String, String> env = pb.environment();
            env.put("PATH", System.getenv("PATH"));
            pb.directory(directory);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.to(log));
            Process p = null;
            try {
                p = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Objects.requireNonNull(p).waitFor(timeoutMinutes, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
