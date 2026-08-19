package com.eventflow.event.validator;

import com.eventflow.event.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketTypeValidatorTest {

    private final TicketTypeValidator validator = new TicketTypeValidator();

    @Test
    void shouldAcceptValidTicketType() {
        assertDoesNotThrow(() -> validator.validateDetails(
                "VIP",
                new BigDecimal("199.99"),
                "PLN",
                100
                )
        );
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(BusinessRuleException.class, () -> validator.validateDetails(
                "VIP",
                new BigDecimal("-1.00"),
                "PLN",
                100
                )
        );
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        assertThrows(BusinessRuleException.class, () -> validator.validateDetails(
                "VIP",
                new BigDecimal("100.00"),
                "ABC",
                100
                )
        );
    }
}
