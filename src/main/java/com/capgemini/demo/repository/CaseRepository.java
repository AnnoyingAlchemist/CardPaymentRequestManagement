package com.capgemini.demo.repository;

import com.capgemini.demo.casefacade.CaseFacade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<CaseFacade, Long> {
}
