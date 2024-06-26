package com.example.quarkus.serialization;

import com.example.quarkus.serialization.dto.ComplexDto;
import com.example.quarkus.serialization.dto.MyEnum;
import com.example.quarkus.serialization.dto.NestedClass;
import com.example.quarkus.serialization.dto.NestedInterface;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
