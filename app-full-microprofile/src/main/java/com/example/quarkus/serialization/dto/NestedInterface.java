package com.example.quarkus.serialization.dto;

public interface NestedInterface {

    NestedInterface INSTANCE = new NestedInterface() {
        @Override
        public String getString() {
            return "Vive la résistance!";
        }

        @Override
        public int getInt() {
            return 42;
        }

        @Override
        public NestedRecord getRecord() {
            return new NestedRecord();
        }

        @Override
        public char getCharacter() {
            return Character.MAX_VALUE;
        }
    };

    String getString();

    int getInt();

    Object getRecord();

    char getCharacter();

}
