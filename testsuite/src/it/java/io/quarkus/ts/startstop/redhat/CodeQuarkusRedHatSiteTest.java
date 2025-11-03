package io.quarkus.ts.startstop.redhat;

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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.quarkus.ts.startstop.utils.Commands.getQuarkusVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for checking presence of element on webpage
 */
@Tag("codequarkus")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CodeQuarkusRedHatSiteTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusRedHatSiteTest.class.getName());

    public static final String pageLoadedSelector = ".project-extensions";
    public static final String pageWithExtensionLoadedSelector = ".extension-picker-list";
    public static final String webPageUrl = Commands.getCodeQuarkusURL("https://code.quarkus.redhat.com/");
    public static final String elementTitleByText = "Quarkus - Start coding with code.quarkus.redhat.com";
    public static final String elementIconByXpath = "//link[@rel=\"shortcut icon\"][@href=\"https://www.redhat.com/favicon.ico\"]";
    public static final String elementJavaVersionSelectByXpath = "//select[@id=\"javaversion\"]";
    public static final String elementRedHatLogoByXpath = "//img[@class=\"logo\"][@alt=\"Red Hat Logo\"]";
    public static final String elementStreamPickerByXpath = "//div[@class=\"stream-picker dropdown\"]";
    public static final String elementStreamItemsByXpath = "//div[@class=\"dropdown-item\"]";
    public static final String elementSupportedFlagByXpath = "//div[@class=\"extension-tag support-full-support dropdown-toggle\"]";
    public static final String elementQuarkusPlatformVersionByXpath = "//div[contains(@class, 'quarkus-stream')]";
    public static final String elementExtensionByXpath = "//div[@class=\"extension-row\" and @aria-label=\"%s\"]";
    public static final String elementSupportFilterXpath = "//div[@class='filter-combo-button dropdown-toggle' and @aria-label=\"Toggle support combobox\"]";
    public static final String elementIntroductionModalWindowXpath = "//button[@aria-label=\"Close the introduction modal\"]";
    public static final String elementExpandExtensionXpath = "//button[@class=\"button-show-more btn-light btn btn-primary\"]";

    public static final String QUARKUS_REST_EXTENSION = "io.quarkus:quarkus-rest";
    public static final String QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION = "io.quarkiverse.langchain4j:quarkus-langchain4j-openai";
    public static final String QUARKUS_REST_LINKS_EXTENSION = "io.quarkus:quarkus-rest-links";

    private BrowserContext browserContext; // Operates in incognito mode

    @BeforeAll
    public void init(){
        Playwright playwright = Playwright.create();
        Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true));
        Browser.NewContextOptions options = new Browser.NewContextOptions();
        options.ignoreHTTPSErrors = true;
        browserContext = browser.newContext(options);
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
        Page page = loadPage(webPageUrl + "/?e=grpc", 60);
        LOGGER.info("Trying to find element: " + elementSupportedFlagByXpath);
        Locator supportedExtensions = page.locator(elementSupportedFlagByXpath);
        assertTrue(supportedExtensions.count() >= 1, "Element: " + elementSupportedFlagByXpath + " is missing!");
    }

    @Test
    public void validatePresenceOfJavaVersionSelect(TestInfo testInfo) {
        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementJavaVersionSelectByXpath);
        Locator javaVersionSelect = page.locator(elementJavaVersionSelectByXpath);
        assertTrue(javaVersionSelect.count() == 1, "Element: " + elementJavaVersionSelectByXpath + " is missing!");

        String javaVersionText = javaVersionSelect.textContent();
        assertTrue(javaVersionText.contains("21"), "Java 21 is missing in java version select! javaVersionText: " + javaVersionText);
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
        assertTrue(streamItems.count() > 1, "Two (or more) streams are expected to be defined defined, streamItems count: " + streamItems.count() + "\n" +
                "Product Update and Support Policy: https://access.redhat.com/support/policy/updates/jboss_notes#p_quarkus");
    }

    @Test
    public void validateQuarkusVersionMatch(TestInfo testInfo) {
        String quarkusPlatformVersion = getQuarkusVersion();
        Assumptions.assumeTrue(quarkusPlatformVersion.contains("redhat"));

        Page page = loadPage(webPageUrl, 60);
        LOGGER.info("Trying to find element: " + elementQuarkusPlatformVersionByXpath);
        String quarkusPlatformVersionFromWeb = page.locator(elementQuarkusPlatformVersionByXpath).elementHandle().getAttribute("title");

        assertNotNull(quarkusPlatformVersionFromWeb, "Element: " + elementQuarkusPlatformVersionByXpath + " is missing!");
        assertTrue(quarkusPlatformVersionFromWeb.contains(quarkusPlatformVersion),
                "Quarkus versions doesn't match. Found on the web: " + quarkusPlatformVersionFromWeb + ". Expected: " + quarkusPlatformVersion);
    }

    @Test
    public void validateQuarkusSearchForAllSupportedExtensions(TestInfo testInfo) {
        Page page = loadPageWithExtensions(webPageUrl + "?extension-search=support:*", 60);

        showAllExtension(page);

        // Check if the supported extension are visible
        LOGGER.info("Trying to find element: " + elementExtensionByXpath.formatted(QUARKUS_REST_EXTENSION));
        assertTrue(page.locator(elementExtensionByXpath.formatted(QUARKUS_REST_EXTENSION)).isVisible(),
                "The extension " + QUARKUS_REST_EXTENSION + " should be visible as it's supported.");

        // Check that the unsupported extension is not visible
        LOGGER.info("Trying to find element: " + elementExtensionByXpath.formatted(QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION));
        assertFalse(page.locator(elementExtensionByXpath.formatted(QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION)).isVisible(),
                "The extension " + QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION + " should not be visible as it isn't supported.");
        LOGGER.info("Trying to find element: " + elementExtensionByXpath.formatted(QUARKUS_REST_LINKS_EXTENSION));
        assertFalse(page.locator(elementExtensionByXpath.formatted(QUARKUS_REST_LINKS_EXTENSION)).isVisible(),
                "The extension " + QUARKUS_REST_LINKS_EXTENSION + " should not be visible as it isn't supported.");
    }

    @Test
    public void validateQuarkusSearchForAllNotSupportedExtensions(TestInfo testInfo) {
        Page page = loadPageWithExtensions(webPageUrl + "?extension-search=!support", 60);

        showAllExtension(page);
        // Check if the supported extension are not visible
        LOGGER.info("Trying to find element: " + elementExtensionByXpath.formatted(QUARKUS_REST_EXTENSION));
        assertFalse(page.locator(elementExtensionByXpath.formatted(QUARKUS_REST_EXTENSION)).isVisible(),
                "The extension " + QUARKUS_REST_EXTENSION + " should not be visible as it's supported.");

        // Check that the unsupported extension is visible
        LOGGER.info("Trying to find element: " + elementExtensionByXpath.formatted(QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION));
        assertTrue(page.locator(elementExtensionByXpath.formatted(QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION)).isVisible(),
                "The extension " + QUARKUS_LANGCHAIN4J_OPENAI_EXTENSION + " should be visible as it isn't supported.");
        LOGGER.info("Trying to find element: " + elementExtensionByXpath.formatted(QUARKUS_REST_LINKS_EXTENSION));
        assertTrue(page.locator(elementExtensionByXpath.formatted(QUARKUS_REST_LINKS_EXTENSION)).isVisible(),
                "The extension " + QUARKUS_REST_LINKS_EXTENSION + " should be visible as it isn't supported.");
    }

    @Test
    public void checkAllSupportOptionArePresent(TestInfo testInfo) {
        String supportButtonXpath = "//button[@aria-label=\"%s\"]";
        String supportDivXpath = "//div[@aria-label=\"Add support:%s filter\"]";
        // TODO add `dev-preview` when the code.quarkus is updated
        String[] supportScopes = {"full-support", "tech-preview", "dev-support", "supported-in-jvm", "deprecated"};

        Page page = loadPage(webPageUrl, 60);
        // The support menu is not visible without clicking at it
        closeIntroductionModalWindow(page);
        page.locator(elementSupportFilterXpath).click();

        assertTrue(page.locator(supportButtonXpath.formatted("Add has support filter")).isVisible(),
                "The option to show all supported extensions should be present under the support filter.");
        assertTrue(page.locator(supportButtonXpath.formatted("Add no support filter")).isVisible(),
                "The option to show all unsupported extensions should be present under the support filter.");

        for (String supportScope : supportScopes) {
            assertTrue(page.locator(supportDivXpath.formatted(supportScope)).isVisible(),
                    "The support scope " + supportScope + "should be present under the support filter.");
        }
    }

    public void closeIntroductionModalWindow(Page page) {
        page.locator(elementIntroductionModalWindowXpath).click();
    }

    public void showAllExtension(Page page){
        // It's needed to show all extension, otherwise all filtered list of extension is not visible
        closeIntroductionModalWindow(page);
        page.locator(elementExpandExtensionXpath).click();
    }

    public Page loadPage(String url, int timeoutSeconds) {
        return loadPageSettingWaitSelector(url, timeoutSeconds, pageLoadedSelector);
    }

    public Page loadPageWithExtensions(String url, int timeoutSeconds) {
        return loadPageSettingWaitSelector(url, timeoutSeconds, pageWithExtensionLoadedSelector);
    }

    public Page loadPageSettingWaitSelector(String url, int timeoutSeconds, String selector) {
        LOGGER.info("Loading web page " + url);
        Page page = browserContext.newPage(); // this will create new tab inside browser
        page.setDefaultTimeout(timeoutSeconds * 1000);
        page.navigate(url);
        page.waitForSelector(selector);
        return page;
    }
}
