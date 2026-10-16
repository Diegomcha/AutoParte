package me.diegomcha.autoparte.api.ocr;

import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import me.diegomcha.autoparte.integration.ocr.OcrClient;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class OcrServiceTest {
    @Autowired
    private OcrService service;
    @MockitoBean
    private OcrClient ocrClient;

    @Test
    void testExtractPersonInfoFromMrz() throws ResourceUnprocessableException, ServiceUnavailableException {
        var image = new MockMultipartFile("image", "document.png", "image/png", new byte[]{1, 2, 3});
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
        var mrzResponse = new MrzDto(true, "raw-mrz", List.of(), List.of(), mrzData);
        Mockito.when(ocrClient.convertImageToMrz(Mockito.any())).thenReturn(mrzResponse);

        var person = service.extractPersonInfoFromMrz(image);

        Assertions.assertEquals("JANE", person.personalInfo().name());
        Assertions.assertEquals("DOE", person.personalInfo().firstSurname());
        Assertions.assertEquals("JOHN", person.personalInfo().secondSurname());
        Assertions.assertEquals("ESP", person.personalInfo().nationality());
        Assertions.assertEquals(LocalDate.of(1990, 1, 2).atStartOfDay().toInstant(java.time.ZoneOffset.UTC), person.personalInfo().birthDate());
        Assertions.assertEquals(PersonalInfo.PersonalInfoGender.FEMALE, person.personalInfo().gender());
        Assertions.assertEquals(Document.DocumentType.NIF, person.document().type());
        Assertions.assertEquals("12345678Z", person.document().number());
        Assertions.assertEquals("ABC123", person.document().supportNumber());
        Mockito.verify(ocrClient).convertImageToMrz(Mockito.any());
    }

    @Test
    void testExtractPersonInfoFromMrzPropagatesResourceUnprocessableException()
            throws ResourceUnprocessableException, ServiceUnavailableException {
        var image = new MockMultipartFile("image", "document.png", "image/png", new byte[]{1});
        var exception = new ResourceUnprocessableException("Unable to process the provided image");
        Mockito.when(ocrClient.convertImageToMrz(Mockito.any())).thenThrow(exception);

        var thrown = Assertions.assertThrows(ResourceUnprocessableException.class, () ->
                service.extractPersonInfoFromMrz(image)
        );

        Assertions.assertSame(exception, thrown);
    }

    @Test
    void testExtractPersonInfoFromMrzPropagatesServiceUnavailableException()
            throws ResourceUnprocessableException, ServiceUnavailableException {
        var image = new MockMultipartFile("image", "document.png", "image/png", new byte[]{1});
        var exception = new ServiceUnavailableException("MRZ service is unavailable");
        Mockito.when(ocrClient.convertImageToMrz(Mockito.any())).thenThrow(exception);

        var thrown = Assertions.assertThrows(ServiceUnavailableException.class, () ->
                service.extractPersonInfoFromMrz(image)
        );

        Assertions.assertSame(exception, thrown);
    }
}
