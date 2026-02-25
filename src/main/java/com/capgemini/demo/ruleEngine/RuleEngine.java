package com.capgemini.demo.ruleEngine;

import com.capgemini.demo.casefacade.CaseFacade;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleEngine {
    private RuleSet ruleset;

    public RuleEngine(RuleSet r){
        this.ruleset = r;
    }

    public RuleSuggestion evaluateCase(CaseFacade c){
        return ruleset.evaluate(c);
    }

}

