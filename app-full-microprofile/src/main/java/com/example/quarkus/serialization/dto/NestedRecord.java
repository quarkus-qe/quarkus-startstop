package com.example.quarkus.serialization.dto;

public record NestedRecord(String recordProperty) {

    public NestedRecord() {
        this("record-property-value");
    }

}
