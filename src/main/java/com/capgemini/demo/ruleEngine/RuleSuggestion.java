package com.capgemini.demo.ruleEngine;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleSuggestion {
    private priority priority;
    private String recommendedNextAction;
}
