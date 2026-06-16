package me.diegomcha.autoparte.integration.ses.dto;

import java.util.List;
import java.util.UUID;

public record BatchDto(
        BatchDtoStatus status,
        String message,
        List<CommunicationDto> communications
) {
    public enum BatchDtoStatus {
        SUCCESS, // 1
        ERROR_FORMAT, // 2
        ERROR_UNKNOWN, // 3
        PROCESSING, // 4
        PENDING, // 5
        ERROR_COMMUNICATIONS // 6
    }

    public record CommunicationDto(
            UUID id,
            String error
    ) {
    }
}
