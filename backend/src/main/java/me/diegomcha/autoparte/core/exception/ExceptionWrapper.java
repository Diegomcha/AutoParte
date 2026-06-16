package me.diegomcha.autoparte.core.exception;

public class ExceptionWrapper extends RuntimeException {
    public ExceptionWrapper(Exception e) {
        super(e);
    }
}
