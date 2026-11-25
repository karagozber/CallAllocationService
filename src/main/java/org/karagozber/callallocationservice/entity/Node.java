package org.karagozber.callallocationservice.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record Node(
    @NotBlank String id,
    @NotBlank String region,
    @Positive int capacity,
    @PositiveOrZero int currentCalls) {}
