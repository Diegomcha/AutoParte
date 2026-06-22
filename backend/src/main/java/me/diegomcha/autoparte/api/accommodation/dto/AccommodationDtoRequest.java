package me.diegomcha.autoparte.api.accommodation.dto;

import jakarta.validation.constraints.NotBlank;

public record AccommodationDtoRequest(
        @NotBlank String name,
        @NotBlank String sesCode,
        Boolean internetConnection
) {
}
