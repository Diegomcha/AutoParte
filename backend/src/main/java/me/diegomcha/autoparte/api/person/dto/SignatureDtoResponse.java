package me.diegomcha.autoparte.api.person.dto;

import jakarta.validation.constraints.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SignatureDtoResponse(
        @NotNull Map<@NotNull Instant, @NotNull List<@NotNull Point>> paths,
        @NotNull Instant signedAt,
        @NotNull String ipAddress,
        @NotNull String userAgent
        ) {
}
