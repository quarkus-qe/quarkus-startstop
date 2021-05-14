package io.quarkus.ts.startstop.utils;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

public class HttpUtils {
    public static String getHttpResponse(String url) {
        return getHttpResponse(url, 1, TimeUnit.MINUTES);
    }

    public static String getHttpResponse(String url, long timeout, TimeUnit timeUnit) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url must not be empty");
        }
        AtomicReference<String> resp = new AtomicReference<>();
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(timeout, timeUnit).until(() -> {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                        // the default Accept header used by HttpURLConnection is not compatible with RESTEasy negotiation
                        // as it uses q=.8
                        conn.setRequestProperty("Accept", "text/html, *; q=0.2, */*; q=0.2");
                        resp.set(IOUtils.toString(getResponseStream(conn), StandardCharsets.UTF_8));
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                });
        return resp.get();
    }

    private static InputStream getResponseStream(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() >= HttpStatus.SC_BAD_REQUEST) {
            return connection.getErrorStream();
        }
        return connection.getInputStream();
    }
}
