package com.capgemini.demo.repository;

import com.capgemini.demo.casefacade.CaseFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CaseRepository extends JpaRepository<CaseFacade, Long> {

    boolean existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
            String caseType,
            String transactionId,
            String status
    );

    // Current status only (no entity hydration)
    @Query("SELECT c.classification.status FROM CaseFacade c WHERE c.id = :id")
    String findStatusById(@Param("id") Long id);

    // Case tuple (caseType, txnId) only (no entity hydration)
    @Query("SELECT c.identifier.caseType, c.identifier.primaryTransactionId FROM CaseFacade c WHERE c.id = :id")
    Object[] findCaseTypeAndTxnIdByCaseId(@Param("id") Long id);

    // Optional filters + pagination
    @Query("""
        SELECT c FROM CaseFacade c
        WHERE (:status IS NULL OR UPPER(c.classification.status) = UPPER(:status))
          AND (:caseType IS NULL OR c.identifier.caseType = :caseType)
          AND (:priority IS NULL OR UPPER(c.classification.priority) = UPPER(:priority))
          AND (:assignedTo IS NULL OR c.assignment.assignedTo = :assignedTo)
          AND (:createdFrom IS NULL OR c.assignment.createdAt >= :createdFrom)
          AND (:createdTo IS NULL OR c.assignment.createdAt <= :createdTo)
        """)
    Page<CaseFacade> searchCases(
            @Param("status") String status,
            @Param("caseType") String caseType,
            @Param("priority") String priority,
            @Param("assignedTo") String assignedTo,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            Pageable pageable
    );
}