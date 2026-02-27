package com.capgemini.demo.repository;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casehelper.CaseHistory;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
@NullMarked
public interface CaseHistoryRepository extends JpaRepository<CaseHistory,Long> {
    CaseHistory findByHistoryId(Long historyId);
    ArrayList<CaseHistory> findByCaseId(CaseFacade caseId);
    @NullMarked
    ArrayList<CaseHistory> findAll();

    void deleteByCaseId(Long id);
}
