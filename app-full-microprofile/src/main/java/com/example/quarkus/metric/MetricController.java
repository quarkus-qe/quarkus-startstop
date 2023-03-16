package com.example.quarkus.metric;

import java.util.Random;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/metric")
@ApplicationScoped //Required for @Gauge
public class MetricController {

    @Inject
    @Metric(name = "endpoint_counter")

    // https://quarkus.io/guides/cdi-reference#private-members
    // - private Counter counter;
    Counter counter;

    @Path("timed")
    @Timed(name = "timed-request")
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
        counter.inc();
        return counter.getCount();
    }

    @Gauge(name = "counter_gauge", unit = MetricUnits.NONE)
    long getCustomerCount() {
        return counter.getCount();
    }
}
