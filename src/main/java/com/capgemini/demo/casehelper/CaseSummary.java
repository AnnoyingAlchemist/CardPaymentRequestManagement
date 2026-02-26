package com.capgemini.demo.casehelper;

import com.capgemini.demo.casefacade.CaseClassification;
import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseIdentifier;
import com.capgemini.demo.casefacade.CaseTransaction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CaseSummary {
    //Note: Should not be a part of the CaseFacade
    //Meant primarily for use in RuleEngineService
    private String caseType;
    private String transactionAmount;
    private String transactionCurrency;
    private String transactionDateTime;
    private String merchantCategoryCode;
    private String scheme;
    private String customerId;
    private String cardNumberMasked;
    private String cardToken;
    private String primaryTransactionId;
    private LocalDateTime dueDate;
    private String transactionCountry;

    public CaseSummary(CaseFacade fullCase) {
        this.transactionAmount = fullCase.getTransaction().getTransactionAmount();
        this.transactionCurrency = fullCase.getTransaction().getTransactionCurrency();
        this.transactionDateTime = fullCase.getTransaction().getTransactionDateTime();
        this.merchantCategoryCode = fullCase.getTransaction().getMerchantCategoryCode();
        this.scheme = fullCase.getTransaction().getScheme();
        this.transactionCountry = fullCase.getTransaction().getTransactionCountry();

        this.caseType = fullCase.getIdentifier().getCaseType();
        this.customerId = fullCase.getIdentifier().getCustomerId();
        this.cardNumberMasked = fullCase.getIdentifier().getCustomerId();
        this.cardToken = fullCase.getIdentifier().getCardToken();
        this.primaryTransactionId = fullCase.getIdentifier().getPrimaryTransactionId();

        this.dueDate = fullCase.getClassification().getDueDate();
    }

    public CaseSummary(CaseTransaction transaction,
                       CaseIdentifier identifier,
                       CaseClassification classification) {
        this.transactionAmount = transaction.getTransactionAmount();
        this.transactionCurrency = transaction.getTransactionCurrency();
        this.transactionDateTime = transaction.getTransactionDateTime();
        this.merchantCategoryCode = transaction.getMerchantCategoryCode();
        this.scheme = transaction.getScheme();
        this.transactionCountry = transaction.getTransactionCountry();

        this.caseType = identifier.getCaseType();
        this.customerId = identifier.getCustomerId();
        this.cardNumberMasked = identifier.getCustomerId();
        this.cardToken = identifier.getCardToken();
        this.primaryTransactionId = identifier.getPrimaryTransactionId();

        this.dueDate = classification.getDueDate();
    }
}
