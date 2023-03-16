package com.example.quarkus.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/client/service")
public class ServiceController {

    @GET
    @Path("/{parameter}")
    public String doSomething(@PathParam("parameter") String parameter) {
        return String.format("Processed parameter value '%s'", parameter);
    }
}
