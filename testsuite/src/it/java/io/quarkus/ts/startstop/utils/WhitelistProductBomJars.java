package io.quarkus.ts.startstop.utils;

/**
 * Whitelists jar names.
 *
 * There are basically known issues. The enum should be empty.
 */
public enum WhitelistProductBomJars {
    PRODUCT_BOM(new String[]{
            "lib/org.checkerframework.checker-qual",
    });

    public final String[] jarNames;

    WhitelistProductBomJars(String[] jarNames) {
        this.jarNames = jarNames;
    }
}
