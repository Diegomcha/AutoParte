package me.diegomcha.autoparte.api.accommodation.dto;

import me.diegomcha.autoparte.core.validation.annotations.NullableNotBlank;

public record AccommodationDtoPatch(
        @NullableNotBlank String name,
        @NullableNotBlank String sesCode,
        Boolean internetConnection
) {
}
