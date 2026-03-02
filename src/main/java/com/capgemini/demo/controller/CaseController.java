package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.service.CaseService;
//import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Gets a case from the database by its unique id")
    public CaseFacade getCase(@PathVariable Long id) {
        return service.getCase(id);
    }

    @GetMapping
    @Operation(summary = "Returns a list of all cases in the database.")
    public List<CaseFacade> all() {
        return service.getAllCases();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a case - selected by its id")
    public CaseFacade updateCase(
            @PathVariable Long id,
            @RequestBody CaseFacade updatedCase) {
        return service.updateCase(id, updatedCase);
    }

    @PutMapping("/{caseId}/status")
    @Operation(summary = "Updates the status of a case. Should only allow certain state transitions")
    public CaseFacade updateStatus(
            @PathVariable Long caseId,
            @RequestParam String newStatus) {
        return service.updateStatus(caseId, newStatus);
    }

    @PutMapping("/{caseId}/assignee")
    @Operation(summary = "Updates who is assigned to a case.")
    public CaseFacade updateAssignee(
            @PathVariable Long caseId,
            @RequestParam String assignee) {
        return service.updateAssignee(caseId, assignee);
    }

    @GetMapping("/{caseId}/history")
    @Operation(summary = "Shows the history of a given case.")
    public List<CaseHistory> getCaseHistory(@PathVariable Long caseId) {
        return service.getCaseHistoryById(caseId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a case")
    public void deleteCase(@PathVariable Long id) {
        service.deleteCase(id);
    }
}
