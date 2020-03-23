/*
 * Copyright (c) 2020 Contributors to the Quarkus StartStop project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.quarkus.ts.startstop.utils;

/**
 * Whitelists jar names.
 *
 * There are basically known issues. The enum should be empty.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum WhitelistProductBomJars {
    PRODUCT_BOM(new String[]{
            "lib/jakarta.",
            "lib/javax.",
            "lib/com.google.code.findbugs.jsr305",
            "lib/com.google.guava.failureaccess",
            "lib/org.eclipse.yasson",
            "lib/com.mchange.mchange-commons-java",
            "lib/org.checkerframework.checker-qual",
            "lib/io.smallrye.reactive.smallrye-axle-amqp-client"
    });

    public final String[] jarNames;

    WhitelistProductBomJars(String[] jarNames) {
        this.jarNames = jarNames;
    }
}
