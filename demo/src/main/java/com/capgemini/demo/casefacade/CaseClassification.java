package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class CaseClassification {

    private String status;
    private String priority;
    private String recommendedNextAction;
    private LocalDateTime dueDate;

    // getters/setters
}
