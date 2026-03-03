package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CaseTransaction {

    private BigDecimal transactionAmount;
    private String transactionCurrency;
    private String transactionDateTime;
    private String merchantCategoryCode;
    private String scheme;
    private String transactionCountry;
}
