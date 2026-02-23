package com.capgemini.demo.ruleEngine;
import com.capgemini.demo.casefacade.CaseFacade;

public interface RuleSet {
    RuleSuggestion evaluate(CaseFacade c);
}
