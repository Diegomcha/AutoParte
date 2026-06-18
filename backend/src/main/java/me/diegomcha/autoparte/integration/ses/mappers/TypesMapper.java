package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.neg.tiposgenerales.*;
import jakarta.xml.bind.JAXBElement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.address.SpanishAddress;
import me.diegomcha.autoparte.domain.booking.payment.CreditCardPaymentInfo;
import me.diegomcha.autoparte.domain.booking.payment.PaymentInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DniDocumentInfo;
import me.diegomcha.autoparte.domain.person.document.DocumentInfo;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TypesMapper {

    private static final DateTimeFormatter CC_EXPIRYDATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy").withZone(ZoneId.systemDefault());

    private final es.mir.hospedajes.neg.altapartehospedaje.ObjectFactory altaFactory = new es.mir.hospedajes.neg.altapartehospedaje.ObjectFactory();
    private final es.mir.hospedajes.neg.altareservahospedaje.ObjectFactory reservaFactory = new es.mir.hospedajes.neg.altareservahospedaje.ObjectFactory();
    private final es.mir.hospedajes.neg.anularcomunicacion.ObjectFactory anularFactory = new es.mir.hospedajes.neg.anularcomunicacion.ObjectFactory();
    private final es.mir.hospedajes.neg.tiposgenerales.ObjectFactory generalFactory = new es.mir.hospedajes.neg.tiposgenerales.ObjectFactory();

    public JAXBElement<es.mir.hospedajes.neg.altareservahospedaje.PeticionType> toPeticionReserva(@NonNull Collection<@NonNull Booking> bookings) {
        var reserva = reservaFactory.createPeticionType();
        var solicitud = reservaFactory.createSolicitudType();

        solicitud.getComunicacion().addAll(
                bookings.stream()
                        .map(booking -> {
                            var comunicacion = reservaFactory.createComunicacionType();

                            var establecimiento = generalFactory.createEstablecimientoType();
                            establecimiento.setCodigo(booking.getAccommodation().getSesCode());
                            comunicacion.setEstablecimiento(establecimiento);

                            comunicacion.setContrato(this.toContratoHospedajeType(booking));

                            // Add the first person as titular and the rest as visitors
                            comunicacion.getPersona().add(this.toPersonaReservaType(booking.getPeople().getFirst(), true));
                            comunicacion.getPersona().addAll(
                                    booking.getPeople().stream()
                                            .skip(1) // Skip the first person since they are already added as titular
                                            .map(person -> this.toPersonaReservaType(person, false))
                                            .toList()
                            );

                            return comunicacion;
                        })
                        .toList()
        );

        reserva.setSolicitud(solicitud);
        return reservaFactory.createPeticion(reserva);
    }

    public JAXBElement<es.mir.hospedajes.neg.altapartehospedaje.PeticionType> toPeticionAlta(@NonNull String accommodationSesCode, @NonNull Collection<@NonNull Booking> bookings) {
        var alta = altaFactory.createPeticionType();
        var solicitud = altaFactory.createSolicitudType();

        solicitud.setCodigoEstablecimiento(accommodationSesCode);

        solicitud.getComunicacion().addAll(
                bookings.stream()
                        .map(booking -> {
                            var comunicacion = altaFactory.createComunicacionType();

                            comunicacion.setContrato(this.toContratoHospedajeType(booking));

                            comunicacion.getPersona().addAll(
                                    booking.getPeople().stream()
                                            .map(this::toPersonaHospedajeType)
                                            .toList()
                            );

                            return comunicacion;
                        }).toList()
        );

        alta.setSolicitud(solicitud);
        return altaFactory.createPeticion(alta);
    }

    public JAXBElement<es.mir.hospedajes.neg.anularcomunicacion.ComunicacionType> toComunicacionAnulacion(@NonNull Collection<@NonNull UUID> sesIds) {
        var comunicacion = anularFactory.createComunicacionType();
        comunicacion.getCodigoComunicacion().addAll(
                sesIds.stream()
                        .map(UUID::toString)
                        .toList()
        );
        return anularFactory.createComunicaciones(comunicacion);
    }

    private ContratoHospedajeType toContratoHospedajeType(@NonNull Booking booking) {
        var contrato = generalFactory.createContratoHospedajeType();
        contrato.setReferencia(booking.getId().toString());
        contrato.setFechaContrato(this.toXmlDate(booking.getCreatedAt()));
        contrato.setFechaEntrada(this.toXmlDate(booking.getStartTime()));
        contrato.setFechaSalida(this.toXmlDate(booking.getEndTime()));
        contrato.setNumPersonas(booking.getNumberOfPeople());
        contrato.setNumHabitaciones(booking.getNumberOfRooms());
        contrato.setInternet(booking.getInternetConnection());
        contrato.setPago(this.toPagoType(booking.getPayment()));
        return contrato;
    }

    private PersonaReservaType toPersonaReservaType(@NonNull Person person, boolean isTitular) {
        var pReserva = generalFactory.createPersonaReservaType();
        pReserva.setRol(isTitular ? RolPersonaType.TI : RolPersonaType.VI);

        pReserva.setNombre(person.getPersonalInfo().getName());
        pReserva.setApellido1(person.getPersonalInfo().getFirstSurname());
        pReserva.setApellido2(person.getPersonalInfo().getSecondSurname());
        pReserva.setSexo(this.toSexo(person.getPersonalInfo().getGender()));
        pReserva.setFechaNacimiento(this.toXmlDate(person.getPersonalInfo().getBirthDate()));
        pReserva.setNacionalidad(person.getPersonalInfo().getNationality());

        pReserva.setTelefono(person.getContactInfo().getPhoneNumber1());
        pReserva.setTelefono2(person.getContactInfo().getPhoneNumber2());
        pReserva.setCorreo(person.getContactInfo().getEmail());

        if (person.getDocumentInfo() != null) {
            pReserva.setTipoDocumento(this.toTipoDocumento(person.getDocumentInfo().getType()));
            pReserva.setNumeroDocumento(person.getDocumentInfo().getNumber());
        }

        pReserva.setDireccion(this.toDireccionType(person.getAddress()));

        return pReserva;
    }

    private PersonaHospedajeType toPersonaHospedajeType(@NonNull Person person) {
        var pHospedaje = generalFactory.createPersonaHospedajeType();

        pHospedaje.setRol(RolPersonaType.VI);

        pHospedaje.setNombre(person.getPersonalInfo().getName());
        pHospedaje.setApellido1(person.getPersonalInfo().getFirstSurname());
        pHospedaje.setApellido2(person.getPersonalInfo().getSecondSurname());
        pHospedaje.setSexo(this.toSexo(person.getPersonalInfo().getGender()));
        pHospedaje.setFechaNacimiento(this.toXmlDate(person.getPersonalInfo().getBirthDate()));
        pHospedaje.setNacionalidad(person.getPersonalInfo().getNationality());

        pHospedaje.setTelefono(person.getContactInfo().getPhoneNumber1());
        pHospedaje.setTelefono2(person.getContactInfo().getPhoneNumber2());
        pHospedaje.setCorreo(person.getContactInfo().getEmail());

        if (person.getDocumentInfo() != null) {
            pHospedaje.setTipoDocumento(this.toTipoDocumento(person.getDocumentInfo().getType()));
            pHospedaje.setNumeroDocumento(person.getDocumentInfo().getNumber());
            if (person.getDocumentInfo() instanceof DniDocumentInfo dniDoc)
                pHospedaje.setSoporteDocumento(dniDoc.getSupportNumber());
        }

        pHospedaje.setDireccion(this.toDireccionType(person.getAddress()));

        pHospedaje.setParentesco(this.toParentesco(person.getRelationship()));

        return pHospedaje;
    }


    private DireccionType toDireccionType(Address address) {
        if (address == null) return null;

        var direccion = generalFactory.createDireccionType();
        direccion.setDireccion(address.getAddressLine1());
        direccion.setDireccionComplementaria(address.getAddressLine2());
        direccion.setCodigoPostal(address.getPostalCode());
        direccion.setPais(address.getCountry());

        if (address instanceof SpanishAddress sAddress)
            direccion.setCodigoMunicipio(sAddress.getMunicipality());
        else
            direccion.setNombreMunicipio(address.getMunicipality());

        return direccion;
    }

    private PagoType toPagoType(@NonNull PaymentInfo payment) {
        var pago = generalFactory.createPagoType();
        pago.setTipoPago(this.toTipoPago(payment.getType()));
        pago.setFechaPago(this.toXmlDate(payment.getDate()));
        pago.setMedioPago(payment.getMean());
        pago.setTitular(payment.getHolder());
        if (payment instanceof CreditCardPaymentInfo ccPayment)
            pago.setCaducidadTarjeta(this.toCCExpiryDate(ccPayment.getExpiryDate()));
        return pago;
    }

    private String toParentesco(Person.PersonRelationship relationship) {
        return switch (relationship) {
            case GRANDPARENT -> "AB";
            case GREAT_GRANDPARENT -> "BA";
            case GREAT_GRANDCHILD -> "BN";
            case SIBLING_IN_LAW -> "CD";
            case SPOUSE -> "CY";
            case CHILD -> "HJ";
            case SIBLING -> "HR";
            case GRANDCHILD -> "NI";
            case PARENT -> "PM";
            case NEPHEW_NIECE -> "SB";
            case PARENT_IN_LAW -> "SG";
            case UNCLE_AUNT -> "TI";
            case SON_DAUGHTER_IN_LAW -> "YN";
            case TUTOR -> "TU";
            case OTHER -> "OT";
            case null -> null;
        };
    }

    private String toSexo(PersonalInfo.PersonalInfoGender gender) {
        return switch (gender) {
            case MALE -> "H";
            case FEMALE -> "M";
            case OTHER -> "O";
            case null -> null;
        };
    }

    private String toTipoDocumento(DocumentInfo.DocumentType type) {
        return switch (type) {
            case NIF -> "NIF";
            case NIE -> "NIE";
            case PASSPORT -> "PAS";
            case OTHER -> "OTRO";
        };
    }

    private String toTipoPago(@NonNull PaymentInfo.PaymentType type) {
        return switch (type) {
            case CASH -> "EFECT";
            case CREDIT_CARD -> "TARJT";
            case PLATFORM -> "PLATF";
            case TRANSFER -> "TRANS";
            case MOBILE -> "MOVIL";
            case GIFT_CARD -> "TREG";
            case ON_SITE -> "DESTI";
            case OTHER -> "OTRO";
        };
    }

    private String toCCExpiryDate(Instant instant) {
        if (instant == null) return null;
        return CC_EXPIRYDATE_FORMATTER.format(instant);
    }

    private XMLGregorianCalendar toXmlDate(Instant instant) {
        if (instant == null) return null;

        var cal = new GregorianCalendar(TimeZone.getTimeZone(ZoneOffset.UTC));
        cal.setTimeInMillis(instant.toEpochMilli());

        return DatatypeFactory
                .newDefaultInstance()
                .newXMLGregorianCalendar(cal);
    }
}
