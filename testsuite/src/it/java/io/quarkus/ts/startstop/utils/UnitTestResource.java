package io.quarkus.ts.startstop.utils;

import java.io.Closeable;
import java.util.function.Supplier;

public interface UnitTestResource extends Closeable {

    Supplier<UnitTestResource> NOOP_SUPPLIER = () -> new UnitTestResource() {

        @Override
        public void close() {

        }

        @Override
        public void reset() {

        }

    };

    /**
     * Resets resources so that next test can start with a clean sheet.
     */
    void reset();

}
