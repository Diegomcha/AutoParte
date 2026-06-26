package me.diegomcha.autoparte.api.ocr;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.ocr.dto.PartialPersonDtoRequest;
import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.core.validation.annotations.Image;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class OcrController implements OcrAPI {

    private final OcrService ocrService;

    @PostMapping(value = "/mrz", consumes = "multipart/form-data")
    @Override
    public PartialPersonDtoRequest extractPersonInfoFromMrz(@RequestPart @Image MultipartFile file) throws ResourceUnprocessableException, ServiceUnavailableException {
        return ocrService.extractPersonInfoFromMrz(file);
    }
}
