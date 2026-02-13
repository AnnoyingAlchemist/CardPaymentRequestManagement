package com.capgemini.demo.casefacade;

import jakarta.persistence.*;
import jakarta.validation.Valid;

@Entity
public class CaseFacade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean ruleEvalFailed;

    @Embedded @Valid
    private CaseClassification classification;

    @Embedded @Valid
    private CaseAssignment assignment;

    @Embedded @Valid
    private CaseIdentifier identifier;

    @Embedded @Valid
    private CaseTransaction transaction;

    @Embedded @Valid
    private CaseOutcome outcome;

    public CaseFacade() {}

    public Long getId() { return id; }

    public Boolean getRuleEvalFailed() { return ruleEvalFailed; }
    public void setRuleEvalFailed(Boolean ruleEvalFailed) {
        this.ruleEvalFailed = ruleEvalFailed;
    }

    public CaseClassification getClassification() { return classification; }
    public void setClassification(CaseClassification classification) {
        this.classification = classification;
    }

    public CaseAssignment getAssignment() { return assignment; }
    public void setAssignment(CaseAssignment assignment) {
        this.assignment = assignment;
    }

    public CaseIdentifier getIdentifier() { return identifier; }
    public void setIdentifier(CaseIdentifier identifier) {
        this.identifier = identifier;
    }

    public CaseTransaction getTransaction() { return transaction; }
    public void setTransaction(CaseTransaction transaction) {
        this.transaction = transaction;
    }

    public CaseOutcome getOutcome() { return outcome; }
    public void setOutcome(CaseOutcome outcome) {
        this.outcome = outcome;
    }
}
