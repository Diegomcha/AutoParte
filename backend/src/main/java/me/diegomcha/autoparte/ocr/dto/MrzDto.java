package me.diegomcha.autoparte.ocr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MrzDto(
        boolean valid,
        String raw,
        String[] errors,
        String[] warnings,
        MrzData data
) {
    record MrzData(
            String surname,
            String name,
            String country,
            String nationality,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("expiry_date") String expiryDate,
            String sex,
            @JsonProperty("document_type") String documentType,
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("optional_data") String optionalData,
            @JsonProperty("birth_date_hash") String birthDateHash,
            @JsonProperty("expiry_date_hash") String expiryDateHash,
            @JsonProperty("document_number_hash") String documentNumberHash,
            @JsonProperty("optional_data_2") String optionalData2,
            @JsonProperty("final_hash") String finalHash
    ) {
    }
}