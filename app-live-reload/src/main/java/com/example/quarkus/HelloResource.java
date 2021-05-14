package com.example.quarkus;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HelloResource {

    static final UUID uuid;

    static {
        uuid = UUID.randomUUID();
    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Path("uuid")
    @Produces(MediaType.TEXT_PLAIN)
    public String uuid() {
        return uuid.toString();
    }

    @GET
    @Path("disable")
    public void disable() {
        System.setProperty("quarkus.live-reload.instrumentation", "false");
    }

    @GET
    @Path("enable")
    public void enable() {
        System.setProperty("quarkus.live-reload.instrumentation", "true");
    }
}
