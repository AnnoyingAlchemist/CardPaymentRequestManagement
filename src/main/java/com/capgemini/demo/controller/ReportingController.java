package com.capgemini.demo.controller;

import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;             // VERSIONING: ADDED
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(
        value = {"/reports", "/v1/reports"},       // VERSIONING: ADDED
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                "application/vnd.cardops.v1+json", // VERSIONING: ADDED
                "application/vnd.cardops.v2+json"  // VERSIONING: ADDED
        }
)
public class ReportingController {
    private final ReportingService reportingService;
    private final CaseService caseService;

    public ReportingController(ReportingService reportingService, CaseService caseService) {
        this.reportingService = reportingService;
        this.caseService = caseService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Gives a summary of cases by type/status/resolution.")
    //@RequestParam(required = false)
    public List<Map<String, Map<String,Integer>>> getSummaryReport(){
        return reportingService.getCaseSummaryReport(caseService.getAllCases());
    }

    @GetMapping("/backlog")
    @Operation(summary = "Backlog & SLA risk.")
    public List<Map<String,Integer>> getBacklogReport(){
        return reportingService.getCaseBacklogReport(caseService.getAllCases());
    }

    @GetMapping("/aging")
    @Operation(summary = "Shows aging of open cases.")
    public Map<String, Integer> getAgingReport(){
        return reportingService.getCaseAgingReport(caseService.getAllCases());
    }
}