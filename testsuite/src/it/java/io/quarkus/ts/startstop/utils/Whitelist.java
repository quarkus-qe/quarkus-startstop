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
 * Whitelists errors in log files.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum Whitelist {
    JAX_RS_MINIMAL(new String[]{
            // Some artifacts names...
            "maven-error-diagnostics",
            "errorprone"
    }),
    FULL_MICROPROFILE(new String[]{
            // Some artifacts names...
            "maven-error-diagnostics",
            "errorprone"
    }),
    GENERATED_SKELETON(new String[]{
            // It so happens that the dummy skeleton tries to find Mongo. This is expected.
            // See app-generated-skeleton/README.md for explanation of the scope.
            "The remote computer refused the network connection",
            // Some artifacts names...
            "maven-error-diagnostics",
            "errorprone"
    });

    public final String[] errs;

    Whitelist(String[] errs) {
        this.errs = errs;
    }
}
