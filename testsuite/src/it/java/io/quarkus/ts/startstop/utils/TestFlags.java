package io.quarkus.ts.startstop.utils;

/**
 * Some flags to drive the tests flow
 */
public enum TestFlags {
    WARM_UP("This run is just a warm up for Dev mode."),
    RESTEASY_REACTIVE("Using RESTEasy Reactive extensions"),
    QUARKUS_BOM("platformArtifactId will use quarkus-bom"),
    PRODUCT_BOM("platformArtifactId will use quarkus-bom from Product");
    public final String label;

    TestFlags(String label) {
        this.label = label;
    }
}
