package com.example.quarkus.secure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@Path("/secured")
@ApplicationScoped
public class TestSecureController {

    private String key;

    @PostConstruct
    public void init() {
        key = readPemFile();
    }

    @GET
    @Path("/test")
    public String testSecureCall() {
        if (key == null) {
            throw new WebApplicationException("Unable to read privateKey.pem", 500);
        }
        String jwt = generateJWT(key);
        WebTarget target = ClientBuilder.newClient().target("http://localhost:8080/data/protected");
        Response response = target.request().header("authorization", "Bearer " + jwt).buildGet().invoke();
        return String.format("Claim value within JWT of 'custom-value' : %s", response.readEntity(String.class));
    }

    private static String generateJWT(String key) {
        JWTAuth provider = JWTAuth.create(null, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setBuffer(key))); // Set up private key

        MPJWTToken token = new MPJWTToken();
        token.setAud("targetService");
        token.setIss("https://server.example.com");
        token.setJti(UUID.randomUUID().toString());
        token.setSub("Jessie");
        token.setUpn("Jessie");
        token.setIat(System.currentTimeMillis());
        token.setExp(System.currentTimeMillis() + 30000);
        token.addAdditionalClaims("custom-value", "Jessie specific value");
        token.setGroups(Arrays.asList("user", "protected"));

        return provider.generateToken(new JsonObject().mergeIn(token.toJSONString()), new JWTOptions().setAlgorithm("RS256"));
    }

    private static String readPemFile() {
        StringBuilder sb = new StringBuilder(8192);
        try (BufferedReader is = new BufferedReader(
                new InputStreamReader(
                        TestSecureController.class.getResourceAsStream("/privateKey.pem"), StandardCharsets.US_ASCII))) {
            String line;
            while ((line = is.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
