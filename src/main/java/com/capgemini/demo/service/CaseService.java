package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseAssignment;
import com.capgemini.demo.casefacade.CaseClassification;
import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseSummary;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.priority;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ADD
import org.springframework.web.server.ResponseStatusException; // <-- ADD

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional // <-- ADD: ensure save + unique constraint violations are handled within tx
public class CaseService {

    private final CaseRepository repository;

    public CaseService(CaseRepository repository) {
        this.repository = repository;
    }

    public CaseFacade createCase(CaseFacade c) {
        // BAD REQUEST for validation issues
        if (c.getIdentifier() == null ||
                c.getIdentifier().getCustomerId() == null ||
                c.getIdentifier().getCustomerId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID required"); // <-- CHANGED
        }

        if (c.getIdentifier().getCaseType() == null ||
                c.getIdentifier().getPrimaryTransactionId() == null ||
                c.getClassification() == null ||
                c.getClassification().getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CaseType, TransactionId and Status are required"); // <-- CHANGED
        }

        boolean exists = repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                c.getIdentifier().getCaseType(),
                c.getIdentifier().getPrimaryTransactionId(),
                c.getClassification().getStatus()
        );


        if (exists){
            // 409 CONFLICT for duplicate business rule
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A case already exists with the same CaseType, primaryTransactionId and status"
            ); // <-- CHANGED
        }

        c.setId(null);

        RuleEngineService ruleService = new RuleEngineService();
        RuleSuggestion suggestion = ruleService.evalCase(new CaseSummary(c));
        if(suggestion.getPriority() == priority.UNKNOWN){
            c.setRuleEvalFailed(true);
        }
        c.getClassification().setPriority(suggestion.getPriority().name());
        c.getClassification().setRecommendedNextAction(suggestion.getRecommendedNextAction());

        try {
            return repository.save(c);
        } catch (DataIntegrityViolationException dive) {
            // If DB-level unique constraint fires (added below)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate case violates unique constraint",
                    dive
            ); // <-- ADDED
        }
    }

    public CaseFacade getCase(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found") // <-- CHANGED
                );
    }

    public List<CaseFacade> getAllCases() {
        return repository.findAll();
    }

    public CaseFacade updateCase(Long id, CaseFacade updatedCase) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found") // <-- CHANGED
                );

        if (updatedCase.getRuleEvalFailed() != null) {
            existing.setRuleEvalFailed(updatedCase.getRuleEvalFailed());
        }
        if (updatedCase.getClassification() != null) {
            existing.setClassification(updatedCase.getClassification());
        }
        if (updatedCase.getAssignment() != null) {
            existing.setAssignment(updatedCase.getAssignment());
        }
        if (updatedCase.getIdentifier() != null) {
            existing.setIdentifier(updatedCase.getIdentifier());
        }
        if (updatedCase.getTransaction() != null) {
            existing.setTransaction(updatedCase.getTransaction());
        }
        if (updatedCase.getOutcome() != null) {
            existing.setOutcome(updatedCase.getOutcome());
        }

        try {
            return repository.save(existing);
        } catch (DataIntegrityViolationException dive) {
            // Handle constraint violations on update, too
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate case violates unique constraint",
                    dive
            ); // <-- ADDED
        }
    }

    public void deleteCase(Long id) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found") // <-- CHANGED
                );

        repository.deleteById(id);
    }

    public CaseFacade updateStatus(Long caseId, String newStatus) {
        CaseFacade existing = repository.findById(caseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Case with ID " + caseId + " not found"));

        if (newStatus == null || newStatus.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status must not be blank");
        }

        if (existing.getClassification() == null) {
            existing.setClassification(new CaseClassification());
        }

        existing.getClassification().setStatus(newStatus);

        return repository.save(existing);
    }

    public CaseFacade updateAssignee(Long caseId, String assignee) {
        CaseFacade existing = repository.findById(caseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Case with ID " + caseId + " not found"));

        if (assignee == null || assignee.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Assignee must not be blank");
        }

        if (existing.getAssignment() == null) {
            existing.setAssignment(new CaseAssignment());
            existing.getAssignment().setCreatedAt(LocalDateTime.now());
        }

        existing.getAssignment().setAssignedTo(assignee);
        existing.getAssignment().setUpdatedAt(LocalDateTime.now());

        return repository.save(existing);
    }

    public List<String> getCaseHistory(Long caseId) {
        CaseFacade existing = repository.findById(caseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Case with ID " + caseId + " not found"));

        List<String> history = new ArrayList<>();

        if (existing.getAssignment() != null) {
            if (existing.getAssignment().getAssignedTo() != null)
                history.add("Assigned to: " + existing.getAssignment().getAssignedTo());
            if (existing.getAssignment().getCreatedAt() != null)
                history.add("Created at: " + existing.getAssignment().getCreatedAt());
            if (existing.getAssignment().getUpdatedAt() != null)
                history.add("Updated at: " + existing.getAssignment().getUpdatedAt());
            if (existing.getAssignment().getResolvedAt() != null)
                history.add("Resolved at: " + existing.getAssignment().getResolvedAt());
        }

        if (existing.getClassification() != null) {
            if (existing.getClassification().getStatus() != null)
                history.add("Status: " + existing.getClassification().getStatus());
            if (existing.getClassification().getPriority() != null)
                history.add("Priority: " + existing.getClassification().getPriority());
            if (existing.getClassification().getDueDate() != null)
                history.add("Due date: " + existing.getClassification().getDueDate());
            if (existing.getClassification().getRecommendedNextAction() != null)
                history.add("Recommended next action: " + existing.getClassification().getRecommendedNextAction());
        }

        if (existing.getOutcome() != null) {
            if (existing.getOutcome().getResolution() != null)
                history.add("Resolution: " + existing.getOutcome().getResolution());
            if (existing.getOutcome().getResolutionNotes() != null)
                history.add("Resolution notes: " + existing.getOutcome().getResolutionNotes());
        }

        return history;
    }
}
