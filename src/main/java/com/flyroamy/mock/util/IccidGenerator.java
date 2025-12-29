package com.flyroamy.mock.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class IccidGenerator {

    private static final Random random = new Random();

    /**
     * Generates a valid ICCID (Integrated Circuit Card Identifier)
     * Format: 89 (Telecom) + 01 (Country - US) + Issuer ID (4 digits) + Individual ID (11 digits) + Check digit
     *
     * @return A 20-digit ICCID string
     */
    public String generate() {
        StringBuilder iccid = new StringBuilder();

        // Major Industry Identifier (89 = Telecom)
        iccid.append("89");

        // Country Code (01 = US, 44 = UK, etc.) - using US
        iccid.append("01");

        // Issuer Identifier (4 digits) - mock issuer
        iccid.append("2345");

        // Individual Account Identification (11 digits)
        for (int i = 0; i < 11; i++) {
            iccid.append(random.nextInt(10));
        }

        // Calculate and append Luhn check digit
        int checkDigit = calculateLuhnCheckDigit(iccid.toString());
        iccid.append(checkDigit);

        return iccid.toString();
    }

    /**
     * Generates an ICCID with a specific prefix for testing
     */
    public String generateWithPrefix(String prefix) {
        StringBuilder iccid = new StringBuilder(prefix);

        int remainingDigits = 19 - prefix.length();
        for (int i = 0; i < remainingDigits; i++) {
            iccid.append(random.nextInt(10));
        }

        int checkDigit = calculateLuhnCheckDigit(iccid.toString());
        iccid.append(checkDigit);

        return iccid.toString();
    }

    /**
     * Calculates the Luhn check digit for ICCID validation
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    /**
     * Validates an ICCID using Luhn algorithm
     */
    public boolean validate(String iccid) {
        if (iccid == null || iccid.length() != 20) {
            return false;
        }

        try {
            int sum = 0;
            boolean alternate = false;

            for (int i = iccid.length() - 1; i >= 0; i--) {
                int digit = Character.getNumericValue(iccid.charAt(i));

                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }

                sum += digit;
                alternate = !alternate;
            }

            return (sum % 10 == 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
