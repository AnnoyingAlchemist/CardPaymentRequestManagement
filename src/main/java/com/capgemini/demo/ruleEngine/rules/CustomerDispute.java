package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.Priority;

import java.math.BigDecimal;

public class CustomerDispute implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();
        int lowMoneyThreshold = 100;

        if(c.getTransactionAmount().compareTo(BigDecimal.valueOf(lowMoneyThreshold)) > 0){
            return suggestion;
        }


        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("None");
        suggestion.setPriority(Priority.UNKNOWN);

        return suggestion;
    }
}
