package com.eventflow.event.validator;

import com.eventflow.event.entity.Event;
import com.eventflow.event.entity.EventStatus;
import com.eventflow.event.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class TicketTypeValidator {

    public void validateCanModify(Event event) {
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Ticket types can be modified only while the event is in DRAFT status");
        }
    }

    public void validateDetails(
            String name,
            BigDecimal price,
            String currency,
            int capacity
    ) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Ticket type name must not be blank");
        }

        if (price == null || price.signum() <= 0) {
            throw new BusinessRuleException("Ticket price must be greater than zero");
        }

        if (price.scale() > 2) {
            throw new BusinessRuleException("Ticket price must have at most two decimal places");
        }

        if (capacity <= 0) {throw new BusinessRuleException("Ticket capacity must be greater than zero");
        }

        validateCurrency(currency);
    }

    private void validateCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new BusinessRuleException("Currency is required");
        }

        try {
            Currency.getInstance(currencyCode.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Unsupported currency code: " + currencyCode);
        }
    }
}
