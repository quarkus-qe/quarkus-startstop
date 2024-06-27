package com.example.quarkus.serialization.dto;

public record ComplexDto(MyEnum myEnum, NestedClass nestedClass, NestedInterface nestedInterface) {
}
