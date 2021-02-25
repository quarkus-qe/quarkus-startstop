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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class JSONtoEnum {
    public static Pattern pattern = Pattern.compile("(?i).*\"id\":\"[^:]*:([^\"]*)\".*\"name\":\"([^\"]*)\".*\"shortId\":\"([^\"]*)\".*\"tags\":\\[([^]]*)].*");

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || !args[0].startsWith("http")) {
            System.out.println("Expects URL e.g. https://code.quarkus.stage.redhat.com/api/extensions");
            System.exit(1);
        }
        StringBuilder s = new StringBuilder();
        s.append("public enum CodeQuarkusExtensions {\n\n");
        Set<String> usedIds = new TreeSet<>();
        new Scanner(new URL(args[0]).openStream(), StandardCharsets.UTF_8).useDelimiter("},\\{").tokens().forEach(x -> {
            Matcher m = pattern.matcher(x);
            if (m.matches() && m.groupCount() == 4) {
                String id = m.group(1);
                if (!usedIds.contains(id)) {
                    String name = m.group(2);
                    String shortId = m.group(3);
                    boolean supported = m.group(4).contains("supported");
                    String label = id.replace("-", "_").toUpperCase();
                    s.append("    ");
                    s.append(label);
                    s.append("(\"");
                    s.append(id);
                    s.append("\", \"");
                    s.append(name);
                    s.append("\", \"");
                    s.append(shortId);
                    s.append("\", ");
                    s.append(supported);
                    s.append("),\n");
                    usedIds.add(id);
                }
            }
        });
        s.replace(s.length() - 2, s.length() - 1, ";");
        s.append("\n    public final String id;" +
                "\n    public final String name;" +
                "\n    public final String shortId;" +
                "\n    public final boolean supported;" +
                "\n\n" +
                "    CodeQuarkusExtensions(String id, String name, String shortId, boolean supported) {\n" +
                "        this.id = id;\n" +
                "        this.name = name;\n" +
                "        this.shortId = shortId;\n" +
                "        this.supported = supported;\n" +
                "    }\n" +
                "}\n");
        System.out.println(s.toString());
    }
}
