package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseSummary;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.priority;

public class CardStatus implements RuleSet {
    @Override
    public RuleSuggestion evaluate(CaseSummary c) {
        RuleSuggestion suggestion = new RuleSuggestion();
        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("None");
        suggestion.setPriority(priority.UNKNOWN);

        return suggestion;
    }
}
