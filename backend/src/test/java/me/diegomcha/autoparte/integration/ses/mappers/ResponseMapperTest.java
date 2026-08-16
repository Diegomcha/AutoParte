package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionResponse;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteResponse;
import es.mir.hospedajes.servicios.soap.tipocomunicacion.ResultadoRespuestaType;
import es.mir.hospedajes.servicios.soap.tipocomunicacion.ResultadoType;
import es.mir.hospedajes.servicios.soap.tipocomunicacion.WscomunicacionType;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

class ResponseMapperTest {

    private static final es.mir.hospedajes.servicios.soap.comunicacion.ObjectFactory COMUNICACION_FACTORY = new es.mir.hospedajes.servicios.soap.comunicacion.ObjectFactory();
    private static final es.mir.hospedajes.servicios.soap.tipocomunicacion.ObjectFactory TIPOCOMUNICACION_FACTORY = new es.mir.hospedajes.servicios.soap.tipocomunicacion.ObjectFactory();

    private static final ResultadoRespuestaType OK_RESPUESTA = getResultadoRespuestaType(0, "Ok");

    private static final Object[][] BATCH_RESPONSES = {
            {
                    getConsultaLoteResponse(OK_RESPUESTA, List.of()),
                    Map.of()
            },
            {
                    getConsultaLoteResponse(OK_RESPUESTA, List.of(
                            getWscomunicacionType("123e4567-e89b-12d3-a456-426614174000", 1, "Ok", List.of(
                                    getResultadoType(1, "123e4567-e89b-12d3-a456-426614174001", null)
                            ))
                    )),
                    Map.of(
                            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), new BatchDto(
                                    BatchDto.BatchDtoStatus.SUCCESS,
                                    "Ok",
                                    List.of(
                                            new BatchDto.CommunicationDto(1, UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), null)
                                    )
                            )
                    )
            },
            {
                    getConsultaLoteResponse(OK_RESPUESTA, List.of(
                            getWscomunicacionType("123e4567-e89b-12d3-a456-426614174000", 6, "Error 1", List.of(
                                    getResultadoType(1, "123e4567-e89b-12d3-a456-426614174001", null),
                                    getResultadoType(2, null, "Error 2")
                            )),
                            getWscomunicacionType("123e4567-e89b-12d3-a456-426614174002", 2, "Error 3", null)
                    )),
                    Map.of(
                            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), new BatchDto(
                                    BatchDto.BatchDtoStatus.ERROR_COMMUNICATIONS,
                                    "Error 1",
                                    List.of(
                                            new BatchDto.CommunicationDto(1, UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), null),
                                            new BatchDto.CommunicationDto(2, null, "Error 2")
                                    )
                            ),
                            UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), new BatchDto(
                                    BatchDto.BatchDtoStatus.ERROR_FORMAT,
                                    "Error 3",
                                    null
                            )
                    )
            }
    };

    private final ResponseMapper mapper = new ResponseMapper();

    @ParameterizedTest
    @FieldSource("BATCH_RESPONSES")
    void testToBatchMapDto(ConsultaLoteResponse response, Object expected) throws BadConfigurationException {
        var result = mapper.toBatchMapDto(response);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testToBatchMapDtoError() {
        var clientError = getConsultaLoteResponse(getResultadoRespuestaType(10103, "El código de arrendador no existe en el sistema"), List.of());
        Assertions.assertThrows(BadConfigurationException.class, () -> mapper.toBatchMapDto(clientError));

        var otherError = getConsultaLoteResponse(getResultadoRespuestaType(10108, "No ha informado la petición"), List.of());
        Assertions.assertThrows(RuntimeException.class, () -> mapper.toBatchMapDto(otherError));
    }

    @Test
    void testToUUID() throws BadConfigurationException {
        var result = mapper.toUUID(getComunicacionResponse(0, "Ok", "123e4567-e89b-12d3-a456-426614174000"));
        Assertions.assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), result);
    }

    @Test
    void testToUUIDError() {
        var clientError = getComunicacionResponse(10107, "Usuario incorrecto", null);
        Assertions.assertThrows(BadConfigurationException.class, () -> mapper.toUUID(clientError));

        var otherError = getComunicacionResponse(10109, "No ha informado la cabecera", null);
        Assertions.assertThrows(RuntimeException.class, () -> mapper.toUUID(otherError));
    }

    // Static helper methods to create test data

    private static ComunicacionResponse getComunicacionResponse(int codigo, String descripcion, String lote) {
        var response = COMUNICACION_FACTORY.createComunicacionResponse();
        response.setRespuesta(TIPOCOMUNICACION_FACTORY.createEstadoRespuestaType());
        response.getRespuesta().setCodigo(codigo);
        response.getRespuesta().setDescripcion(descripcion);
        response.getRespuesta().setLote(lote);
        return response;
    }

    private static ConsultaLoteResponse getConsultaLoteResponse(ResultadoRespuestaType resultadoRespuestaType, List<WscomunicacionType> wscomunicacionTypes) {
        var response = COMUNICACION_FACTORY.createConsultaLoteResponse();
        response.setRespuesta(resultadoRespuestaType);
        response.getResultado().addAll(wscomunicacionTypes);
        return response;
    }

    private static WscomunicacionType getWscomunicacionType(String lote, int codigoEstado, String descEstado, List<ResultadoType> resultadosType) {
        var wscomunicacionType = TIPOCOMUNICACION_FACTORY.createWscomunicacionType();
        wscomunicacionType.setLote(lote);
        wscomunicacionType.setCodigoEstado(codigoEstado);
        wscomunicacionType.setDescEstado(descEstado);
        wscomunicacionType.setResultadoComunicaciones(TIPOCOMUNICACION_FACTORY.createResultadosType());

        // Emulate response from SES where resultadoComunicaciones can be null
        if (resultadosType != null)
            wscomunicacionType.getResultadoComunicaciones().getResultadoComunicacion().addAll(resultadosType);
        else
            wscomunicacionType.setResultadoComunicaciones(null);

        return wscomunicacionType;
    }

    private static ResultadoType getResultadoType(int order, String codigoComunicacion, String error) {
        var resultadoType = TIPOCOMUNICACION_FACTORY.createResultadoType();
        resultadoType.setOrden(order);
        resultadoType.setCodigoComunicacion(codigoComunicacion);
        resultadoType.setError(error);
        return resultadoType;
    }

    private static ResultadoRespuestaType getResultadoRespuestaType(int code, String description) {
        var respuesta = TIPOCOMUNICACION_FACTORY.createResultadoRespuestaType();
        respuesta.setCodigo(code);
        respuesta.setDescripcion(description);
        return respuesta;
    }
}
