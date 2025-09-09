package com.bmstu.lab.model;

public record Service(
    Long id,
    String title,
    double index,
    int basePrice,
    Long imageId,
    String description,
    String shortDescription) {}
