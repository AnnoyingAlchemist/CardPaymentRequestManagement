package com.capgemini.demo.ruleEngine;
import com.capgemini.demo.casehelper.CaseSummary;

public interface RuleSet {
    RuleSuggestion evaluate(CaseSummary c);
}
