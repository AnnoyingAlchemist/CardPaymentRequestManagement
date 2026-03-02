package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseFacade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    public String getCaseSummaryReport(List<CaseFacade> caseFacadeList){
        Map<Long, String> closedCases =
                caseFacadeList.stream()
                        .filter(c->!c.isOpen())
                        .collect(Collectors.toMap(CaseFacade::getId,c->c.getOutcome().getResolution()));
        return STR."closed cases: \{closedCases}";
    }

    public String getCaseBacklogReport(List<CaseFacade> caseFacadeList){

        Predicate<CaseFacade> nearlyDue = c -> (!c.isPastDue() &&
                (LocalDateTime.now()
                        .until(c.getClassification()
                                .getDueDate(), TimeUnit.DAYS.toChronoUnit())) <= 3);

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
    }

    public String getCaseAgingReport(List<CaseFacade> caseFacadeList){
        List<CaseFacade> lessThanADay = caseFacadeList.stream()
                .filter(cardOpCase ->
                        (cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 1)
                .toList();

        List<CaseFacade> oneToThreeDays = caseFacadeList.stream()
                .filter(cardOpCase ->
                        ((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) >= 3)
                                &&((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 1))
                .toList();

        List<CaseFacade> threeToSevenDays = caseFacadeList.stream()
                .filter(cardOpCase ->
                        ((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) >= 3)
                                &&((cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 7))
                .toList();

        List<CaseFacade> greaterThanSevenDays = caseFacadeList.stream()
                .filter(cardOpCase ->
                        (cardOpCase.getAssignment().getCreatedAt()
                                .until(LocalDateTime.now(), TimeUnit.DAYS.toChronoUnit())) <= 1)
                .toList();
        return STR."""
Report by caseID:

0-1 Days: \{lessThanADay.stream().map(CaseFacade::getId).toList()}

1-3 Days: \{oneToThreeDays.stream().map(CaseFacade::getId).toList()}

3-7 Days: \{threeToSevenDays.stream().map(CaseFacade::getId).toList()}

7+ Days: \{greaterThanSevenDays.stream().map(CaseFacade::getId).toList()}
""";
    }
}
