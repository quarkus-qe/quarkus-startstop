package com.example.quarkus.client;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@ApplicationScoped
public interface Service {

    @GET
    @Path("/{parameter}")
    String doSomething(@PathParam("parameter") String parameter);

}
