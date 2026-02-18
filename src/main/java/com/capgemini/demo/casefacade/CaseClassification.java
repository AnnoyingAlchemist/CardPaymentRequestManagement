package com.capgemini.demo.casefacade;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CaseClassification {

    private String status;
    private String priority;
    private String recommendedNextAction;
    private LocalDateTime dueDate;
}
