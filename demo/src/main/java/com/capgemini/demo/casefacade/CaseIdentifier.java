package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;

@Embeddable
public class CaseIdentifier {

    private String caseType;
    private String primaryTransactionId;
    private String cardNumberMasked;
    private String cardToken;
    private String customerId;

    // getters/setters
}
