package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.AutoparteProperties;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RequestMapper {

    public enum SesCommunicationType {
        BOOKING,
        CHECKIN
    }

    private final es.mir.hospedajes.servicios.soap.tipocomunicacion.ObjectFactory tipocomunicacionFactory = new es.mir.hospedajes.servicios.soap.tipocomunicacion.ObjectFactory();
    private final es.mir.hospedajes.servicios.soap.comunicacion.ObjectFactory comunicacionFactory = new es.mir.hospedajes.servicios.soap.comunicacion.ObjectFactory();

    private final ApplicationContext applicationContext;
    private final AutoparteProperties autoparteProperties;
    private final Marshaller marshaller;

    public ConsultaLoteRequest toConsultaLoteRequest(@NonNull Collection<@NonNull UUID> batchIds) {
        var request = comunicacionFactory.createConsultaLoteRequest();
        var codigosLoteType = tipocomunicacionFactory.createCodigosLoteType();

        codigosLoteType.getLote().addAll(
                batchIds.stream()
                        .map(UUID::toString)
                        .toList()
        );

        request.setCodigosLote(codigosLoteType);
        return request;
    }

    public ComunicacionRequest toSubmitCommunicationRequest(SesCommunicationType type, Object peticion) {
        return this.createCommunicationRequest(
                "A",
                switch (type) {
                    case BOOKING -> "RH";
                    case CHECKIN -> "PV";
                },
                peticion
        );
    }

    public ComunicacionRequest toCancelCommunicationRequest(Object peticion) {
        return this.createCommunicationRequest(
                "B",
                null,
                peticion
        );
    }

    private ComunicacionRequest createCommunicationRequest(@NonNull String tipoOperacion, @Nullable String tipoComunicacion, @NonNull Object peticionContent) {
        var request = comunicacionFactory.createComunicacionRequest();
        var peticion = tipocomunicacionFactory.createPeticionType();
        var cabecera = tipocomunicacionFactory.createCabeceraLoteType();

        cabecera.setCodigoArrendador(autoparteProperties.getSes().getLandlordCode());
        cabecera.setAplicacion(applicationContext.getApplicationName());
        cabecera.setTipoOperacion(tipoOperacion);
        cabecera.setTipoComunicacion(tipoComunicacion);

        peticion.setCabecera(cabecera);
        peticion.setSolicitud(this.encodePeticion(peticionContent));
        request.setPeticion(peticion);
        return request;
    }

    private String encodePeticion(@NonNull Object obj) {
        try (var outputStream = new ByteArrayOutputStream()) {
            try (var zipStream = new ZipOutputStream(outputStream)) {
                zipStream.putNextEntry(new ZipEntry("peticion.xml"));
                marshaller.marshal(obj, new StreamResult(zipStream));
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
