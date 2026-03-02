package com.capgemini.demo.service;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.RuleEngine;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.rules.*;
import org.springframework.stereotype.Service;

@Service
public class RuleEngineService {
    //private CaseService caseService;
    //private RuleEngine ruleEngine = new RuleEngine(new UnknownRule());


    public RuleEngineService() {
    }

    public RuleSuggestion evalCase(CaseSummary c){
        //caseService.getCase(c.getId());
        RuleEngine ruleEngine = new RuleEngine(new UnknownRule());
        switch(c.getCaseType()){
            case "FRAUD_INVESTIGATION":
                ruleEngine.setRuleset(new Fraud());
                break;
            case "CUSTOMER_DISPUTE":
                ruleEngine.setRuleset(new CustomerDispute());
                break;
            case "CHARGEBACK":
                ruleEngine.setRuleset(new Chargeback());
                break;
            case "CARD_STATUS":
                ruleEngine.setRuleset(new CardStatus());
                break;
            default:
                ruleEngine.setRuleset(new UnknownRule());
                //UnknownRule should return unknown priority
                //set rule_eval_failed flag to true
                break;
        }
        RuleSuggestion suggestion = new RuleSuggestion();
        suggestion = ruleEngine.evaluateCase(c);
        //TODO: Modify case to update suggestion

        return suggestion;
    }
}
