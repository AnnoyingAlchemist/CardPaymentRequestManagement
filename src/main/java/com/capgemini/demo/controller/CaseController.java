package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;               // VERSIONING: ADDED
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;                      // VERSIONING: ADDED
import org.springframework.http.ResponseEntity;               // VERSIONING: ADDED
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;                                         // VERSIONING: ADDED

@RestController
@RequestMapping(
        value = {"/cases", "/v1/cases"},                     // VERSIONING: ADDED (legacy + v1)
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                "application/vnd.cardops.v1+json",           // VERSIONING: ADDED
                "application/vnd.cardops.v2+json"            // VERSIONING: ADDED
        }
)
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    // VERSIONING: ADDED — helper to attach deprecation headers on legacy (no /v1 or /v2 prefix)
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

    @PostMapping
    @Operation(summary = "Creates a case")
    public ResponseEntity<CaseFacade> createCase(@RequestBody CaseFacade c, HttpServletRequest req) { // VERSIONING: CHANGED
        return maybeDeprecate(req, service.createCase(c));                                            // VERSIONING: ADDED
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a case by id (legacy & v1)")
    public ResponseEntity<CaseFacade> getCase(@PathVariable Long id, HttpServletRequest req) {       // VERSIONING: CHANGED
        return maybeDeprecate(req, service.getCase(id));                                             // VERSIONING: ADDED
    }

    /**
     * GET /cases (legacy & v1) with optional filters + pagination.
     */
    @GetMapping
    @Operation(summary = "List cases with optional filters and pagination. (legacy & v1)")
    public ResponseEntity<Page<CaseFacade>> searchCases(                                             // VERSIONING: CHANGED
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
                service.searchCases(status, caseType, priority, assignedTo, createdFrom, createdTo, pageable)); // VERSIONING: ADDED
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a case by id (legacy & v1)")
    public ResponseEntity<CaseFacade> updateCase(
            @PathVariable Long id,
            @RequestBody CaseFacade updatedCase,
            HttpServletRequest req) {                                                                // VERSIONING: CHANGED
        return maybeDeprecate(req, service.updateCase(id, updatedCase));                             // VERSIONING: ADDED
    }

    @PutMapping("/{caseId}/status")
    @Operation(summary = "Updates the status of a case (legacy & v1)")
    public ResponseEntity<CaseFacade> updateStatus(
            @PathVariable Long caseId,
            @RequestParam String newStatus,
            HttpServletRequest req) {                                                                // VERSIONING: CHANGED
        return maybeDeprecate(req, service.updateStatus(caseId, newStatus));                         // VERSIONING: ADDED
    }

    // ---------------------------
    // V2: stricter transitions + optional comment
    // ---------------------------
    @PutMapping("/v2/cases/{caseId}/status")                                                         // VERSIONING: ADDED
    @Operation(summary = "V2: Updates the status with stricter transitions (e.g., OPEN→CLOSED disallowed directly)")
    public ResponseEntity<CaseFacade> updateStatusV2(                                                // VERSIONING: ADDED
                                                                                                     @PathVariable Long caseId,
                                                                                                     @RequestParam String newStatus,
                                                                                                     @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(service.updateStatusV2(caseId, newStatus, comment));
    }

    @PutMapping("/{caseId}/assignee")
    @Operation(summary = "Updates who is assigned to a case (legacy & v1)")
    public ResponseEntity<CaseFacade> updateAssignee(
            @PathVariable Long caseId,
            @RequestParam String assignee,
            HttpServletRequest req) {                                                                // VERSIONING: CHANGED
        return maybeDeprecate(req, service.updateAssignee(caseId, assignee));                        // VERSIONING: ADDED
    }

    @GetMapping("/{caseId}/history")
    @Operation(summary = "Shows the history of a given case (legacy & v1)")
    public ResponseEntity<List<CaseHistory>> getCaseHistory(
            @PathVariable Long caseId, HttpServletRequest req) {                                     // VERSIONING: CHANGED
        return maybeDeprecate(req, service.getCaseHistoryById(caseId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a case (legacy & v1)")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id, HttpServletRequest req) {          // VERSIONING: CHANGED
        service.deleteCase(id);
        return maybeDeprecate(req, (Void) null);
    }

    // ---------------------------
    // V2: trimmed case view (DTO-less)
    // ---------------------------
    @GetMapping("/v2/cases/{id}")                                                                    // VERSIONING: ADDED
    @Operation(summary = "V2: Trimmed case response (id, status, priority, assignee, caseType, primaryTransactionId)")
    public Map<String, Object> getCaseV2(@PathVariable Long id) {                                    // VERSIONING: ADDED
        CaseFacade c = service.getCase(id);
        return Map.of(
                "id", c.getId(),
                "status", c.getClassification() != null ? c.getClassification().getStatus() : null,
                "priority", c.getClassification() != null ? c.getClassification().getPriority() : null,
                "assignee", c.getAssignment() != null ? c.getAssignment().getAssignedTo() : null,
                "caseType", c.getIdentifier() != null ? c.getIdentifier().getCaseType() : null,
                "primaryTransactionId", c.getIdentifier() != null ? c.getIdentifier().getPrimaryTransactionId() : null
        );
    }

    // ---------------------------
    // V2: multi-status search (comma-separated)
    // ---------------------------
    @GetMapping("/v2/cases")                                                                         // VERSIONING: ADDED
    @Operation(summary = "V2: Search cases with multi-status filter (e.g., statuses=OPEN,IN_REVIEW)")
    public Page<CaseFacade> searchCasesV2(                                                           // VERSIONING: ADDED
                                                                                                     @RequestParam(required = false) String statuses,   // "OPEN,IN_REVIEW"
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