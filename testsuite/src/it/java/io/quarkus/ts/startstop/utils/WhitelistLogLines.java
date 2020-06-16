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

import java.util.regex.Pattern;

/**
 * Whitelists errors in log files.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum WhitelistLogLines {
    JAX_RS_MINIMAL(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core/src/com/oracle/svm/core/jdk/VarHandleFeature.java#L199
            Pattern.compile(".*GR-10238.*")
    }),
    FULL_MICROPROFILE(new Pattern[]{
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // Needs fixing in the demo app?
            Pattern.compile(".*TestSecureController.java.*"),
            // Well, the RestClient demo probably should do some cleanup before shutdown...?
            Pattern.compile(".*Closing a class org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient.*"),
            // https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core/src/com/oracle/svm/core/jdk/VarHandleFeature.java#L199
            Pattern.compile(".*GR-10238.*")
    }),
    GENERATED_SKELETON(new Pattern[]{
            // It so happens that the dummy skeleton tries to find Mongo. This is expected.
            // See app-generated-skeleton/README.md for explanation of the scope.
            Pattern.compile(".*The remote computer refused the network connection.*"),
            // Harmless warning
            Pattern.compile(".*The Agroal dependency is present but no JDBC datasources have been defined.*"),
            // Due to our not exactly accurate application.properties, these expected warnings occur...
            Pattern.compile(".*Unrecognized configuration key[ \\\\\"]*(" +
                    "quarkus.oidc.auth-server-url|" +
                    "quarkus.oidc.client-id|" +
                    "quarkus.smallrye-jwt.enabled|" +
                    "quarkus.jaeger.service-name|" +
                    "quarkus.jaeger.sampler-param|" +
                    "quarkus.jaeger.endpoint|" +
                    "quarkus.jaeger.sampler-type" +
                    ")[ \\\\\"]*was provided.*"),
            // Hmm, weird, right? Deprecations should be fixed
            Pattern.compile(".*`io.vertx.reactivex.core.Vertx` is deprecated.*"),
            // Some artifacts names...
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // When GraalVM 19.3.1 is used; unrelated to the test
            Pattern.compile(".*forcing TieredStopAtLevel to full optimization because JVMCI is enabled.*"),
            Pattern.compile(".*error_prone_annotations.*"),
            Pattern.compile(".*Unable to properly register the hierarchy of the following classes for reflection as they are not in the Jandex index.*"),
    });

    public final Pattern[] errs;

    WhitelistLogLines(Pattern[] errs) {
        this.errs = errs;
    }
}
