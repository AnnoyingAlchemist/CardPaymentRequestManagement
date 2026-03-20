package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Action;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.Priority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Fraud implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();

        if(LocalDateTime.now().until(c.getDueDate(), ChronoUnit.DAYS) <= 2){
            suggestion.setPriority(Priority.CRITICAL);
            suggestion.setRecommendedNextAction(Action.ESCALATE.name());
            return suggestion;
        }
        int lowValueThreshold = 100;
        int mediumValueThreshold = 1000;
        int highValueThreshold = 4000;
        int criticalValueThreshold = 8000;

        if(c.getTransactionAmount().compareTo(BigDecimal.valueOf(criticalValueThreshold)) > 0){
            suggestion.setPriority(Priority.CRITICAL);
            suggestion.setRecommendedNextAction(Action.ESCALATE.name());
            return suggestion;
        }
        if(c.getTransactionAmount().compareTo(BigDecimal.valueOf(highValueThreshold)) > 0){
            suggestion.setPriority(Priority.HIGH);
            suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
            return suggestion;
        }
        if(c.getTransactionAmount().compareTo(BigDecimal.valueOf(mediumValueThreshold)) > 0){
            suggestion.setPriority(Priority.MEDIUM);
            suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
            return suggestion;
        }
        if(c.getTransactionAmount().compareTo(BigDecimal.valueOf(lowValueThreshold)) < 0){
            suggestion.setPriority(Priority.LOW);
            suggestion.setRecommendedNextAction(Action.AUTO_CREDIT.name());
            return suggestion;
        }

        suggestion.setRecommendedNextAction("None");
        suggestion.setPriority(Priority.UNKNOWN);

        return suggestion;
    }
}
