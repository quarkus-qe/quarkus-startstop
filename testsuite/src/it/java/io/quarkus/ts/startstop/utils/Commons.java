package io.quarkus.ts.startstop.utils;

import java.util.regex.Pattern;

public class Commons {
    static final Pattern CLOSED_STREAM = Pattern.compile(".*Stream is closed, ignoring and trying to continue.*"); // https://github.com/quarkusio/quarkus/pull/28810
    static final Pattern STREAM_ERROR = Pattern.compile(".*java.lang.RuntimeException: Error reading stream.*"); // https://github.com/quarkusio/quarkus/issues/28799 (should be removed once 2.14.0.Final is out)
    static Pattern NETTY_HANDLERS = Pattern.compile(".*Could not register io.netty.handler.codec.*"); //https://github.com/quarkusio/quarkus/issues/30508
}
