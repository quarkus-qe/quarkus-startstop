package io.quarkus.ts.startstop.utils;

import java.util.regex.Pattern;

public class Commons {
    static Pattern NETTY_HANDLERS=Pattern.compile(".*Could not register io.netty.handler.codec.*"); //https://github.com/quarkusio/quarkus/issues/30508
    static Pattern NO_IMAGE = Pattern.compile(".*Tag 22.3-java17 was deleted or has expired.*"); //https://github.com/quarkusio/quarkus/issues/30738
}
