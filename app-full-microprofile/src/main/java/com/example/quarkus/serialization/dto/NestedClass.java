package com.example.quarkus.serialization.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NestedClass {

    public String getProperty() {
        return "property-value";
    }

    @JsonIgnore
    public String getIgnoredProperty() {
        return "ignored-value";
    }
}
