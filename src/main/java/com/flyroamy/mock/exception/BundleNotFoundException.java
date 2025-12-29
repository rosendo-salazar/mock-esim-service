package com.flyroamy.mock.exception;

public class BundleNotFoundException extends RuntimeException {
    private final String bundleId;

    public BundleNotFoundException(String bundleId) {
        super("Bundle with ID '" + bundleId + "' was not found");
        this.bundleId = bundleId;
    }

    public String getBundleId() {
        return bundleId;
    }
}
