package org.karagozber.callallocationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AllocateCallRequest(@NotBlank String callId, @NotBlank String region) {
}