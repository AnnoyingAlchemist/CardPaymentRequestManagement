package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- ADDED
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(
        value = {"/cases", "/v1/cases"},
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                "application/vnd.cardops.v1+json",
                "application/vnd.cardops.v2+json"
        }
)
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    // Helper to attach deprecation headers on legacy (no /v1 or /v2 prefix)
    private <T> ResponseEntity<T> maybeDeprecate(HttpServletRequest req, T body) {
        String uri = req.getRequestURI();
        boolean legacy = !(uri.startsWith("/v1/") || uri.startsWith("/v2/"));
        if (legacy) {
            return ResponseEntity.ok()
                    .header("Deprecation", "true")
                    .header("Sunset", "Wed, 01 Jul 2026 00:00:00 GMT")
                    .header("Link", "</v1" + uri + ">; rel=\"successor-version\"")
                    .body(body);
        }
        return ResponseEntity.ok(body);
    }

    // CREATE
    @PostMapping
    @Operation(summary = "Creates a case")
    @PreAuthorize("hasAnyRole('AGENT','OPS_MANAGER')")
    public ResponseEntity<CaseFacade> createCase(@RequestBody CaseFacade c, HttpServletRequest req) {
        return maybeDeprecate(req, service.createCase(c));
    }

    // READ (by id)
    @GetMapping("/{id}")
    @Operation(summary = "Gets a case by id (legacy & v1)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaseFacade> getCase(@PathVariable Long id, HttpServletRequest req) {
        return maybeDeprecate(req, service.getCase(id));
    }

    // SEARCH (legacy & v1)
    @GetMapping
    @Operation(summary = "List cases with optional filters and pagination. (legacy & v1)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CaseFacade>> searchCases(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String caseType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return maybeDeprecate(req,
                service.searchCases(status, caseType, priority, assignedTo, createdFrom, createdTo, pageable));
    }

    // UPDATE (general)
    @PutMapping("/{id}")
    @Operation(summary = "Updates a case by id (legacy & v1)")
    @PreAuthorize("hasAnyRole('AGENT','OPS_MANAGER')")
    public ResponseEntity<CaseFacade> updateCase(
            @PathVariable Long id,
            @RequestBody CaseFacade updatedCase,
            HttpServletRequest req) {
        return maybeDeprecate(req, service.updateCase(id, updatedCase));
    }

    // UPDATE STATUS (v1)
    @PutMapping("/{caseId}/status")
    @Operation(summary = "Updates the status of a case (legacy & v1)")
    @PreAuthorize("hasAnyRole('FRAUD_ANALYST','DISPUTE_ANALYST','OPS_MANAGER','AGENT','SYSTEM')")
    public ResponseEntity<CaseFacade> updateStatus(
            @PathVariable Long caseId,
            @RequestParam String newStatus,
            HttpServletRequest req) {
        return maybeDeprecate(req, service.updateStatus(caseId, newStatus));
    }

    // UPDATE STATUS (v2, stricter transitions + optional comment)
    @PutMapping("/v2/cases/{caseId}/status")
    @Operation(summary = "V2: Updates the status with stricter transitions (e.g., OPEN→CLOSED disallowed directly)")
    @PreAuthorize("hasAnyRole('FRAUD_ANALYST','DISPUTE_ANALYST','OPS_MANAGER','AGENT','SYSTEM')")
    public ResponseEntity<Map<String, Object>> updateStatusV2(
            @PathVariable Long caseId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String comment) {
        var updated = service.updateStatusV2(caseId, newStatus, comment);
        boolean transitionedToClosure = "CLOSED".equals(updated.getClassification().getStatus());
        return ResponseEntity.ok(
                Map.of(
                        "id", updated.getId(),
                        "status", updated.getClassification().getStatus(),
                        "priority", updated.getClassification().getPriority(),
                        "caseType", updated.getIdentifier().getCaseType(),
                        "transitionedToClosure", transitionedToClosure
                )
        );
    }

    // UPDATE ASSIGNEE
    @PutMapping("/{caseId}/assignee")
    @Operation(summary = "Updates who is assigned to a case (legacy & v1)")
    @PreAuthorize("hasRole('OPS_MANAGER')")
    public ResponseEntity<CaseFacade> updateAssignee(
            @PathVariable Long caseId,
            @RequestParam String assignee,
            HttpServletRequest req) {
        return maybeDeprecate(req, service.updateAssignee(caseId, assignee));
    }

    // HISTORY
    @GetMapping("/{caseId}/history")
    @Operation(summary = "Shows the history of a given case (legacy & v1)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CaseHistory>> getCaseHistory(
            @PathVariable Long caseId, HttpServletRequest req) {
        return maybeDeprecate(req, service.getCaseHistoryById(caseId));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a case (legacy & v1)")
    @PreAuthorize("hasRole('OPS_MANAGER')")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id, HttpServletRequest req) {
        service.deleteCase(id);
        return maybeDeprecate(req, (Void) null);
    }

    // V2: trimmed case view + enriched fields
    @GetMapping("/v2/cases/{id}")
    @Operation(summary = "V2: Trimmed case response with extended fields")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getCaseV2(@PathVariable Long id) {

        CaseFacade c = service.getCase(id);

        long caseAgeDays = c.getAssignment().getCreatedAt() != null
                ? java.time.Duration.between(c.getAssignment().getCreatedAt(), LocalDateTime.now()).toDays()
                : 0;

        boolean isEscalated = c.getClassification() != null &&
                ("HIGH".equalsIgnoreCase(c.getClassification().getPriority()) ||
                        "CRITICAL".equalsIgnoreCase(c.getClassification().getPriority()));

        String statusDisplay = switch (c.getClassification().getStatus()) {
            case "OPEN" -> "Open – Awaiting Review";
            case "IN_REVIEW" -> "Under Analyst Review";
            case "PENDING_CUSTOMER" -> "Waiting on Customer";
            case "PENDING_PARTNER" -> "Waiting on Partner";
            case "CLOSED" -> "Closed";
            default -> "Unknown";
        };

        String nextSteps = c.getClassification().getRecommendedNextAction();

        return Map.of(
                "id", c.getId(),
                "status", c.getClassification().getStatus(),
                "priority", c.getClassification().getPriority(),
                "assignee", c.getAssignment().getAssignedTo(),
                "caseType", c.getIdentifier().getCaseType(),
                "primaryTransactionId", c.getIdentifier().getPrimaryTransactionId(),
                "caseAgeDays", caseAgeDays,
                "isEscalated", isEscalated,
                "statusDisplay", statusDisplay,
                "nextSteps", nextSteps
        );
    }

    // V2: multi-status search with enriched fields
    @GetMapping("/v2/cases")
    @Operation(summary = "V2: Search cases with multi-status filter (e.g., statuses=OPEN,IN_REVIEW)")
    @PreAuthorize("isAuthenticated()")
    public Page<CaseFacade> searchCasesV2(
            @RequestParam(required = false) String statuses,
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
        return service.searchCasesV2(statuses, caseType, priority, assignedTo, createdFrom, createdTo, pageable);
    }
}