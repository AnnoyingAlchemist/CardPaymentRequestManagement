package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Action;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.Priority;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CardStatus implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();

        if(LocalDateTime.now().until(c.getDueDate(), ChronoUnit.DAYS) <= 2){
            suggestion.setPriority(Priority.CRITICAL);
            suggestion.setRecommendedNextAction(Action.ESCALATE.name());
            return suggestion;
        }


        suggestion.setRecommendedNextAction("None");
        suggestion.setPriority(Priority.UNKNOWN);

        return suggestion;
    }
}
