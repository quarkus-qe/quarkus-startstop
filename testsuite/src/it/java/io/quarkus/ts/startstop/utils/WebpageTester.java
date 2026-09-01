package io.quarkus.ts.startstop.utils;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebpageTester {
    private static final Logger LOGGER = Logger.getLogger(WebpageTester.class.getName());

    /**
     * Patiently try to wait for a web page and examine it
     *
     * @param url             address
     * @param timeout         timeout
     * @param stringToLookFor string must be present on the page
     * @param measureTime     whether to try to measure time as precisely as possible
     * @return the time it took for the {@code url} to contain the given {@code stringToLookFor}
     */
    public static long testWeb(String url, Timeout timeout, String stringToLookFor, boolean measureTime) throws InterruptedException, IOException {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url must not be empty");
        }
        if (StringUtils.isBlank(stringToLookFor)) {
            throw new IllegalArgumentException("stringToLookFor must contain a non-empty string");
        }
        String webPage = "";

        boolean found = false;
        TimeoutMeasure measure = timeout.measure();
        while (measure.hasTimeLeft()) {
            URLConnection c = URI.create(url).toURL().openConnection();
            c.setRequestProperty("Accept", "*/*");
            c.setConnectTimeout(500);
            try (InputStream in = c.getInputStream();
                 Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.toString())) {
                scanner.useDelimiter("\\A");
                webPage = scanner.hasNext() ? scanner.next() : "";
            } catch (Exception e) {
                LOGGER.debug("Waiting `" + stringToLookFor + "' to appear on " + url);
            }
            if (webPage.contains(stringToLookFor)) {
                found = true;
                break;
            }
            if (!measureTime) {
                Thread.sleep(500);
            } else {
                LockSupport.parkNanos(100000);
            }
        }

        String failureMessage = "Timeout " + timeout + " was reached. " +
                (StringUtils.isNotBlank(webPage) ? webPage + " must contain string: " : "Empty webpage does not contain string: ") +
                "`" + stringToLookFor + "'";
        if (!found) {
            LOGGER.info(failureMessage);
        }
        assertTrue(found, failureMessage);
        return measure.elapsedMillis();
    }
}
