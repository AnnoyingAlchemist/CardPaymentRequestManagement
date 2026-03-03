package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;

public class UnknownRule implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();
        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("MANUAL_REVIEW");
        suggestion.setPriority(Priority.UNKNOWN);

        return suggestion;
    }
}
