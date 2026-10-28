package dev.mmatokovic.pantrio.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

class BarcodeValidator implements ConstraintValidator<Barcode, String> {

    private static final Pattern NUMERIC = Pattern.compile("\\d{8}|\\d{12}|\\d{13}");

    @Override
    public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (!NUMERIC.matcher(value).matches()) {
            return false;
        }
        return hasValidCheckDigit(value);
    }

    private static boolean hasValidCheckDigit(String barcode) {
        int sum = 0;
        int length = barcode.length();
        for (int i = 0; i < length - 1; i++) {
            int digit = barcode.charAt(i) - '0';
            int weight = ((length - 1 - i) % 2 == 0) ? 1 : 3;
            sum += digit * weight;
        }
        int expected = (10 - (sum % 10)) % 10;
        return (barcode.charAt(length - 1) - '0') == expected;
    }
}
