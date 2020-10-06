package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.WebpageTester;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests for checking presence of element on webpage
 *
 * @author Shpak Kyrylo <kshpak@redhat.com>
 */
@Tag("codequarkus")
public class CodeQuarkusSiteTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusSiteTest.class.getName());

    public static final String webPageUrl = "https://preview-code-quarkus-redhat-int-build.apps.app-sre-stage-0.k3s7.p1.openshiftapps.com/";
    public static final String elementIcon = "<link rel=\"shortcut icon\" type=\"image/png\" href=\"https://www.redhat.com/misc/favicon.ico\">";
    public static final String elementTitle = "<title>Quarkus - Start coding with code.quarkus.redhat.com</title>";

    public void testRuntime(TestInfo testInfo, String searchElement) throws Exception {

        try {
            long timeoutS = 1 * 60;
            LOGGER.info("Timeout: " + timeoutS + "s. Waiting for the web to load...");
            long totalTimeMillis = WebpageTester.testWeb(webPageUrl, timeoutS, searchElement, true);
            LOGGER.info("Element has been found! Total time: " + totalTimeMillis);
            assertTrue(timeoutS * 1000 > totalTimeMillis);
        } finally {
            LOGGER.info("Test is finished!");
        }
    }


    @Test
    public void validatePresenceOfIcon(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, elementIcon);
    }

    @Test
    public void validatePresenceOfTitle(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, elementTitle);
    }
}