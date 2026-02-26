package com.capgemini.demo.repository;

import com.capgemini.demo.casehelper.CaseHistory;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseHistoryRepository extends JpaRepository<@NonNull CaseHistory,@NonNull Integer> {
    CaseHistory findByHistoryId();
}
