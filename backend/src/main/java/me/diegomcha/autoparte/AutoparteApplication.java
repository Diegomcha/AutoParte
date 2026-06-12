package me.diegomcha.autoparte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutoparteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoparteApplication.class, args);
    }

    // TODO: Uncomment this code to test the SOAP API connection
//    @Bean
//    CommandLineRunner test(WebServiceTemplate sesAPI) {
//        return args -> {
//            try {
//                var objectFactory = new ObjectFactory();
//                var catalogo = objectFactory.createCatalogoRequestType();
//                catalogo.setCatalogo("SEXO");
//                var request = objectFactory.createCatalogoRequest();
//                request.setPeticion(catalogo);
//
//                var response = (CatalogoResponse) sesAPI
//                        .marshalSendAndReceive(request);
//
//                System.out.println(response);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        };
//    }
}
