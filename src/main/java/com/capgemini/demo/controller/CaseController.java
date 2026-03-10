package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseStatusCode;
import com.capgemini.demo.casefacade.CaseTypeCode;
import com.capgemini.demo.casefacade.Role;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")
    public CaseFacade createCase(@RequestBody CaseFacade c) {
        return service.createCase(c);
    }
/*
    @PostMapping("/simple")
    @Operation(summary = "Creates a case based on key attributes")
    public CaseFacade createCaseSimple(@RequestParam String transactionId,
                                       @RequestParam CaseTypeCode caseType,
                                       @RequestParam BigDecimal transactionAmount,
                                       @RequestParam String currency,
                                       @RequestParam String cardToken,
                                       @RequestParam String createdBy) {
        CaseFacade c = new CaseFacade();
        c.getIdentifier().setPrimaryTransactionId(transactionId);
        c.getIdentifier().setCaseType(caseType.name());
        c.getTransaction().setTransactionAmount(transactionAmount);
        c.getTransaction().setTransactionCurrency(currency);
        c.getIdentifier().setCardToken(cardToken);
        c.getAssignment().setCreatedBy(createdBy);

        c.getClassification().setStatus(CaseStatusCode.OPEN.name());
        c.getAssignment().setCreatedAt(LocalDateTime.now());

        return service.createCase(c);
    }
*/


    public static final String CONTRACT_GET_CASES = """
    Contract: GET /cases with filters
    Request:
      GET /cases?status=OPEN&priority=HIGH&page=0&size=20
    Response:
      status: 200
      body: { "content": [...], "number":0, "size":20 }
    """;

    public static final String CONTRACT_INVALID_TRANSITION = """
    Contract: PUT /cases/{id}/status invalid transition
    Request:
      PUT /cases/10/status?newStatus=IN_REVIEW
    Response:
      status: 409
      body: { "error":"Conflict" }
    """;

    // ==============================
// BDD SCENARIO DEFINITIONS
// ==============================
    public static final String BDD_STATUS_TRANSITION = """
    Feature: Status transitions
      Scenario: Allowed transition OPEN -> IN_REVIEW
        Given a case exists with status "OPEN"
        When the client updates the case status to "IN_REVIEW"
        Then the response status should be 200
    """;

    public static final String BDD_SEARCH_FILTERS = """
    Feature: Searching cases
      Scenario: Filter by OPEN status
        Given cases exist with various statuses
        When the client searches for status "OPEN"
        Then only OPEN cases should be returned
    """;

    @GetMapping("/{id}")
    @Operation(summary = "Gets a case by id")
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")
    @PostAuthorize("returnObject.assignment.createdBy == authentication.name " +
            "|| hasRole('OPS_MANAGER') " +
            "|| hasRole('SYSTEM')")
    public CaseFacade getCase(@PathVariable Long id) {
        return service.getCase(id);
    }

    /**
     * GET /cases with optional filters + basic pagination
     * Filters: status, caseType, Priority, assignedTo, createdFrom, createdTo
     * Pagination: page (default 0), size (default 20), sorted .;by id desc
     */
    @GetMapping
    @Operation(summary = "List cases with optional filters and pagination.")
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")
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
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")    //@PostAuthorize("returnObject.assignment.createdBy == authentication.name " +
      //      "|| hasRole('OPS_MANAGER') " +
        //    "|| hasRole('SYSTEM')")
    public CaseFacade updateCase(
            @PathVariable Long id,
            @RequestBody CaseFacade updatedCase) {
        return service.updateCase(id, updatedCase);
    }

    @PutMapping("/{caseId}/status")
    @Operation(summary = "Updates the status of a case")
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")
    public CaseFacade updateStatus(
            @PathVariable Long caseId,
            @RequestParam String newStatus) {
        return service.updateStatus(caseId, newStatus);
    }

    @PutMapping("/{caseId}/assignee")
    @Operation(summary = "Updates who is assigned to a case")
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")
    public CaseFacade updateAssignee(
            @PathVariable Long caseId,
            @RequestParam String assignee) {
        return service.updateAssignee(caseId, assignee);
    }

    @GetMapping("/{caseId}/history")
    @Operation(summary = "Shows the history of a given case")
    @PreAuthorize("hasAnyRole('SYSTEM','FRAUD_ANALYST','OPS_MANAGER','AGENT','DISPUTE_ANALYST')")
    public List<CaseHistory> getCaseHistory(@PathVariable Long caseId) {
        return service.getCaseHistoryById(caseId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a case")
    @PreAuthorize("hasAnyRole('SYSTEM','OPS_MANAGER')")
    public void deleteCase(@PathVariable Long id) {
        service.deleteCase(id);
    }


}