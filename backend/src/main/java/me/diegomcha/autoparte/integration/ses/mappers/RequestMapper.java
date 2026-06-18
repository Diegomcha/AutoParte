package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RequestMapper {

    public enum SesCommunicationType {
        BOOKING,
        CHECKIN
    }

    private final es.mir.hospedajes.servicios.soap.tipocomunicacion.ObjectFactory tipocomunicacionFactory = new es.mir.hospedajes.servicios.soap.tipocomunicacion.ObjectFactory();
    private final es.mir.hospedajes.servicios.soap.comunicacion.ObjectFactory comunicacionFactory = new es.mir.hospedajes.servicios.soap.comunicacion.ObjectFactory();

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

    public ComunicacionRequest toSubmitCommunicationRequest(String appName, String landlordCode, SesCommunicationType type, String encodedSolicitud) {
        return this.createCommunicationRequest(
                appName,
                landlordCode,
                "A",
                switch (type) {
                    case BOOKING -> "RH";
                    case CHECKIN -> "PV";
                },
                encodedSolicitud
        );
    }

    public ComunicacionRequest toCancelCommunicationRequest(String appName, String landlordCode, String encodedSolicitud) {
        return this.createCommunicationRequest(
                appName,
                landlordCode,
                "B",
                null,
                encodedSolicitud
        );
    }

    private ComunicacionRequest createCommunicationRequest(@NonNull String appName, @NonNull String codigoArrendador, @NonNull String tipoOperacion, @Nullable String tipoComunicacion, @NonNull String encodedSolicitud) {
        var request = comunicacionFactory.createComunicacionRequest();
        var peticion = tipocomunicacionFactory.createPeticionType();
        var cabecera = tipocomunicacionFactory.createCabeceraLoteType();

        cabecera.setCodigoArrendador(codigoArrendador);
        cabecera.setAplicacion(appName);
        cabecera.setTipoOperacion(tipoOperacion);
        cabecera.setTipoComunicacion(tipoComunicacion);

        peticion.setCabecera(cabecera);
        peticion.setSolicitud(encodedSolicitud);
        request.setPeticion(peticion);
        return request;
    }

}
