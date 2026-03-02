package com.capgemini.demo.repository;

import com.capgemini.demo.casefacade.CaseFacade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<CaseFacade, Long> {
    boolean existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
            String caseType,
            String transactionId,
            String status
    ); // <-- REPLACED
}