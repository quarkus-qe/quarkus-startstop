package com.example.quarkus;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/data")
public class QuarkusRestApplication extends Application {
}
