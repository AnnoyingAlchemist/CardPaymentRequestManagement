package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.ruleEngine.*;
import com.capgemini.demo.ruleEngine.rules.*;
import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.RuleEngineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rules")
public class RuleEngineController {
    private final RuleEngineService ruleService;
    private final CaseService caseService;
    private RuleEngine ruleEngine;

    public RuleEngineController(RuleEngineService service, CaseService caseService) {
        this.ruleService = service;
        this.caseService = caseService;
    }


    @PostMapping("/evaluate")
    public String evalCase(@RequestBody CaseFacade c) {
        caseService.getCase(c.getId());
        switch(c.getIdentifier().getCaseType()){
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

            ruleEngine.evaluateCase(c);

        return "Case priority and recommendation";
    }

    @PostMapping("/register-case")
    public String evaluateCase(@RequestBody CaseFacade c) {
        caseService.getCase(c.getId());
        ruleEngine.evaluateCase(c);
        return "Case priority and recommendation";
    }
}
