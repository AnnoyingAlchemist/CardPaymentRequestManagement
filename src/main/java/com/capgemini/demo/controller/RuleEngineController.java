package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseSummary;
import com.capgemini.demo.ruleEngine.*;
import com.capgemini.demo.ruleEngine.rules.*;
import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.RuleEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rules")
public class RuleEngineController {
    private final RuleEngineService ruleService;
    private RuleEngine ruleEngine;

    public RuleEngineController(RuleEngineService service) {
        this.ruleService = service;
    }


    @PostMapping("/evaluate")
    public RuleSuggestion evalCase(@RequestBody CaseFacade c) {
        return ruleService.evalCase(new CaseSummary(c));
    }

    @PostMapping("/register-case")
    public String evaluateCase(@RequestBody CaseFacade c) {
        //caseService.getCase(c.getId());
        ruleEngine.evaluateCase(new CaseSummary(c));
        return "Case priority and recommendation";
    }
}
