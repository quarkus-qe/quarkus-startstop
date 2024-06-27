package com.example.quarkus.metric;

import java.util.Random;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/metric")
@ApplicationScoped //Required for @Gauge
public class MetricController {

    private final MeterRegistry registry;

    public MetricController(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("counter_gauge", this, MetricController::getCustomerCount);
    }

    @Path("timed")
    @Timed(value = "timed-request")
    @GET
    public String timedRequest() {
        // Demo, not production style
        int wait = new Random().nextInt(1000);
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            // Demo
            e.printStackTrace();
        }

        return "Request is used in statistics, check with the Metrics call.";
    }

    @Path("increment")
    @GET
    public long doIncrement() {
        getEndpointCounter().increment();
        return (long) getEndpointCounter().count();
    }

    long getCustomerCount() {
        return (long) getEndpointCounter().count();
    }

    private Counter getEndpointCounter() {
        return registry.counter("endpoint_counter");
    }
}
