package com.capgemini.demo.casefacade;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaseFacade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean ruleEvalFailed;

    @Embedded
    private CaseClassification classification;

    @Embedded
    private CaseAssignment assignment;

    @Embedded
    private CaseIdentifier identifier;

    @Embedded
    private CaseTransaction transaction;

    @Embedded
    private CaseOutcome outcome;
}
