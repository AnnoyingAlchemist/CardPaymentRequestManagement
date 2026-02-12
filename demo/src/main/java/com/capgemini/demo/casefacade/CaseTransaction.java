package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;

@Embeddable
public class CaseTransaction {

    private String transactionAmount;
    private String transactionCurrency;
    private String transactionDateTime;
    private String merchantCategoryCode;
    private String scheme;

    // getters/setters
}
