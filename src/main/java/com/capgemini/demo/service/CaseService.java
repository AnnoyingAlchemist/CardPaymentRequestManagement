package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseAssignment;
import com.capgemini.demo.casefacade.CaseClassification;
import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.priority;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CaseService {

    private final CaseRepository repository;
    private final CaseHistoryRepository historyRepository;

    public CaseService(CaseRepository repository, CaseHistoryRepository historyRepo) {
        this.repository = repository;
        this.historyRepository = historyRepo;
    }

    // ---------------------------
    // Allowed states & transitions
    // ---------------------------
    private static final java.util.Set<String> ALLOWED_STATES = java.util.Set.of(
            "OPEN", "IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER",
            "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"
    );

    private static final java.util.Map<String, java.util.Set<String>> ALLOWED_TRANSITIONS;
    static {
        java.util.Map<String, java.util.Set<String>> m = new java.util.HashMap<>();
        m.put("OPEN", java.util.Set.of("IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER", "CLOSED"));
        m.put("IN_REVIEW", java.util.Set.of("PENDING_CUSTOMER", "PENDING_PARTNER",
                "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"));
        m.put("PENDING_CUSTOMER", java.util.Set.of("IN_REVIEW", "CLOSED"));
        m.put("PENDING_PARTNER", java.util.Set.of("IN_REVIEW", "CLOSED"));
        m.put("RESOLVED_CUSTOMER_FAVOUR", java.util.Set.of("CLOSED"));
        m.put("RESOLVED_BANK_FAVOUR", java.util.Set.of("CLOSED"));
        m.put("CLOSED", java.util.Set.of()); // terminal
        ALLOWED_TRANSITIONS = java.util.Collections.unmodifiableMap(m);
    }

    public CaseFacade createCase(CaseFacade c) {
        if (c.getIdentifier() == null ||
                c.getIdentifier().getCustomerId() == null ||
                c.getIdentifier().getCustomerId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID required");
        }

        if (c.getIdentifier().getCaseType() == null ||
                c.getIdentifier().getPrimaryTransactionId() == null ||
                c.getClassification() == null ||
                c.getClassification().getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CaseType, TransactionId and Status are required");
        }

        boolean exists = repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                c.getIdentifier().getCaseType(),
                c.getIdentifier().getPrimaryTransactionId(),
                c.getClassification().getStatus()
        );

        if (exists){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A case already exists with the same CaseType, primaryTransactionId and status"
            );
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
            addCaseToHistory(c, "created", c.getClassification().getStatus());
            return repository.save(c);
        } catch (DataIntegrityViolationException dive) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate case violates unique constraint",
                    dive
            );
        }
    }

    @Transactional(readOnly = true)
    public CaseFacade getCase(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found")
                );
    }

    @Transactional(readOnly = true)
    public List<CaseFacade> getAllCases() {
        return repository.findAll();
    }

    // --- NEW: filtered search with pagination ---
    @Transactional(readOnly = true)
    public Page<CaseFacade> searchCases(
            String status,
            String caseType,
            String priority,
            String assignedTo,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        String normStatus   = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        String normPriority = (priority == null || priority.isBlank()) ? null : priority.trim().toUpperCase();
        String normCaseType = (caseType == null || caseType.isBlank()) ? null : caseType.trim();
        String normAssignee = (assignedTo == null || assignedTo.isBlank()) ? null : assignedTo.trim();

        return repository.searchCases(
                normStatus, normCaseType, normPriority, normAssignee, createdFrom, createdTo, pageable
        );
    }

    public CaseFacade updateCase(Long id, CaseFacade updatedCase) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found")
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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate case violates unique constraint",
                    dive
            );
        }
    }

    public void deleteCase(Long id) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found")
                );

        repository.deleteById(id);
        historyRepository.deleteByCaseId(id);
    }

    // --- UPDATED: status endpoint with allowed transitions + uniqueness guard ---
    public CaseFacade updateStatus(Long caseId, String requestedStatus) {
        if (requestedStatus == null || requestedStatus.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not be blank");
        }
        final String newStatus = requestedStatus.trim().toUpperCase();

        if (!ALLOWED_STATES.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Illegal status: " + newStatus + ". Allowed: " + ALLOWED_STATES);
        }

        CaseFacade existing = repository.findById(caseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Case with ID " + caseId + " not found"));

        String current = existing.getClassification() != null ? existing.getClassification().getStatus() : null;
        String currentStatus = (current == null ? null : current.trim().toUpperCase());

        if (currentStatus == null) {
            // If no status yet, only allow initializing to OPEN
            if (!"OPEN".equals(newStatus)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Invalid transition from <null> to " + newStatus + ". Only OPEN is allowed as the first state.");
            }
        } else {
            if (currentStatus.equals(newStatus)) {
                // No change
                return existing;
            }
            java.util.Set<String> next = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, java.util.Set.of());
            if (!next.contains(newStatus)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Invalid transition: " + currentStatus + " -> " + newStatus +
                                ". Allowed next states: " + next);
            }
        }

        // Enforce uniqueness (case_type + primary_txn_id + status)
        String caseType = existing.getIdentifier().getCaseType();
        String txnId    = existing.getIdentifier().getPrimaryTransactionId();
        boolean duplicateExists =
                repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                        caseType, txnId, newStatus
                );
        if (duplicateExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A case already exists with the same CaseType, primaryTransactionId and status"
            );
        }

        // Update status + history
        if (existing.getClassification() == null) {
            existing.setClassification(new CaseClassification());
        }

        // Write history with correct old/new values
        addCaseToHistory(existing, "status change", newStatus);

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

    @Transactional(readOnly = true)
    public List<CaseHistory> getCaseHistoryById(Long caseId) {
        try{
            return historyRepository.findByCaseId(repository.getById(caseId)).stream()
                    .sorted(Comparator.comparing(CaseHistory::getChangedAt))
                    .toList();
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Case with ID " + caseId + " not found");
        }
    }

    public void addCaseToHistory(Long caseId, String comment, String newStatus){
        CaseFacade caseEntry = getCase(caseId);

        CaseHistory historyEntry = CaseHistory.builder()
                .caseId(caseEntry)
                .oldStatus(caseEntry.getClassification() == null ? null : caseEntry.getClassification().getStatus())
                .newStatus(newStatus)
                .comment(comment)
                .changedBy(caseEntry.getAssignment() == null ? null : caseEntry.getAssignment().getAssignedTo())
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(historyEntry);
    }

    public void addCaseToHistory(CaseFacade caseEntry, String comment, String newStatus){
        CaseHistory historyEntry = CaseHistory.builder()
                .caseId(caseEntry)
                .oldStatus(caseEntry.getClassification() == null ? null : caseEntry.getClassification().getStatus())
                .newStatus(newStatus)
                .comment(comment)
                .changedBy(caseEntry.getAssignment() == null ? null : caseEntry.getAssignment().getAssignedTo())
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(historyEntry);
    }
}