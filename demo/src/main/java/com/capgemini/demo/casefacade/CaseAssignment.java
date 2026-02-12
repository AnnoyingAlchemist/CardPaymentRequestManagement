package com.capgemini.demo.casefacade;

import java.time.LocalDateTime;


import jakarta.persistence.Embeddable;

@Embeddable
public class CaseAssignment {

    private String createdBy;
    private String assignedTo;
    private String createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // getters/setters
}
