package com.example.quarkus.serialization;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.quarkus.serialization.dto.ComplexDto;
import com.example.quarkus.serialization.dto.MyEnum;
import com.example.quarkus.serialization.dto.NestedClass;
import com.example.quarkus.serialization.dto.NestedInterface;

/**
 * Tests DTO serialization as this is most likely done by majority of apps these days.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("serialization/json")
public class JsonResource {

    @GET
    @Path("complex-dto")
    public ComplexDto complexDto() {
        return new ComplexDto(MyEnum.ONE, new NestedClass(), NestedInterface.INSTANCE);
    }

}
