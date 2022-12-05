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
            "org.jboss.spec.javax.resource.jboss-connector-api_1.7_spec",
            "io.perfmark.perfmark-api",
            "com.graphql-java.graphql-java",
            "com.graphql-java.java-dataloader",
            "org.apache.thrift.libthrift",
            "com.aayushatharva.brotli4j.native-osx-x86_64",
    });

    public final String[] jarNames;

    WhitelistProductBomJars(String[] jarNames) {
        this.jarNames = jarNames;
    }
}
