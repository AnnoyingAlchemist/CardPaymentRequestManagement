package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;

@Embeddable
public class CaseOutcome {

    private String resolution;
    private String resolutionNotes;

    // getters/setters
}
