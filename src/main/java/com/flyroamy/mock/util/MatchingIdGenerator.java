package com.flyroamy.mock.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MatchingIdGenerator {

    /**
     * Generates a unique matching ID for eSIM activation
     * Format: UUID v4
     *
     * @return A UUID string
     */
    public String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates an eSIM ID with maya_ prefix
     *
     * @return An eSIM ID string
     */
    public String generateEsimId() {
        return "maya_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates an activation code
     * Format: 16 character alphanumeric uppercase
     *
     * @return An activation code string
     */
    public String generateActivationCode() {
        return UUID.randomUUID().toString()
            .replace("-", "")
            .substring(0, 16)
            .toUpperCase();
    }

    /**
     * Generates LPA activation string for QR code
     * Format: LPA:1$smdp.example.com$ACTIVATION_CODE
     *
     * @param activationCode The activation code
     * @return LPA string for QR code encoding
     */
    public String generateLpaString(String activationCode) {
        return "LPA:1$smdp.mock-maya.com$" + activationCode;
    }
}
