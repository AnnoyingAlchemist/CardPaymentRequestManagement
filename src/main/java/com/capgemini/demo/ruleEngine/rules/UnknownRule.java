package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.priority;

public class UnknownRule implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseFacade c) {
        RuleSuggestion suggestion = new RuleSuggestion();
        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("MANUAL_REVIEW");
        suggestion.setPriority(priority.UNKNOWN);

        return suggestion;
    }
}
