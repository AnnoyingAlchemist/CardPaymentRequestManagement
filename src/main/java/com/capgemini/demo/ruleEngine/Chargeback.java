package com.capgemini.demo.ruleEngine;

import com.capgemini.demo.casefacade.CaseFacade;

public class Chargeback implements RuleSet {

    @Override
    public RuleSuggestion evaluate(CaseFacade c) {
        return new RuleSuggestion();
    }

}
