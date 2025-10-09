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
            "com.github.victools.jsonschema-generator",  // https://issues.redhat.com/browse/QUARKUS-6713
            "org.apache.opennlp.opennlp-tools",          // https://issues.redhat.com/browse/QUARKUS-6715
            "com.knuddels.jtokkit"                       // https://issues.redhat.com/browse/QUARKUS-6716
    });

    public final String[] jarNames;

    WhitelistProductBomJars(String[] jarNames) {
        this.jarNames = jarNames;
    }
}
