package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.service.RuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = {"/rules", "/v1/rules"},
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                "application/vnd.cardops.v1+json",
                "application/vnd.cardops.v2+json"
        }
)
public class RuleEngineController {
    private final RuleEngineService ruleService;

    public RuleEngineController(RuleEngineService service) {
        this.ruleService = service;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate a case based on predefined rules")
    @PreAuthorize("hasAnyRole('FRAUD_ANALYST','DISPUTE_ANALYST','OPS_MANAGER','SYSTEM')")
    public RuleSuggestion evalCase(@RequestBody CaseFacade c) {
        return ruleService.evalCase(new CaseSummary(c));
    }
}