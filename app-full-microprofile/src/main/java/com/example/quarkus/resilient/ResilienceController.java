package com.example.quarkus.resilient;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/resilience")
@ApplicationScoped
public class ResilienceController {

    @Fallback(fallbackMethod = "fallback")
    @Timeout(500)
    @GET
    public String checkTimeout() {
        try {
            Thread.sleep(700L);
        } catch (InterruptedException e) {
            // Silence is golden
        }
        return "Never from normal processing";
    }

    public String fallback() {
        return "Fallback answer due to timeout";
    }
}
