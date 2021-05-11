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

import java.util.stream.Stream;

import static io.quarkus.ts.startstop.utils.Commands.getQuarkusNativeProperties;
import static io.quarkus.ts.startstop.utils.Commands.getLocalMavenRepoDir;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusVersion;

/**
 * Maven commands.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum MvnCmds {
    JVM(new String[][]{
            new String[]{"mvn", "clean", "compile", "quarkus:build", "-Dquarkus.package.output-name=quarkus"},
            new String[]{"java", "-jar", "target/quarkus-app/quarkus-run.jar"}
    }),
    DEV(new String[][]{
            new String[]{"mvn", "clean", "quarkus:dev", "-Dmaven.repo.local=" + getLocalMavenRepoDir()}
    }),
    NATIVE(new String[][]{
            Stream.concat(Stream.of("mvn", "clean", "compile", "package", "-Pnative"),
                    getQuarkusNativeProperties().stream()).toArray(String[]::new),
            new String[]{Commands.isThisWindows ? "target\\quarkus-runner" : "./target/quarkus-runner"}
    }),
    GENERATOR(new String[][]{
            new String[]{
                    "mvn",
                    "io.quarkus:quarkus-maven-plugin:" + getQuarkusVersion() + ":create",
                    "-DprojectGroupId=my-groupId",
                    "-DprojectArtifactId=" + Apps.GENERATED_SKELETON.dir,
                    "-DprojectVersion=1.0.0-SNAPSHOT",
                    "-DpackageName=org.my.group"
            }
    }),
    MVNW_DEV(new String[][]{
            new String[]{Commands.mvnw(), "quarkus:dev"}
    }),
    MVNW_JVM(new String[][]{
        new String[]{Commands.mvnw(), "clean", "compile", "quarkus:build", "-Dquarkus.package.output-name=quarkus"},
        new String[]{"java", "-jar", "target/quarkus-app/quarkus-run.jar"}
    }),
    MVNW_NATIVE(new String[][]{
        Stream.concat(Stream.of(Commands.mvnw(), "clean", "compile", "package", "-Pnative", "-Dquarkus.package.output-name=quarkus"),
                getQuarkusNativeProperties().stream()).toArray(String[]::new),
        new String[]{Commands.isThisWindows ? "target\\quarkus-runner" : "./target/quarkus-runner"}
    });

    public final String[][] mvnCmds;

    MvnCmds(String[][] mvnCmds) {
        this.mvnCmds = mvnCmds;
    }
}
