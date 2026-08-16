package me.diegomcha.autoparte.api.ocr;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.ocr.dto.PartialPersonDtoRequest;
import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.integration.ocr.OcrClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// TODO: Test this service
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class OcrService {

    private final OcrClient ocrClient;
    private final OcrMapper ocrMapper;

    /**
     * Extracts person information from an image file containing a Machine Readable Zone (MRZ).
     *
     * @param image The image file containing the MRZ to be processed
     * @return A PartialPersonDtoRequest containing the extracted person information
     * @throws ResourceUnprocessableException if the file cannot be processed
     * @throws ServiceUnavailableException    if the OCR service is unavailable or fails to process the image
     */
    public PartialPersonDtoRequest extractPersonInfoFromMrz(MultipartFile image) throws ResourceUnprocessableException, ServiceUnavailableException {
        return ocrMapper.toPartialRequest(
                ocrClient.convertImageToMrz(image.getResource()).data()
        );
    }
}
