package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionResponse;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteResponse;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceValidationException;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ResponseMapper {

    public Map<UUID, BatchDto> toBatchMapDto(ConsultaLoteResponse response) throws BadConfigurationException, ServiceValidationException {
        this.handleErrorResponse(response.getRespuesta().getCodigo(), response.getRespuesta().getDescripcion());

        return response.getResultado().stream()
                .map(batch ->
                        Map.entry(
                                UUID.fromString(batch.getLote()),
                                new BatchDto(
                                        BatchDto.BatchDtoStatus.values()[batch.getCodigoEstado() - 1],
                                        batch.getDescEstado(),
                                        batch.getResultadoComunicaciones() != null
                                                ? batch.getResultadoComunicaciones().getResultadoComunicacion().stream()
                                                .map(comunicacion ->
                                                        new BatchDto.CommunicationDto(
                                                                comunicacion.getOrden(),
                                                                comunicacion.getCodigoComunicacion() != null
                                                                        ? UUID.fromString(comunicacion.getCodigoComunicacion())
                                                                        : null,
                                                                comunicacion.getError()
                                                        ))
                                                .toList()
                                                : null
                                ))
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public UUID toUUID(ComunicacionResponse response) throws BadConfigurationException, ServiceValidationException {
        this.handleErrorResponse(response.getRespuesta().getCodigo(), response.getRespuesta().getDescripcion());

        return UUID.fromString(response.getRespuesta().getLote());
    }

    private void handleErrorResponse(int code, String description) throws BadConfigurationException, ServiceValidationException {
        // Response was successful
        if (code == 0)
            return;

        // Handle known validation errors
        if (ServiceValidationException.matchesCode(code))
            throw new ServiceValidationException(code, description);

        // Handle known configuration errors
        if (BadConfigurationException.matchesCode(code))
            throw new BadConfigurationException(code, description);

        throw new RuntimeException("Unexpected error code from SES: [" + code + "] " + description);
    }
}
