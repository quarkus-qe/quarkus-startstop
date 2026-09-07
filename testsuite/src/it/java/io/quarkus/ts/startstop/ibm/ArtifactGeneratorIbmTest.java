package io.quarkus.ts.startstop.ibm;

import io.quarkus.ts.startstop.ArtifactGeneratorTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Tag("product")
public class ArtifactGeneratorIbmTest extends ArtifactGeneratorTest {

    @Override
    protected String getOfferingString() {
        return "ibm";
    }

    @Test
    public void flowExtensions(TestInfo testInfo) throws Exception {
        smokeCheck(testInfo, SMOKE_CHECK_TYPE.PRODUCTIZED, flowExtensions, flowScannedDependencies);
    }
    @Test
    public void camelExtensions(TestInfo testInfo) throws Exception {
        smokeCheck(testInfo, SMOKE_CHECK_TYPE.PRODUCTIZED, cxfExtensions, cxfScannedDependencies);
    }
}
