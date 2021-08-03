package io.quarkus.ts.startstop.utils;

/**
 * Some flags to drive the tests flow
 */
public enum TestFlags {
    WARM_UP("This run is just a warm up for Dev mode."),
    QUARKUS_BOM("platformArtifactId will use quarkus-bom"),
    PRODUCT_BOM("platformArtifactId will use quarkus-product-bom");
    public final String label;

    TestFlags(String label) {
        this.label = label;
    }
}
