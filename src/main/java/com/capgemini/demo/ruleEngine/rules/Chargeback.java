package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.priority;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Chargeback implements RuleSet {

    @Override
    public RuleSuggestion evaluate(CaseSummary caseSummary) {
        RuleSuggestion suggestion = new RuleSuggestion();

        if(LocalDateTime.now().until(caseSummary.getDueDate(), ChronoUnit.DAYS) <= 2){
            suggestion.setPriority(priority.CRITICAL);
            suggestion.setRecommendedNextAction("ESCALATE");
            return suggestion;
        }
        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("MANUAL_EVALUATION");
        suggestion.setPriority(priority.UNKNOWN);

        return suggestion;
    }

}
