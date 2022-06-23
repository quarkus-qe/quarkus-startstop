package io.quarkus.ts.startstop;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ElementState;
import io.quarkus.ts.startstop.utils.Commands;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for checking presence of element on webpage
 */
@Tag("codequarkus")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CodeQuarkusSiteTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusSiteTest.class.getName());

    public static final String pageLoadedSelector = ".extension-category";
    public static final String webPageUrl = Commands.getCodeQuarkusURL("https://code.quarkus.redhat.com/");
    public static final String elementTitleByText = "Quarkus - Start coding with code.quarkus.redhat.com";
    public static final String elementIconByXpath = "//link[@rel=\"shortcut icon\"][@href=\"https://www.redhat.com/misc/favicon.ico\"]";
    public static final String elementJavaVersionSelectByXpath = "//select[@id=\"javaversion\"]";
    public static final String elementRedHatLogoByXpath= "//img[@class=\"logo\"][@alt=\"Red Hat Logo\"]";
    public static final String elementStreamPickerByXpath= "//div[@class=\"stream-picker dropdown\"]";
    public static final String elementStreamItemsByXpath= "//div[@class=\"dropdown-item\"]";
    public static final String elementSupportedFlagByXpath = "//div[@class=\"extension-tag redhat-support:supported dropdown-toggle\"]";
    public static final String elementQuarkusPlatformVersionByXpath = "//div[@class=\"quarkus-stream final\"]";

    private BrowserContext browserContext; // Operates in incognito mode

    @BeforeAll
    public void init(){
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setArgs(List.of("--headless", "--disable-gpu", "--no-sandbox")));
        browserContext = browser.newContext();
        LOGGER.info("Incognito browser session has been created");
    }

    @AfterAll
    public void close(){
        browserContext.close();
    }


    @Test
    public void validatePresenceOfIcon(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementIconByXpath);
        Locator icon = page.locator(elementIconByXpath);
        assertEquals(1, icon.count(), "Element: " + elementIconByXpath + " is missing!");
    }

    @Test
    public void validatePresenceOfTitle(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Verify page title is: " + elementTitleByText);
        assertEquals(elementTitleByText, page.title(),
                "Title doesn't match. Found on the web: " + page.title() + ". Expected: " + elementTitleByText);
    }

    @Test
    public void validatePresenceOfRedHatLogo(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementRedHatLogoByXpath);
        Locator redHatLogo = page.locator(elementRedHatLogoByXpath);
        redHatLogo.elementHandle().waitForElementState(ElementState.VISIBLE);
        assertTrue(redHatLogo.isVisible(), "Element: " + elementRedHatLogoByXpath + " is missing or not visible!");
    }

    @Test
    public void validatePresenceOfSupportedFlags(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementSupportedFlagByXpath);
        Locator supportedExtensions = page.locator(elementSupportedFlagByXpath);
        assertTrue(supportedExtensions.count() > 1, "Element: " + elementSupportedFlagByXpath + " is missing!");
    }

    @Test
    public void validatePresenceOfJavaVersionSelect(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementJavaVersionSelectByXpath);
        Locator javaVersionSelect = page.locator(elementJavaVersionSelectByXpath);
        assertTrue(javaVersionSelect.count() == 1, "Element: " + elementJavaVersionSelectByXpath + " is missing!");

        String javaVersionText = javaVersionSelect.textContent();
        assertTrue(javaVersionText.contains("11"), "Java 11 is missing in java version select! javaVersionText: " + javaVersionText);
        assertTrue(javaVersionText.contains("17"), "Java 17 is missing in java version select! javaVersionText: " + javaVersionText);
    }

    @Test
    public void validatePresenceOfStreamVersionSelect(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementStreamPickerByXpath);
        Locator streamPicker = page.locator(elementStreamPickerByXpath);
        assertTrue(streamPicker.isVisible(), "Element: " + streamPicker + " is missing!");

        LOGGER.info("Trying to find elements: " + elementStreamItemsByXpath);
        streamPicker.click();
        Locator streamItems = page.locator(elementStreamItemsByXpath);
        assertTrue(streamItems.count() > 0, "No stream is defined");
//        assertTrue(streamItems.count() > 1, "Just one stream is defined, streamItems count: " + streamItems.count() + "\n" +
//                "Please make sure the 2 months period for 2 supported streams applies before filing issue for the product. \n" +
//                "Product Update and Support Policy: https://access.redhat.com/support/policy/updates/jboss_notes#p_quarkus");
    }

    @Test
    public void validateQuarkusVersionMatch(TestInfo testInfo) {
        String quarkusPlatformVersion = "";
        if(System.getProperty("maven.repo.local") == null){
            LOGGER.warn("System property 'maven.repo.local' is not specified. Skip test execution.");
            return;
        }
        Path quarkusProductBomPath = Paths.get(System.getProperty("maven.repo.local")).resolve("com/redhat/quarkus/platform/quarkus-bom");
        try (Stream<Path> paths = Files.list(quarkusProductBomPath)) {
            List<String> folders = paths.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
            quarkusPlatformVersion = folders.get(folders.size() - 1); // get the last directory from the sorted list
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementQuarkusPlatformVersionByXpath);
        String quarkusPlatformVersionFromWeb = page.locator(elementQuarkusPlatformVersionByXpath).elementHandle().getAttribute("title");

        assertNotNull(quarkusPlatformVersionFromWeb, "Element: " + elementQuarkusPlatformVersionByXpath + " is missing!");
        assertTrue(quarkusPlatformVersionFromWeb.contains(quarkusPlatformVersion),
                "Quarkus versions doesn't match. Found on the web: " + quarkusPlatformVersionFromWeb + ". Expected: " + quarkusPlatformVersion);
    }


    public Page loadPage(String url, int timeoutSeconds) {
        LOGGER.info("Loading web page " + url);
        Page page = browserContext.newPage(); // this will create new tab inside browser
        page.setDefaultTimeout(timeoutSeconds * 1000);
        page.navigate(url);
        page.waitForSelector(pageLoadedSelector);
        return page;
    }
}