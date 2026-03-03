package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/cases")
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Creates a case")
    public CaseFacade createCase(@RequestBody CaseFacade c) {
        return service.createCase(c);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a case by id")
    public CaseFacade getCase(@PathVariable Long id) {
        return service.getCase(id);
    }

    /**
     * GET /cases with optional filters + basic pagination
     * Filters: status, caseType, priority, assignedTo, createdFrom, createdTo
     * Pagination: page (default 0), size (default 20), sorted by id desc
     */
    @GetMapping
    @Operation(summary = "List cases with optional filters and pagination.")
    public Page<CaseFacade> searchCases(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String caseType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return service.searchCases(status, caseType, priority, assignedTo, createdFrom, createdTo, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a case by id")
    public CaseFacade updateCase(
            @PathVariable Long id,
            @RequestBody CaseFacade updatedCase) {
        return service.updateCase(id, updatedCase);
    }

    @PutMapping("/{caseId}/status")
    @Operation(summary = "Updates the status of a case")
    public CaseFacade updateStatus(
            @PathVariable Long caseId,
            @RequestParam String newStatus) {
        return service.updateStatus(caseId, newStatus);
    }

    @PutMapping("/{caseId}/assignee")
    @Operation(summary = "Updates who is assigned to a case")
    public CaseFacade updateAssignee(
            @PathVariable Long caseId,
            @RequestParam String assignee) {
        return service.updateAssignee(caseId, assignee);
    }

    @GetMapping("/{caseId}/history")
    @Operation(summary = "Shows the history of a given case")
    public List<CaseHistory> getCaseHistory(@PathVariable Long caseId) {
        return service.getCaseHistoryById(caseId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a case")
    public void deleteCase(@PathVariable Long id) {
        service.deleteCase(id);
    }
}