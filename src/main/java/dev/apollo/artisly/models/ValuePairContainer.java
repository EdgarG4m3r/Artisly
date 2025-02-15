package dev.apollo.artisly.models;

import java.util.UUID;

public class ValuePairContainer {
    private UUID id;
    private double value;

    public ValuePairContainer(UUID id, double value) {
        this.id = id;
        this.value = value;
    }

    public UUID id() {
        return id;
    }

    public double value() {
        return value;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void addValue(double value) {
        this.value += value;
    }
}
