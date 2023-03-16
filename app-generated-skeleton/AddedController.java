package org.my.group;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/hello-added")
@Singleton
public class AddedController {
    @GET
    public String sayHello() {
        return "Hello added";
    }
}
