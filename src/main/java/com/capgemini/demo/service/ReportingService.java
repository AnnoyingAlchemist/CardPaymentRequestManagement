package com.capgemini.demo.service;

import com.capgemini.demo.casefacade.CaseFacade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    public String getCaseSummaryReport(List<CaseFacade> caseFacadeList){
        return "";
    }

    public String getCaseBacklogReport(List<CaseFacade> caseFacadeList){
        return "";
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
        return "0-1 Days: " + lessThanADay +"\n\n"
                +"1-3 Days: " + oneToThreeDays +"\n\n"
                +"3-7 Days: " + threeToSevenDays +"\n\n"
                +"7+ Days: " + greaterThanSevenDays +"\n";
    }
}
