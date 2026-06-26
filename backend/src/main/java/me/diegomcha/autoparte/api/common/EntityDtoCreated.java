package me.diegomcha.autoparte.api.common;

import java.time.Instant;
import java.util.UUID;

public record EntityDtoCreated(
        UUID id,
        Instant createdAt
) {
}
