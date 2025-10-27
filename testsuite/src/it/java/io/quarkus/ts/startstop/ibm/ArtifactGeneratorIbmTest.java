package io.quarkus.ts.startstop.ibm;

import io.quarkus.ts.startstop.ArtifactGeneratorTest;
import org.junit.jupiter.api.Tag;

@Tag("product")
public class ArtifactGeneratorIbmTest extends ArtifactGeneratorTest {

    @Override
    protected String getOfferingString() {
        return "ibm";
    }
}
