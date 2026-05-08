package me.diegomcha.autoparte.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class ResourceUnprocessableException extends Exception {
    public ResourceUnprocessableException(String message) {
        super(message);
    }
}
