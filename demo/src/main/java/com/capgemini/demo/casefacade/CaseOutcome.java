package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;

@Embeddable
public class CaseOutcome {

    private String resolution;
    private String resolutionNotes;

    public CaseOutcome() {}

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
