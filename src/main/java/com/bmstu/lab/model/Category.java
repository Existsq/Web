package com.bmstu.lab.model;

public record Category(
    Long id,
    String title,
    int basePrice,
    Long imageId,
    String description,
    double coefficient,
    String shortDescription) {}
