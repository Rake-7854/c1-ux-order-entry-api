package com.rrd.c1ux.api.exceptions;

public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = -1083328286624784422L;

    public BadRequestException(String message) {
        super(message);
    }
}