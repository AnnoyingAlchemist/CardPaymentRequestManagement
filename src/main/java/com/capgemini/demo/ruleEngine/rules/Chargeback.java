package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Chargeback implements RuleSet {

    @Override
    public RuleSuggestion evaluate(CaseSummary caseSummary) {
        RuleSuggestion suggestion = new RuleSuggestion();

        if(LocalDateTime.now().until(caseSummary.getDueDate(), ChronoUnit.DAYS) <= 2){
            suggestion.setPriority(Priority.CRITICAL);
            suggestion.setRecommendedNextAction("ESCALATE");
            return suggestion;
        }
        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("MANUAL_EVALUATION");
        suggestion.setPriority(Priority.UNKNOWN);

        return suggestion;
    }

}
