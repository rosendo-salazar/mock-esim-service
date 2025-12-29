package com.flyroamy.mock.exception;

public class EsimExpiredException extends RuntimeException {
    private final String esimId;

    public EsimExpiredException(String esimId) {
        super("eSIM with ID '" + esimId + "' has expired");
        this.esimId = esimId;
    }

    public String getEsimId() {
        return esimId;
    }
}
