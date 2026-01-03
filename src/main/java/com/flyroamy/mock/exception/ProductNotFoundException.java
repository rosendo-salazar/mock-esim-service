package com.flyroamy.mock.exception;

public class ProductNotFoundException extends RuntimeException {
    private final String productId;

    public ProductNotFoundException(String productId) {
        super("Product with ID '" + productId + "' was not found");
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    // Backward compatibility
    public String getBundleId() {
        return productId;
    }
}
