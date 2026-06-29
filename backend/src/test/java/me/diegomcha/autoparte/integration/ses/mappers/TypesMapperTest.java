package me.diegomcha.autoparte.integration.ses.mappers;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;

import java.util.List;
import java.util.UUID;

class TypesMapperTest {

    private final TypesMapper typesMapper = new TypesMapper();

    private Jaxb2Marshaller marshaller;
    private List<Booking> bookings;
    private String accommodationCode;

    @BeforeEach
    void setUp() {
        // Get marshaller and set packages to scan for JAXB context
        this.marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(
                "es.mir.hospedajes.neg.altapartehospedaje",
                "es.mir.hospedajes.neg.altareservahospedaje",
                "es.mir.hospedajes.neg.anularcomunicacion",
                "es.mir.hospedajes.neg.tiposgenerales",
                "es.mir.hospedajes.neg.altapartehospedaje",
                "es.mir.hospedajes.servicios.soap.comunicacion",
                "es.mir.hospedajes.servicios.soap.tipocomunicacion");

        // Create test data: accommodation, bookings, and persons
        try (var ignored = TestingUtils.getMockedUuidGenerator()) {
            var accommodation = new Accommodation("name", "SESCODE", null);
            var booking1 = new Booking(
                    accommodation,
                    TestingUtils.INSTANT,
                    TestingUtils.FUTURE_INSTANT,
                    1,
                    Payment.of(
                            Payment.PaymentType.CASH,
                            null,
                            null,
                            null,
                            null
                    ),
                    null,
                    null
            );
            new Person(
                    booking1,
                    new PersonalInfo(
                            "name",
                            "surname1",
                            null,
                            null,
                            TestingUtils.PAST_INSTANT,
                            null
                    ),
                    new ContactInfo(
                            null,
                            null,
                            "email@email.com"
                    ),
                    null,
                    null,
                    null
            );

            var booking2 = new Booking(
                    accommodation,
                    TestingUtils.INSTANT,
                    TestingUtils.FUTURE_INSTANT,
                    2,
                    Payment.of(
                            Payment.PaymentType.CREDIT_CARD,
                            "mean",
                            "holder",
                            TestingUtils.PAST_INSTANT,
                            TestingUtils.FUTURE_INSTANT
                    ),
                    1,
                    true
            );
            new Person(
                    booking2,
                    new PersonalInfo(
                            "name",
                            "surname1",
                            "surname2",
                            "ESP",
                            TestingUtils.PAST_INSTANT,
                            PersonalInfo.PersonalInfoGender.MALE
                    ),
                    new ContactInfo(
                            "943 63 39 68",
                            "971 37 71 26",
                            "email@email.com"
                    ),
                    Document.of(
                            Document.DocumentType.NIF,
                            "82198129Q",
                            "support"
                    ),
                    Address.of(
                            "line1",
                            "line2",
                            "municipality",
                            "postalcode",
                            "USA"
                    ),
                    Person.PersonRelationship.NEPHEW_NIECE
            );
            new Person(
                    booking2,
                    new PersonalInfo(
                            "name_2",
                            "surname1_2",
                            "surname2_2",
                            "ESP",
                            TestingUtils.PAST_INSTANT,
                            PersonalInfo.PersonalInfoGender.MALE
                    ),
                    new ContactInfo(
                            "943 63 39 68",
                            "971 37 71 26",
                            "email_2@email.com"
                    ),
                    Document.of(
                            Document.DocumentType.PASSPORT,
                            "number_2",
                            null
                    ),
                    Address.of(
                            "line1_2",
                            "line2_2",
                            "12345",
                            "12345",
                            "ESP"
                    ),
                    Person.PersonRelationship.GRANDCHILD
            );

            this.accommodationCode = accommodation.getSesCode();
            this.bookings = List.of(booking1, booking2);
        }
    }

    @Test
    void testToPeticionReserva() {
        this.assertXmlEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns5:peticion xmlns:ns5=\"http://www.neg.hospedajes.mir.es/altaReservaHospedaje\" xmlns:ns2=\"http://www.neg.hospedajes.mir.es/anularComunicacion\" xmlns:ns4=\"http://www.neg.hospedajes.mir.es/altaParteHospedaje\" xmlns:ns3=\"http://www.soap.servicios.hospedajes.mir.es/comunicacion\"><solicitud><comunicacion><establecimiento><codigo>SESCODE</codigo></establecimiento><contrato><referencia>00000000-0000-0000-0000-000000000003</referencia><fechaEntrada>2024-01-01T00:00:00.000Z</fechaEntrada><fechaSalida>2024-01-01T01:00:00.000Z</fechaSalida><numPersonas>1</numPersonas><pago><tipoPago>EFECT</tipoPago></pago></contrato><persona><rol>TI</rol><nombre>name</nombre><apellido1>surname1</apellido1><fechaNacimiento>2023-12-31Z</fechaNacimiento><correo>email@email.com</correo></persona></comunicacion><comunicacion><establecimiento><codigo>SESCODE</codigo></establecimiento><contrato><referencia>00000000-0000-0000-0000-000000000006</referencia><fechaEntrada>2024-01-01T00:00:00.000Z</fechaEntrada><fechaSalida>2024-01-01T01:00:00.000Z</fechaSalida><numPersonas>2</numPersonas><numHabitaciones>1</numHabitaciones><internet>true</internet><pago><tipoPago>TARJT</tipoPago><fechaPago>2023-12-31Z</fechaPago><medioPago>mean</medioPago><titular>holder</titular><caducidadTarjeta>01/2024</caducidadTarjeta></pago></contrato><persona><rol>TI</rol><nombre>name</nombre><apellido1>surname1</apellido1><apellido2>surname2</apellido2><tipoDocumento>NIF</tipoDocumento><numeroDocumento>82198129Q</numeroDocumento><fechaNacimiento>2023-12-31Z</fechaNacimiento><nacionalidad>ESP</nacionalidad><sexo>H</sexo><direccion><direccion>line1</direccion><direccionComplementaria>line2</direccionComplementaria><nombreMunicipio>municipality</nombreMunicipio><codigoPostal>postalcode</codigoPostal><pais>USA</pais></direccion><telefono>943 63 39 68</telefono><telefono2>971 37 71 26</telefono2><correo>email@email.com</correo></persona><persona><rol>VI</rol><nombre>name_2</nombre><apellido1>surname1_2</apellido1><apellido2>surname2_2</apellido2><tipoDocumento>PAS</tipoDocumento><numeroDocumento>number_2</numeroDocumento><fechaNacimiento>2023-12-31Z</fechaNacimiento><nacionalidad>ESP</nacionalidad><sexo>H</sexo><direccion><direccion>line1_2</direccion><direccionComplementaria>line2_2</direccionComplementaria><codigoMunicipio>12345</codigoMunicipio><codigoPostal>12345</codigoPostal><pais>ESP</pais></direccion><telefono>943 63 39 68</telefono><telefono2>971 37 71 26</telefono2><correo>email_2@email.com</correo></persona></comunicacion></solicitud></ns5:peticion>",
                typesMapper.toPeticionReserva(bookings)
        );
    }

    @Test
    void testToPeticionAlta() {
        this.assertXmlEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns4:peticion xmlns:ns5=\"http://www.neg.hospedajes.mir.es/altaReservaHospedaje\" xmlns:ns2=\"http://www.neg.hospedajes.mir.es/anularComunicacion\" xmlns:ns4=\"http://www.neg.hospedajes.mir.es/altaParteHospedaje\" xmlns:ns3=\"http://www.soap.servicios.hospedajes.mir.es/comunicacion\"><solicitud><codigoEstablecimiento>SESCODE</codigoEstablecimiento><comunicacion><contrato><referencia>00000000-0000-0000-0000-000000000003</referencia><fechaEntrada>2024-01-01T00:00:00.000Z</fechaEntrada><fechaSalida>2024-01-01T01:00:00.000Z</fechaSalida><numPersonas>1</numPersonas><pago><tipoPago>EFECT</tipoPago></pago></contrato><persona><rol>VI</rol><nombre>name</nombre><apellido1>surname1</apellido1><fechaNacimiento>2023-12-31Z</fechaNacimiento><correo>email@email.com</correo></persona></comunicacion><comunicacion><contrato><referencia>00000000-0000-0000-0000-000000000006</referencia><fechaEntrada>2024-01-01T00:00:00.000Z</fechaEntrada><fechaSalida>2024-01-01T01:00:00.000Z</fechaSalida><numPersonas>2</numPersonas><numHabitaciones>1</numHabitaciones><internet>true</internet><pago><tipoPago>TARJT</tipoPago><fechaPago>2023-12-31Z</fechaPago><medioPago>mean</medioPago><titular>holder</titular><caducidadTarjeta>01/2024</caducidadTarjeta></pago></contrato><persona><rol>VI</rol><nombre>name</nombre><apellido1>surname1</apellido1><apellido2>surname2</apellido2><tipoDocumento>NIF</tipoDocumento><numeroDocumento>82198129Q</numeroDocumento><soporteDocumento>support</soporteDocumento><fechaNacimiento>2023-12-31Z</fechaNacimiento><nacionalidad>ESP</nacionalidad><sexo>H</sexo><direccion><direccion>line1</direccion><direccionComplementaria>line2</direccionComplementaria><nombreMunicipio>municipality</nombreMunicipio><codigoPostal>postalcode</codigoPostal><pais>USA</pais></direccion><telefono>943 63 39 68</telefono><telefono2>971 37 71 26</telefono2><correo>email@email.com</correo><parentesco>SB</parentesco></persona><persona><rol>VI</rol><nombre>name_2</nombre><apellido1>surname1_2</apellido1><apellido2>surname2_2</apellido2><tipoDocumento>PAS</tipoDocumento><numeroDocumento>number_2</numeroDocumento><fechaNacimiento>2023-12-31Z</fechaNacimiento><nacionalidad>ESP</nacionalidad><sexo>H</sexo><direccion><direccion>line1_2</direccion><direccionComplementaria>line2_2</direccionComplementaria><codigoMunicipio>12345</codigoMunicipio><codigoPostal>12345</codigoPostal><pais>ESP</pais></direccion><telefono>943 63 39 68</telefono><telefono2>971 37 71 26</telefono2><correo>email_2@email.com</correo><parentesco>NI</parentesco></persona></comunicacion></solicitud></ns4:peticion>",
                typesMapper.toPeticionAlta(accommodationCode, bookings)
        );
    }

    @Test
    void testToComunicacionAnulacion() {
        this.assertXmlEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:comunicaciones xmlns:ns5=\"http://www.neg.hospedajes.mir.es/altaReservaHospedaje\" xmlns:ns2=\"http://www.neg.hospedajes.mir.es/anularComunicacion\" xmlns:ns4=\"http://www.neg.hospedajes.mir.es/altaParteHospedaje\" xmlns:ns3=\"http://www.soap.servicios.hospedajes.mir.es/comunicacion\"><ns2:codigoComunicacion>00000000-0000-0000-0000-000000000001</ns2:codigoComunicacion><ns2:codigoComunicacion>00000000-0000-0000-0000-000000000002</ns2:codigoComunicacion></ns2:comunicaciones>",
                typesMapper.toComunicacionAnulacion(List.of(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000002")))
        );
    }

    private void assertXmlEquals(String expected, Object element) {
        var result = new StringResult();
        marshaller.marshal(element, result);

        Assertions.assertEquals(
                expected,
                result.toString()
        );
    }
}
