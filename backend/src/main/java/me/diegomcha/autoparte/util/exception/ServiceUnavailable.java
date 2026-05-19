package me.diegomcha.autoparte.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailable extends Exception {
    public ServiceUnavailable(String message) {
        super(message);
    }
}
