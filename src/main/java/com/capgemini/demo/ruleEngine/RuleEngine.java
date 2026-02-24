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

    public void evaluateCase(CaseFacade c){
        ruleset.evaluate(c);
    }

}

