package io.quarkus.ts.startstop.redhat;

import io.quarkus.ts.startstop.ArtifactGeneratorTest;
import org.junit.jupiter.api.Tag;

@Tag("product")
public class ArtifactGeneratorRedHatTest extends ArtifactGeneratorTest {

    @Override
    protected String getOfferingString() {
        return "redhat";
    }
}
