package me.diegomcha.autoparte.api.ocr;

import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.integration.ocr.OcrClient;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OcrControllerTest extends BaseControllerTest {

    private static final byte[] PNG_IMAGE = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
    );

    @MockitoBean
    private OcrClient ocrClient;

    @Test
    void mrzImageIsConvertedToPartialPersonData() throws Exception {
        var image = new MockMultipartFile("file", "document.png", MediaType.IMAGE_PNG_VALUE, PNG_IMAGE);
        var mrzData = new MrzDto.MrzData(
                "DOE JOHN",
                "JANE",
                "ESP",
                "ESP",
                LocalDate.of(1990, 1, 2),
                LocalDate.of(2030, 1, 2),
                MrzDto.MrzData.Gender.FEMALE,
                "ID",
                "ABC123",
                "12345678Z",
                null,
                null,
                null,
                null,
                null
        );
        Mockito.when(ocrClient.convertImageToMrz(Mockito.any()))
                .thenReturn(new MrzDto(true, "raw-mrz", List.of(), List.of(), mrzData));

        mockMvc.perform(multipart("/api/ocr/mrz")
                        .file(image)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.name").value("JANE"))
                .andExpect(jsonPath("$.personalInfo.firstSurname").value("DOE"))
                .andExpect(jsonPath("$.personalInfo.secondSurname").value("JOHN"))
                .andExpect(jsonPath("$.personalInfo.nationality").value("ESP"))
                .andExpect(jsonPath("$.personalInfo.gender").value("FEMALE"))
                .andExpect(jsonPath("$.document.type").value("NIF"))
                .andExpect(jsonPath("$.document.number").value("12345678Z"))
                .andExpect(jsonPath("$.document.supportNumber").value("ABC123"));

        Mockito.verify(ocrClient).convertImageToMrz(Mockito.any());
    }

    @Test
    void ocrClientFailuresAreTranslated() throws Exception {
        var image = new MockMultipartFile("file", "document.png", MediaType.IMAGE_PNG_VALUE, PNG_IMAGE);

        Mockito.when(ocrClient.convertImageToMrz(Mockito.any()))
                .thenThrow(new ResourceUnprocessableException("Unable to process the provided image"));

        mockMvc.perform(multipart("/api/ocr/mrz")
                        .file(image)
                        .with(csrf()))
                .andExpect(status().isUnprocessableContent());

        Mockito.reset(ocrClient);
        Mockito.when(ocrClient.convertImageToMrz(Mockito.any()))
                .thenThrow(new ServiceUnavailableException("MRZ service is unavailable"));

        mockMvc.perform(multipart("/api/ocr/mrz")
                        .file(image)
                        .with(csrf()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void mrzEndpointRequiresMultipartFileAndCsrf() throws Exception {
        mockMvc.perform(multipart("/api/ocr/mrz")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(multipart("/api/ocr/mrz")
                        .file(new MockMultipartFile("file", "document.png", MediaType.IMAGE_PNG_VALUE, PNG_IMAGE)))
                .andExpect(status().isForbidden());

        Mockito.verifyNoInteractions(ocrClient);
    }
}
