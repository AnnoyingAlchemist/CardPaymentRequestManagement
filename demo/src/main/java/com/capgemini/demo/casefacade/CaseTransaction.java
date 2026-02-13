package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;

@Embeddable
public class CaseTransaction {

    private String transactionAmount;
    private String transactionCurrency;
    private String transactionDateTime;
    private String merchantCategoryCode;
    private String scheme;

    public CaseTransaction() {}

    public String getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionCurrency() { return transactionCurrency; }
    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getTransactionDateTime() { return transactionDateTime; }
    public void setTransactionDateTime(String transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public String getMerchantCategoryCode() { return merchantCategoryCode; }
    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getScheme() { return scheme; }
    public void setScheme(String scheme) { this.scheme = scheme; }
}
