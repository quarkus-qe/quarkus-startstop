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

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.quarkus.ts.startstop.StartStopTest.BASE_DIR;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Maven commands.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum Apps {
    JAX_RS_MINIMAL("app-jax-rs-minimal", URLContent.JAX_RS_MINIMAL, Whitelist.JAX_RS_MINIMAL),
    FULL_MICROPROFILE("app-full-microprofile", URLContent.FULL_MICROPROFILE, Whitelist.FULL_MICROPROFILE),
    GENERATED_SKELETON("app-generated-skeleton", URLContent.GENERATED_SKELETON, Whitelist.GENERATED_SKELETON);

    public final String dir;
    public final URLContent urlContent;
    public final Whitelist whitelist;
    public final Map<String, Long> thresholdProperties = new HashMap<>();

    Apps(String dir, URLContent urlContent, Whitelist whitelist) {
        this.dir = dir;
        this.urlContent = urlContent;
        this.whitelist = whitelist;
        File tpFile = new File(BASE_DIR + File.separator + dir + File.separator + "threshold.properties");
        String appDirNormalized = dir.toUpperCase().replace('-', '_') + "_";
        try (InputStream input = new FileInputStream(tpFile)) {
            Properties props = new Properties();
            props.load(input);
            for (String pn : props.stringPropertyNames()) {
                String normPn = pn.toUpperCase().replace('.', '_');
                String env = System.getenv().get(appDirNormalized + normPn);
                if (StringUtils.isNotBlank(env)) {
                    props.replace(pn, env);
                }
                String sys = System.getProperty(appDirNormalized + normPn);
                if (StringUtils.isNotBlank(sys)) {
                    props.replace(pn, sys);
                }
                thresholdProperties.put(pn, Long.parseLong(props.getProperty(pn)));
            }
        } catch (NumberFormatException e) {
            fail("Check threshold.properties and Sys and Env variables (upper case, underscores instead of dots). " +
                    "All values are expected to be of type long.");
        } catch (IOException e) {
            fail("Couldn't find " + tpFile.getAbsolutePath());
        }
    }
}
