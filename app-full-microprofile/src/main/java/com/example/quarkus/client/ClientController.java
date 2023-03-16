package com.example.quarkus.client;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/client")
@ApplicationScoped
public class ClientController {

    @Inject
    @RestClient
    // https://quarkus.io/guides/cdi-reference#private-members
    // - private Service service;
    Service service;

    @GET
    @Path("/test/{parameter}")
    public String onClientSide(@PathParam("parameter") String parameter) {
        return service.doSomething(parameter);
    }
}
