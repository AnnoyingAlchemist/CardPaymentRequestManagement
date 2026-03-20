package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Action;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Chargeback implements RuleSet {

    @Override
    public RuleSuggestion evaluate(CaseSummary caseSummary) {
        RuleSuggestion suggestion = new RuleSuggestion();

        if(LocalDateTime.now().until(caseSummary.getDueDate(), ChronoUnit.DAYS) <= 2){
            suggestion.setPriority(Priority.CRITICAL);
            suggestion.setRecommendedNextAction(Action.ESCALATE.name());
            return suggestion;
        }
        int lowValueThreshold = 1000;
        int mediumValueThreshold = 5000;
        int highValueThreshold = 10000;
        int criticalValueThreshold = 50000;

        if(caseSummary.getTransactionAmount().compareTo(BigDecimal.valueOf(criticalValueThreshold)) > 0){
            suggestion.setPriority(Priority.CRITICAL);
            suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
            return suggestion;
        }
        if(caseSummary.getTransactionAmount().compareTo(BigDecimal.valueOf(highValueThreshold)) > 0){
            suggestion.setPriority(Priority.HIGH);
            suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
            return suggestion;
        }
        if(caseSummary.getTransactionAmount().compareTo(BigDecimal.valueOf(mediumValueThreshold)) > 0){
            suggestion.setPriority(Priority.MEDIUM);
            suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
            return suggestion;
        }
        if(caseSummary.getTransactionAmount().compareTo(BigDecimal.valueOf(lowValueThreshold)) < 0){
            suggestion.setPriority(Priority.LOW);
            suggestion.setRecommendedNextAction(Action.AUTO_CREDIT.name());
            return suggestion;
        }
/*
        switch(caseSummary.getScheme().toLowerCase()){
            case "capital one":
                suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
                suggestion.setPriority(Priority.UNKNOWN);
                break;
            case "visa":
                suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
                suggestion.setPriority(Priority.UNKNOWN);
                break;
            case "mastercard":
                suggestion.setRecommendedNextAction(Action.REVIEW_NORMAL.name());
                suggestion.setPriority(Priority.UNKNOWN);
                break;
            default:
                suggestion.setRecommendedNextAction(Action.MANUAL_EVALUATION.name());
                suggestion.setPriority(Priority.UNKNOWN);

        }

 */
        suggestion.setPriority(Priority.UNKNOWN);
        suggestion.setRecommendedNextAction("UNKNOWN");
        return suggestion;
    }

}
