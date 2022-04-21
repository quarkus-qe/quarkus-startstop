package io.quarkus.ts.startstop.utils;

/**
 * Whitelists jar names.
 *
 * There are basically known issues. The enum should be empty.
 */
public enum WhitelistProductBomJars {
    PRODUCT_BOM(new String[]{
            "org.checkerframework.checker-qual",
            "com.microsoft.sqlserver.mssql-jdbc",
            "com.microsoft.azure.adal4j",
            "com.nimbusds.oauth2-oidc-sdk",
            "com.nimbusds.content-type",
            "io.quarkus.quarkus-resteasy-reactive-links", // TODO https://issues.redhat.com/browse/QUARKUS-2033
    });

    public final String[] jarNames;

    WhitelistProductBomJars(String[] jarNames) {
        this.jarNames = jarNames;
    }
}
