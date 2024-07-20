package com.rrd.c1ux.api.exceptions;

public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2382268493248702961L;

    public EntityNotFoundException(String identifyingFieldValues) {
        super("Could not find entity identified by " + identifyingFieldValues);
    }
}
