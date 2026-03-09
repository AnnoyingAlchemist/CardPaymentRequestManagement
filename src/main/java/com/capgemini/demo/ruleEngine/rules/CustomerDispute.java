package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Action;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.Priority;

import java.math.BigDecimal;

public class CustomerDispute implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();
        int lowValueThreshold = 100;

        //TODO: implement more cases
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
