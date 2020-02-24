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
public enum Apps {
    JAX_RS_MINIMAL("app-jax-rs-minimal", URLContent.JAX_RS_MINIMAL, Whitelist.JAX_RS_MINIMAL),
    FULL_MICROPROFILE("app-full-microprofile", URLContent.FULL_MICROPROFILE, Whitelist.FULL_MICROPROFILE);

    public final String dir;
    public final URLContent urlContent;
    public final Whitelist whitelist;

    Apps(String dir, URLContent urlContent, Whitelist whitelist) {
        this.dir = dir;
        this.urlContent = urlContent;
        this.whitelist = whitelist;
    }
}
