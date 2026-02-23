package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.ruleEngine.RuleEngine;
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
        //RuleEngine.evaluate(c);
        return "Case priority and recommendation";
    }

    @PostMapping("/register-case")
    public String evaluateCase(@RequestBody CaseFacade c) {
        caseService.getCase(c.getId());
        ruleEngine.evaluateCase(c);
        return "Case priority and reccomendation";
    }
}
