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
            c.getIdentifier().getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID required");
        }
        return repository.save(c);
    }

    public CaseFacade getCase(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
    }

    public List<CaseFacade> getAllCases() {
        return repository.findAll();
    }

    
    public CaseFacade updateCase(Long id, CaseFacade updatedCase) {
        CaseFacade existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        existing.setRuleEvalFailed(updatedCase.getRuleEvalFailed());
        existing.setClassification(updatedCase.getClassification());
        existing.setAssignment(updatedCase.getAssignment());
        existing.setIdentifier(updatedCase.getIdentifier());
        existing.setTransaction(updatedCase.getTransaction());
        existing.setOutcome(updatedCase.getOutcome());

        return repository.save(existing);
    }

    /* NEW: Delete Case */
    public void deleteCase(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Case not found");
        }
        repository.deleteById(id);
    }
}
