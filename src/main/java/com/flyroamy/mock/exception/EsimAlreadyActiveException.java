package com.flyroamy.mock.exception;

public class EsimAlreadyActiveException extends RuntimeException {
    private final String esimId;

    public EsimAlreadyActiveException(String esimId) {
        super("eSIM with ID '" + esimId + "' is already active");
        this.esimId = esimId;
    }

    public String getEsimId() {
        return esimId;
    }
}
