package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.repository.CaseRepository;
//import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CaseService {

    private final CaseRepository repository;

    public CaseService(CaseRepository repository) {
        this.repository = repository;
    }

    public CaseFacade createCase(CaseFacade c) {
        if (c.getIdentifier() == null ||
            c.getIdentifier().getCustomerId() == null ||
            c.getIdentifier().getCustomerId().isBlank()) {
            throw new IllegalArgumentException("Customer ID required");
        }
        return repository.save(c);
    }

    public CaseFacade getCase(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case with ID " + id + " not found"));
    }

    public List<CaseFacade> getAllCases() {
        return repository.findAll();
    }

    
    public CaseFacade updateCase(Long id, CaseFacade updatedCase) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case with ID " + id + "not found"));

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

        return repository.save(existing);
    }


    public void deleteCase(Long id) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Case with ID " + id + " not found"));

        repository.deleteById(id);
    }
}
