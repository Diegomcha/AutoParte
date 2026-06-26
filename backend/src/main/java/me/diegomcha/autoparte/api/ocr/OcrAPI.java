package me.diegomcha.autoparte.api.ocr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.ocr.dto.PartialPersonDtoRequest;
import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.core.validation.annotations.Image;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Optical Character Recognition", description = "Operations related to OCR")
@SuppressWarnings("unused")
interface OcrAPI {

    @Operation(summary = "Person information extraction from image through MRZ")
    PartialPersonDtoRequest extractPersonInfoFromMrz(@Image MultipartFile file) throws ResourceUnprocessableException, ServiceUnavailableException;

}
