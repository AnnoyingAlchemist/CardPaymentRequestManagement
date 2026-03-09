package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CaseTransaction {

    private BigDecimal transactionAmount;
    private String transactionCurrency;

    // ✅ Refactored from String → LocalDateTime
    private LocalDateTime transactionDateTime;

    private String merchantCategoryCode;
    private String scheme;
    private String transactionCountry;
}
