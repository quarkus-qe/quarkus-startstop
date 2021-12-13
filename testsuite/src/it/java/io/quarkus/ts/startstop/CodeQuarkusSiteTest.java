package io.quarkus.ts.startstop;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import io.quarkus.ts.startstop.utils.Commands;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for checking presence of element on webpage
 */
@Tag("codequarkus")
public class CodeQuarkusSiteTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusSiteTest.class.getName());

    public static final String pageLoadedSelector = ".extension-category";
    public static final String webPageUrl = Commands.getCodeQuarkusURL("https://code.quarkus.redhat.com/");
    public static final String elementTitleByText = "Quarkus - Start coding with code.quarkus.redhat.com";
    public static final String elementIconByXpath = "//link[@rel=\"shortcut icon\"][@href=\"https://www.redhat.com/misc/favicon.ico\"]";
    public static final String elementRedHatLogoByXpath= "//img[@class=\"logo\"][@alt=\"Red Hat Logo\"]";
    public static final String elementSupportedFlagByXpath = "//div[@class=\"extension-tag supported dropdown-toggle\"]";
    public static final String elementQuarkusPlatformVersionByXpath = "normalize-space(//div[@class=\"quarkus-stream final\"]/@title)";

    private WebClient webClient;

    @BeforeEach
    public void init(){
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
    }

    @AfterEach
    public void close(){
        webClient.close();
    }


    @Test
    public void validatePresenceOfIcon(TestInfo testInfo) throws Exception {
        final HtmlPage page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementIconByXpath);
        HtmlElement shortcutIcon = page.getFirstByXPath(elementIconByXpath);
        assertNotNull(shortcutIcon, "Element: " + elementIconByXpath + " is missing!");
    }

    @Test
    public void validatePresenceOfTitle(TestInfo testInfo) throws Exception {
        final HtmlPage page = loadPage(webPageUrl, 60);
        LOGGER.info("Verify page title is: " + elementTitleByText);
        assertEquals(page.getTitleText(), elementTitleByText,
                "Title doesn't match. Found on the web: " + page.getTitleText() + ". Expected: " + elementTitleByText);
    }

    @Test
    public void validatePresenceOfRedHatLogo(TestInfo testInfo) throws Exception {
        final HtmlPage page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementRedHatLogoByXpath);
        HtmlImage redHatLogo = page.getFirstByXPath(elementRedHatLogoByXpath);
        assertNotNull(redHatLogo, "Element: " + elementRedHatLogoByXpath + " is missing!");
    }

    @Test
    public void validatePresenceOfSupportedFlags(TestInfo testInfo) throws Exception {
        final HtmlPage page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementSupportedFlagByXpath);
        List<HtmlElement> supportedExtensions = page.getByXPath(elementSupportedFlagByXpath);
        assertTrue(supportedExtensions.size() > 1, "Element: " + elementSupportedFlagByXpath + " is missing!");
    }

    @Test
    public void validateQuarkusVersionMatch(TestInfo testInfo) throws Exception{
        String quarkusPlatformVersion = "";
        if(System.getProperty("maven.repo.local") == null){
            LOGGER.warn("System property 'maven.repo.local' is not specified. Skip test execution.");
            return;
        }
        Path quarkusProductBomPath = Paths.get(System.getProperty("maven.repo.local")).resolve("com/redhat/quarkus/platform/quarkus-bom");
        try (Stream<Path> paths = Files.walk(quarkusProductBomPath)) {
            List<Path> folders = paths.filter(Files::isDirectory).collect(Collectors.toList());
            quarkusPlatformVersion = folders.get(1).getFileName().toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        final HtmlPage page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementQuarkusPlatformVersionByXpath);
        String quarkusPlatformVersionFromWeb = page.getFirstByXPath(elementQuarkusPlatformVersionByXpath);

        assertNotNull(quarkusPlatformVersionFromWeb, "Element: " + elementQuarkusPlatformVersionByXpath + " is missing!");
        assertTrue(quarkusPlatformVersionFromWeb.contains(quarkusPlatformVersion),
                "Quarkus versions doesn't match. Found on the web: " + quarkusPlatformVersionFromWeb + ". Expected: " + quarkusPlatformVersion);
    }


    public HtmlPage loadPage(String url, int timeoutSeconds) throws InterruptedException {
        HtmlPage page = null;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutSeconds * 1000;
        LOGGER.info("Loading web page " + url);
        try {
            page = webClient.getPage(url);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load the page: " + webPageUrl);
        }
        JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();
        while (manager.getJobCount() > 0 && System.currentTimeMillis() < endTime) {
            Thread.sleep(1000);
        }

        return page;
    }
}