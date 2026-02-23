package com.capgemini.demo.ruleEngine;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleSuggestion {
    private String priority;
    private String recommendedNextAction;
}
