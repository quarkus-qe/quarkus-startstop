package io.quarkus.ts.startstop.redhat;

import io.quarkus.ts.startstop.ArtifactGeneratorTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Tag("product")
public class ArtifactGeneratorRedHatTest extends ArtifactGeneratorTest {

    @Override
    protected String getOfferingString() {
        return "redhat";
    }

    @Test
    public void flowExtensions(TestInfo testInfo) throws Exception {
        smokeCheck(testInfo, SMOKE_CHECK_TYPE.COMMUNITY, flowExtensions, flowScannedDependencies);
    }
    @Test
    public void camelExtensions(TestInfo testInfo) throws Exception {
        smokeCheck(testInfo, SMOKE_CHECK_TYPE.COMMUNITY, cxfExtensions, cxfScannedDependencies);
    }
}
