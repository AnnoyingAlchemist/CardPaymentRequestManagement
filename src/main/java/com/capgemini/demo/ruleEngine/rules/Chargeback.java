package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;

public class Chargeback implements RuleSet {

    @Override
    public RuleSuggestion evaluate(CaseFacade c) {
        return new RuleSuggestion();
    }

}
