package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.casefacade.CaseStatusCode;
import com.capgemini.demo.casehelper.CaseReport;
import com.capgemini.demo.ruleEngine.Priority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    public List<Map<String,Map<String,Integer>>> getCaseSummaryReport(List<CaseFacade> caseFacadeList){
        // Returns # of cases by type, status, and priority
        CaseReport caseReport = new CaseReport(caseFacadeList);


        Map<String, Integer> caseCountByType = caseReport.getCasesByType().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        Map<String, Map<String, Integer>> casesByType = new HashMap<>();
        casesByType.put("Cases By Type", caseCountByType);

        Map<String, Integer> caseCountByStatus = caseReport.getCasesByStatus().entrySet().stream()
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                        entry -> entry.getValue().size()));

        Map<String,Map<String,Integer>> casesByStatus = new HashMap<>();
        casesByStatus.put("Cases By Status", caseCountByStatus);

        Map<String, Integer> caseCountByResolution = caseReport.getCasesByResolution().entrySet().stream()
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> entry.getValue().size()));

        Map<String,Map<String,Integer>> casesByResolution = new HashMap<>();
        casesByResolution.put("Cases By Resolution", caseCountByResolution);

        List<Map<String,Map<String,Integer>>> casesByTypeStatusResolution = new ArrayList<>();
        casesByTypeStatusResolution.add(casesByType);
        casesByTypeStatusResolution.add(casesByStatus);
        casesByTypeStatusResolution.add(casesByResolution);

        return casesByTypeStatusResolution;

        /*

        Map<Long, String> closedCases =
                caseFacadeList.stream()
                        .filter(c->!c.isOpen())
                        .collect(Collectors.toMap(CaseFacade::getId,c->c.getOutcome().getResolution()));
         */
        /*
        int closedCases =
                caseFacadeList.stream()
                        .filter(c->!c.isOpen())
                        .mapToInt(x -> 1).sum();
        int openCases =
                caseFacadeList.stream()
                        .filter(CaseFacade::isOpen)
                        .mapToInt(x -> 1).sum();

        int inReviewCases =
                caseFacadeList.stream()
                        .filter(c -> c.getStatus().equals(CaseStatusCode.IN_REVIEW.name()))
                        .mapToInt(x -> 1).sum();

        int pendingCustomerCases =
                caseFacadeList.stream()
                        .filter(c -> c.getStatus().equals(CaseStatusCode.PENDING_CUSTOMER.name()))
                        .mapToInt(x -> 1).sum();

        int pendingPartnerCases =
                caseFacadeList.stream()
                        .filter(c -> c.getStatus().equals(CaseStatusCode.PENDING_PARTNER.name()))
                        .mapToInt(x -> 1).sum();
        int resolvedCustomerFavorCases =
                caseFacadeList.stream()
                        .filter(c -> c.getStatus().equals(CaseStatusCode.RESOLVED_CUSTOMER_FAVOUR.name()))
                        .mapToInt(x -> 1).sum();
        int resolvedBankFavorCases =
                caseFacadeList.stream()
                        .filter(c -> c.getStatus().equals(CaseStatusCode.RESOLVED_BANK_FAVOUR.name()))
                        .mapToInt(x -> 1).sum();

        int lowPriorityCases =
                caseFacadeList.stream()
                        .filter(c -> c.getPriority().equals(Priority.LOW.name()))
                        .mapToInt(x -> 1).sum();

        int mediumPriorityCases =
                caseFacadeList.stream()
                        .filter(c -> c.getPriority().equals(Priority.MEDIUM.name()))
                        .mapToInt(x -> 1).sum();

        int highPriorityCases =
                caseFacadeList.stream()
                        .filter(c -> c.getPriority().equals(Priority.HIGH.name()))
                        .mapToInt(x -> 1).sum();

        int criticalPriorityCases =
                caseFacadeList.stream()
                        .filter(c -> c.getPriority().equals(Priority.CRITICAL.name()))
                        .mapToInt(x -> 1).sum();

        int unknownPriorityCases =
                caseFacadeList.stream()
                        .filter(c -> c.getPriority().equals(Priority.UNKNOWN.name()))
                        .mapToInt(x -> 1).sum();

        //Should return 2 maps: cases by type, and cases by resolution
        return STR."""
open cases: \{openCases}
closed cases: \{closedCases}
cases in review: \{inReviewCases}
pending customer cases: \{pendingCustomerCases}
resolved customer favor cases: \{resolvedCustomerFavorCases}
resolved bank favor cases: \{resolvedBankFavorCases}
low priority cases: \{lowPriorityCases}
medium priority cases: \{mediumPriorityCases}
high priority cases: \{highPriorityCases}
critical priority cases: \{criticalPriorityCases}
unknown priority cases: \{unknownPriorityCases}""";

         */
    }

    public List<Map<String,Integer>> getCaseBacklogReport(List<CaseFacade> caseFacadeList){
        //returns open cases by type and priority, near due case count, overdue case count
        CaseReport caseReport = new CaseReport(caseFacadeList);

        Map<String,Integer> nearDue = caseReport
                .getCasesByDaysUntilDue().entrySet()
                .stream().filter(c->
                        c.getKey() < 3 && c.getKey() > 0)
                .collect(Collectors
                        .toMap(c -> STR."Nearly due \{c.getKey()} days left:",
                                entry -> entry.getValue().size()));

        Map<String,Integer> overDue = caseReport
                .getCasesByDaysUntilDue().entrySet()
                .stream().filter(c->
                        c.getKey() < 0)
                .collect(Collectors
                        .toMap(c -> STR."Overdue by \{c.getKey()} day(s):",
                                entry -> entry.getValue().size()));

        Map<String,Integer> openByType = caseReport
                .getCasesByType().entrySet()
                        .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        c->c.getValue()
                                .stream().filter(CaseFacade::isOpen).toList()
                                .size()));

        Map<String,Integer> openByPriority = caseReport
                .getCasesByPriority().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        c->c.getValue()
                                .stream().filter(CaseFacade::isOpen).toList()
                                .size()));

        List<Map<String,Integer>> backlogReport = new ArrayList<>();
        backlogReport.add(nearDue);
        backlogReport.add(overDue);
        backlogReport.add(openByType);
        backlogReport.add(openByPriority);

        return backlogReport;

        /*
        Predicate<CaseFacade> nearlyDue = c -> (!c.isPastDue() &&
                (LocalDateTime.now()
                        .until(c.getClassification()
                                .getDueDate(), TimeUnit.DAYS.toChronoUnit())) <= 3 ) &&
                (!c.isPastDue() &&
                        (LocalDateTime.now()
                                .until(c.getClassification()
                                        .getDueDate(), TimeUnit.DAYS.toChronoUnit())) > 0);

        Map<Long, LocalDateTime> overdueCases = caseFacadeList.stream()
                .filter(CaseFacade::isPastDue)
                .collect(Collectors
                        .toMap(CaseFacade::getId,
                    c->c.getClassification().getDueDate()));

        Map<Long, Long> nearOverdueCases = caseFacadeList.stream()
                .filter(nearlyDue)
                .collect(Collectors
                        .toMap(CaseFacade::getId,
                                c->LocalDateTime.now().until(c.getClassification().getDueDate(), ChronoUnit.DAYS)));
        return STR."""

Overdue cases:
\{overdueCases}
Nearly overdue Cases:
\{nearOverdueCases}
""";
         */

    }

    public Map<String, Integer> getCaseAgingReport(List<CaseFacade> caseFacadeList){
        CaseReport caseReport = new CaseReport(caseFacadeList);
        Map<String, Integer> ageBucketCount =
                caseReport.getCasesByAgeBucket()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> entry.getValue().size()));

        return ageBucketCount;

        /*
        List<CaseFacade> lessThanADay = caseFacadeList.stream()
                .filter(cardOpCase ->
                        (cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 1)
                .toList();

        List<CaseFacade> oneToThreeDays = caseFacadeList.stream()
                .filter(cardOpCase ->
                        ((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 3)
                                &&((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) > 1))
                .toList();

        List<CaseFacade> threeToSevenDays = caseFacadeList.stream()
                .filter(cardOpCase ->
                        ((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) > 3)
                                &&((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 7))
                .toList();

        List<CaseFacade> greaterThanSevenDays = caseFacadeList.stream()
                .filter(cardOpCase ->
                        (cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) > 7)
                .toList();
        return STR."""
Report by caseID:

0-1 Days: \{lessThanADay.stream().map(CaseFacade::getId).toList()}

1-3 Days: \{oneToThreeDays.stream().map(CaseFacade::getId).toList()}

3-7 Days: \{threeToSevenDays.stream().map(CaseFacade::getId).toList()}

7+ Days: \{greaterThanSevenDays.stream().map(CaseFacade::getId).toList()}
""";

         */
    }
}
