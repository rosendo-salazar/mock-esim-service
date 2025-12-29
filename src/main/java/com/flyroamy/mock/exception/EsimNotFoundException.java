package com.flyroamy.mock.exception;

public class EsimNotFoundException extends RuntimeException {
    private final String esimId;

    public EsimNotFoundException(String esimId) {
        super("eSIM with ID '" + esimId + "' was not found");
        this.esimId = esimId;
    }

    public String getEsimId() {
        return esimId;
    }
}
