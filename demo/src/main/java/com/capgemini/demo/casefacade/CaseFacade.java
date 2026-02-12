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

    // getters/setters
}
