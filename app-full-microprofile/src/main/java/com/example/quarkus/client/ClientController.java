package com.example.quarkus.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RestClient;

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
