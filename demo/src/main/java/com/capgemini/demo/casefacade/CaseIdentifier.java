package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;

@Embeddable
public class CaseIdentifier {

    private String caseType;
    private String primaryTransactionId;
    private String cardNumberMasked;
    private String cardToken;
    private String customerId;

    public CaseIdentifier() {}

    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }

    public String getPrimaryTransactionId() { return primaryTransactionId; }
    public void setPrimaryTransactionId(String primaryTransactionId) {
        this.primaryTransactionId = primaryTransactionId;
    }

    public String getCardNumberMasked() { return cardNumberMasked; }
    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
