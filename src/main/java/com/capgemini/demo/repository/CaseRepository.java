package com.capgemini.demo.repository;

import com.capgemini.demo.casefacade.CaseFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;                       // VERSIONING: ADDED

@Repository
public interface CaseRepository extends JpaRepository<CaseFacade, Long> {

    boolean existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
            String caseType,
            String transactionId,
            String status
    );

    @Query("SELECT c.classification.status FROM CaseFacade c WHERE c.id = :id")
    String findStatusById(@Param("id") Long id);

    @Query("SELECT c.identifier.caseType, c.identifier.primaryTransactionId FROM CaseFacade c WHERE c.id = :id")
    Object[] findCaseTypeAndTxnIdByCaseId(@Param("id") Long id);

    /**
     * v1 (legacy & v1) search using empty-string sentinels and BETWEEN.
     */
    @Query("""
        SELECT c FROM CaseFacade c
        WHERE (:status = '' OR c.classification.status = :status)
          AND (:caseType = '' OR c.identifier.caseType = :caseType)
          AND (:priority = '' OR c.classification.priority = :priority)
          AND (:assignedTo = '' OR c.assignment.assignedTo = :assignedTo)
          AND c.assignment.createdAt BETWEEN :createdFrom AND :createdTo
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

    // VERSIONING
    @Query("""
        SELECT c FROM CaseFacade c
        WHERE (:statusesEmpty = true OR c.classification.status IN :statuses)
          AND (:caseType = '' OR c.identifier.caseType = :caseType)
          AND (:priority = '' OR c.classification.priority = :priority)
          AND (:assignedTo = '' OR c.assignment.assignedTo = :assignedTo)
          AND c.assignment.createdAt BETWEEN :createdFrom AND :createdTo
        """)
    Page<CaseFacade> searchCasesV2(
            @Param("statuses") List<String> statuses,
            @Param("statusesEmpty") boolean statusesEmpty,
            @Param("caseType") String caseType,
            @Param("priority") String priority,
            @Param("assignedTo") String assignedTo,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            Pageable pageable
    );
}