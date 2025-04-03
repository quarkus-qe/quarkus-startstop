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
            "io.github.crac",
            // jna and jna-platform is dependency of io.quarkus:quarkus-jdbc-mariadb
            "net.java.dev.jna.jna",
            "net.java.dev.jna.jna-platform",
            // TODO: whitelisted for 3.20.0, remove next line when https://issues.redhat.com/browse/QUARKUS-5937 is fixed
            "org.latencyutils.LatencyUtils-2.0.3"
    });

    public final String[] jarNames;

    WhitelistProductBomJars(String[] jarNames) {
        this.jarNames = jarNames;
    }
}
