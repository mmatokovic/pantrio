package dev.mmatokovic.pantrio.domain.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BarcodeValidatorShould {

    @ParameterizedTest
    @ValueSource(strings = {"96385074", "737628064502", "8000500179864"})
    void isValid_acceptBarcode_whenCheckDigitMatches(String barcode) {
        var barcodeValidator = new BarcodeValidator();

        assertThat(barcodeValidator.isValid(barcode, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"8000500179865", "737628064503"})
    void isValid_rejectBarcode_whenCheckDigitDoesNotMatch(String barcode) {
        var barcodeValidator = new BarcodeValidator();

        assertThat(barcodeValidator.isValid(barcode, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567", "12345678901", "80005001798AB", "12345678901234"})
    void isValid_rejectBarcode_whenFormatIsWrong(String barcode) {
        var barcodeValidator = new BarcodeValidator();

        assertThat(barcodeValidator.isValid(barcode, null)).isFalse();
    }

    @Test
    void isValid_acceptNull() {
        var barcodeValidator = new BarcodeValidator();

        assertThat(barcodeValidator.isValid(null, null)).isTrue();
    }
}
