package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseAssignment;
import com.capgemini.demo.casefacade.CaseClassification;
import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.Priority;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Month;                            // used for date bounds
import java.util.*;
import java.util.stream.Collectors;

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
    // Allowed states & transitions (v1)
    // ---------------------------
    private static final Set<String> ALLOWED_STATES = Set.of(
            "OPEN", "IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER",
            "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"
    );

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS;
    static {
        Map<String, Set<String>> m = new HashMap<>();
        m.put("OPEN", Set.of("IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER", "CLOSED"));
        m.put("IN_REVIEW", Set.of("PENDING_CUSTOMER", "PENDING_PARTNER",
                "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"));
        m.put("PENDING_CUSTOMER", Set.of("IN_REVIEW", "CLOSED"));
        m.put("PENDING_PARTNER", Set.of("IN_REVIEW", "CLOSED"));
        m.put("RESOLVED_CUSTOMER_FAVOUR", Set.of("CLOSED"));
        m.put("RESOLVED_BANK_FAVOUR", Set.of("CLOSED"));
        m.put("CLOSED", Set.of());
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(m);
    }

    // ---------------------------
    // VERSIONING - stricter transitions for v2
    // ---------------------------
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS_V2;
    static {
        Map<String, Set<String>> m2 = new HashMap<>();
        m2.put("OPEN", Set.of("IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER")); // CLOSED removed
        m2.put("IN_REVIEW", Set.of("PENDING_CUSTOMER", "PENDING_PARTNER",
                "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"));
        m2.put("PENDING_CUSTOMER", Set.of("IN_REVIEW", "CLOSED"));
        m2.put("PENDING_PARTNER", Set.of("IN_REVIEW", "CLOSED"));
        m2.put("RESOLVED_CUSTOMER_FAVOUR", Set.of("CLOSED"));
        m2.put("RESOLVED_BANK_FAVOUR", Set.of("CLOSED"));
        m2.put("CLOSED", Set.of());
        ALLOWED_TRANSITIONS_V2 = Collections.unmodifiableMap(m2);
    }

    // ---------------------------
    // CRUD & Business
    // ---------------------------

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

        // normalize
        c.getClassification().setStatus(c.getClassification().getStatus().trim().toUpperCase());
        if (c.getClassification().getPriority() != null) {
            c.getClassification().setPriority(c.getClassification().getPriority().trim().toUpperCase());
        }

        boolean exists = repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                c.getIdentifier().getCaseType(),
                c.getIdentifier().getPrimaryTransactionId(),
                c.getClassification().getStatus()
        );
        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A case already exists with the same CaseType, primaryTransactionId and status"
            );
        }

        c.setId(null);

        RuleEngineService ruleService = new RuleEngineService();
        RuleSuggestion suggestion = ruleService.evalCase(new CaseSummary(c));
        if (suggestion.getPriority() == Priority.UNKNOWN) {
            c.setRuleEvalFailed(true);
        }
        c.getClassification().setPriority(suggestion.getPriority().name());
        c.getClassification().setRecommendedNextAction(suggestion.getRecommendedNextAction());

        try {
            repository.save(c);
            addNewCaseToHistory(c, "created", c.getClassification().getStatus());
            return c;
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
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<CaseFacade> getAllCases() {
        return repository.findAll();
    }

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
        final String normStatus   = (status == null || status.isBlank()) ? "" : status.trim().toUpperCase();
        final String normPriority = (priority == null || priority.isBlank()) ? "" : priority.trim().toUpperCase();
        final String normCaseType = (caseType == null || caseType.isBlank()) ? "" : caseType.trim();
        final String normAssignee = (assignedTo == null || assignedTo.isBlank()) ? "" : assignedTo.trim();

        final LocalDateTime from = (createdFrom != null)
                ? createdFrom
                : LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0);
        final LocalDateTime to = (createdTo != null)
                ? createdTo
                : LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59, 59);
        LocalDateTime effFrom = from, effTo = to;
        if (effFrom.isAfter(effTo)) { effFrom = to; effTo = from; }

        return repository.searchCases(
                normStatus, normCaseType, normPriority, normAssignee, effFrom, effTo, pageable
        );
    }

    // VERSIONING — v2 multi-status search
    @Transactional(readOnly = true)
    public Page<CaseFacade> searchCasesV2(
            String statusesCsv,
            String caseType,
            String priority,
            String assignedTo,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        final String normPriority = (priority == null || priority.isBlank()) ? "" : priority.trim().toUpperCase();
        final String normCaseType = (caseType == null || caseType.isBlank()) ? "" : caseType.trim();
        final String normAssignee = (assignedTo == null || assignedTo.isBlank()) ? "" : assignedTo.trim();

        List<String> statuses = List.of();
        boolean statusesEmpty = true;
        if (statusesCsv != null && !statusesCsv.isBlank()) {
            statuses = Arrays.stream(statusesCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .distinct()
                    .collect(Collectors.toList());
            statusesEmpty = statuses.isEmpty();
        }

        final LocalDateTime from = (createdFrom != null)
                ? createdFrom
                : LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0);
        final LocalDateTime to = (createdTo != null)
                ? createdTo
                : LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59, 59);
        LocalDateTime effFrom = from, effTo = to;
        if (effFrom.isAfter(effTo)) { effFrom = to; effTo = from; }

        return repository.searchCasesV2(
                statuses, statusesEmpty, normCaseType, normPriority, normAssignee, effFrom, effTo, pageable
        );
    }

    public CaseFacade updateCase(Long id, CaseFacade updatedCase) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found"));

        if (updatedCase.getRuleEvalFailed() != null) {
            existing.setRuleEvalFailed(updatedCase.getRuleEvalFailed());
        }
        if (updatedCase.getClassification() != null) {
            if (updatedCase.getClassification().getStatus() != null) {
                updatedCase.getClassification().setStatus(
                        updatedCase.getClassification().getStatus().trim().toUpperCase()
                );
            }
            if (updatedCase.getClassification().getPriority() != null) {
                updatedCase.getClassification().setPriority(
                        updatedCase.getClassification().getPriority().trim().toUpperCase()
                );
            }
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
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Case with ID " + id + " not found"));

        repository.deleteById(id);
        historyRepository.deleteByCaseId(existing);
    }

    /**
     * v1 status update (legacy & v1).
     */
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
            if (!"OPEN".equals(newStatus)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Invalid transition from <null> to " + newStatus + ". Only OPEN is allowed as the first state.");
            }
        } else {
            if (currentStatus.equals(newStatus)) {
                return existing;
            }
            Set<String> next = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
            if (!next.contains(newStatus)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Invalid transition: " + currentStatus + " -> " + newStatus +
                                ". Allowed next states: " + next);
            }
        }

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

        if (existing.getClassification() == null) {
            existing.setClassification(new CaseClassification());
        }
        addCaseToHistory(existing, "status change", newStatus);
        existing.getClassification().setStatus(newStatus);

        return repository.save(existing);
    }

    // VERSIONING — v2 status update with stricter transitions and optional comment
    public CaseFacade updateStatusV2(Long caseId, String requestedStatus, String comment) {
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
            if (!"OPEN".equals(newStatus)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Invalid transition from <null> to " + newStatus + ". Only OPEN is allowed as the first state.");
            }
        } else {
            if (currentStatus.equals(newStatus)) {
                return existing;
            }
            Set<String> next = ALLOWED_TRANSITIONS_V2.getOrDefault(currentStatus, Set.of());
            if (!next.contains(newStatus)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Invalid transition (v2): " + currentStatus + " -> " + newStatus +
                                ". Allowed next states: " + next);
            }
        }

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

        if (existing.getClassification() == null) {
            existing.setClassification(new CaseClassification());
        }
        addCaseToHistory(existing, (comment == null ? "status change (v2)" : comment), newStatus);
        existing.getClassification().setStatus(newStatus);

        return repository.save(existing);
    }

    public CaseFacade updateAssignee(Long caseId, String assignee) {
        CaseFacade existing = repository.findById(caseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Case with ID " + caseId + " not found"));

        if (assignee == null || assignee.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must not be blank");
        }

        if (existing.getAssignment() == null) {
            existing.setAssignment(new CaseAssignment());
            existing.getAssignment().setCreatedAt(LocalDateTime.now());
        }
        existing.getAssignment().setAssignedTo(assignee.trim());
        existing.getAssignment().setUpdatedAt(LocalDateTime.now());

        return repository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<CaseHistory> getCaseHistoryById(Long caseId) {
        CaseFacade caseRef = repository.findById(caseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Case with ID " + caseId + " not found"));

        return historyRepository.findByCaseId(caseRef).stream()
                .sorted(Comparator.comparing(CaseHistory::getChangedAt))
                .toList();
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

    public void addNewCaseToHistory(CaseFacade caseEntry, String comment, String newStatus){
        CaseHistory historyEntry = CaseHistory.builder()
                .caseId(caseEntry)
                .oldStatus(null)
                .newStatus(newStatus)
                .comment(comment)
                .changedBy(caseEntry.getAssignment() == null ? null : caseEntry.getAssignment().getAssignedTo())
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(historyEntry);
    }
}