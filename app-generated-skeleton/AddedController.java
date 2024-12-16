package org.my.group;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello-added")
@Singleton
public class AddedController {
    @GET
    public String sayHello() {
        return "Hello added";
    }
}
