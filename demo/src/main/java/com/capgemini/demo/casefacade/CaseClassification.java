package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class CaseClassification {

    private String status;
    private String priority;
    private String recommendedNextAction;
    private LocalDateTime dueDate;

    public CaseClassification() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getRecommendedNextAction() { return recommendedNextAction; }
    public void setRecommendedNextAction(String recommendedNextAction) {
        this.recommendedNextAction = recommendedNextAction;
    }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
}
