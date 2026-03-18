package com.capgemini.demo.casehelper;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseTransaction;
import com.capgemini.demo.casefacade.CaseTypeCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class CaseReport {
    //We'll assume that the reporting service will handle counting cases per each attribute
    private Map<String, List<CaseFacade>> casesByType;
    private Map<String, List<CaseFacade>> casesByResolution;

    //Age should be in buckets: 0-1 days, 1-3 days, 3-7 days, over 7 days
    private Map<String, List<CaseFacade>> casesByAgeBucket;
    private Map<Long, List<CaseFacade>> casesByDaysUntilDue;

    private Map<String, List<CaseFacade>> casesByStatus;
    private Map<String, List<CaseFacade>> casesByPriority;

    public CaseReport(List<CaseFacade> caseList) {
        this.casesByType = caseList.stream().collect(Collectors.groupingBy(CaseFacade::getCaseType));

        this.casesByResolution = caseList.stream().collect(Collectors.groupingBy(CaseFacade::getResolution));


        this.casesByStatus = caseList.stream().collect(Collectors.groupingBy(CaseFacade::getStatus));
        this.casesByPriority = caseList.stream().collect(Collectors.groupingBy(CaseFacade::getPriority));

        this.casesByDaysUntilDue =  caseList.stream().collect(Collectors.groupingBy(
                c-> LocalDateTime.now().until(c.getDueDate(), ChronoUnit.DAYS))
        );
        this.casesByAgeBucket = new HashMap<>();
        this.casesByAgeBucket.put("0-1 days",
                caseList.stream().filter(c->
                                c.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS) <= 1)
                        .collect(Collectors.toList())
                );
        this.casesByAgeBucket.put("1-3 days",
                caseList.stream().filter(c->
                                c.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS) <= 3 &&
                                c.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS) >= 1)
                        .collect(Collectors.toList()));
        this.casesByAgeBucket.put("3-7 days",
                caseList.stream().filter(c->
                                c.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS) <= 7 &&
                                c.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS) >= 3)
                        .collect(Collectors.toList()));
        this.casesByAgeBucket.put("7+ days",
                caseList.stream().filter(c->
                                c.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS) > 7)
                        .collect(Collectors.toList())
                );
        //this.casesByAgeBucket = caseList.stream().collect(Collectors.groupingBy(CaseFacade::getCreatedAt));
    }
}
