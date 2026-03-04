package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.Priority;

public class Fraud implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();
        //TODO
        suggestion.setRecommendedNextAction("None");
        suggestion.setPriority(Priority.UNKNOWN);

        return suggestion;
    }
}
