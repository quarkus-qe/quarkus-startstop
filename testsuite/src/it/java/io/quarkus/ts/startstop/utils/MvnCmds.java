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
 * Maven commands.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum MvnCmds {
    JVM(new String[][]{
            new String[]{"mvn", "clean", "compile", "quarkus:build"},
            new String[]{"java", "-jar", "target/quarkus-runner.jar"}
    }),
    //  DEV(new String[][]{
    //          new String[]{"mvn", "clean", "compile", "quarkus:dev"}
    //  }),
    NATIVE(new String[][]{
            new String[]{"mvn", "clean", "compile", "quarkus:native-image", "-Pnative"},
            new String[]{Commands.isThisWindows ? "target\\quarkus-runner" : "./target/quarkus-runner"}
    });

    public final String[][] mvnCmds;

    MvnCmds(String[][] mvnCmds) {
        this.mvnCmds = mvnCmds;
    }
}
