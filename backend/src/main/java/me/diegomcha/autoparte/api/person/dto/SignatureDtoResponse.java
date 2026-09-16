package me.diegomcha.autoparte.api.person.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SignatureDtoResponse(
        @Nonnull Map<@NotNull Instant, @NotNull List<@NotNull Point>> paths,
        @Nonnull Instant signedAt,
        @Nonnull String ipAddress,
        @Nonnull String userAgent
        ) {
}
