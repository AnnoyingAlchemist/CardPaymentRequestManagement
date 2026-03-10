package com.capgemini.demo.controller;

import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportingController {
    private final ReportingService reportingService;
    private final CaseService caseService;

    public ReportingController(ReportingService reportingService, CaseService caseService) {
        this.reportingService = reportingService;
        this.caseService = caseService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Gives a summary of cases by type/status/resolution.")
    @PreAuthorize("hasAnyRole('SYSTEM','OPS_MANAGER')")
    public String getSummaryReport(){
        return reportingService.getCaseSummaryReport(caseService.getAllCases());
    }

    @GetMapping("/backlog")
    @Operation(summary = "Backlog & SLA risk.")
    @PreAuthorize("hasAnyRole('SYSTEM','OPS_MANAGER')")
    public String getBacklogReport(){
        return reportingService.getCaseBacklogReport(caseService.getAllCases());
    }

    @GetMapping("/aging")
    @Operation(summary = "Shows aging of open cases.")
    @PreAuthorize("hasAnyRole('SYSTEM','OPS_MANAGER')")
    public String getAgingReport(){
        return reportingService.getCaseAgingReport(caseService.getAllCases());
    }
}
