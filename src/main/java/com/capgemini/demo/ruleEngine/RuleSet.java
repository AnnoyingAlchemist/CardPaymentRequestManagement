package com.capgemini.demo.ruleEngine;
import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseSummary;

public interface RuleSet {
    RuleSuggestion evaluate(CaseSummary c);
}
