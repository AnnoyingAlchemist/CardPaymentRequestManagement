package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.repository.CaseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ADD
import org.springframework.web.server.ResponseStatusException; // <-- ADD

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
}
