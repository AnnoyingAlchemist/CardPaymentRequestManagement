package com.capgemini.demo;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportingServiceTest {

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService();
    }

    // --- Helpers to build mocks ---

    private CaseFacade mockClosedCase(long id, String resolution) {
        CaseFacade c = mock(CaseFacade.class, withSettings().lenient());
        when(c.getId()).thenReturn(id);
        when(c.isOpen()).thenReturn(false);

        var outcome = mock(Object.class, withSettings().lenient());

        c = mock(CaseFacade.class, Mockito.RETURNS_DEEP_STUBS);
        when(c.getId()).thenReturn(id);
        when(c.isOpen()).thenReturn(false);
        when(c.getOutcome().getResolution()).thenReturn(resolution);

        return c;
    }

    private CaseFacade mockCaseForBacklog(long id, boolean pastDue, LocalDateTime dueDate) {
        CaseFacade c = mock(CaseFacade.class, Mockito.RETURNS_DEEP_STUBS);
        when(c.getId()).thenReturn(id);
        when(c.isPastDue()).thenReturn(pastDue);
        when(c.getClassification().getDueDate()).thenReturn(dueDate);
        // isOpen not relevant here
        return c;
    }

    private CaseFacade mockCaseForAging(long id, LocalDateTime createdAt) {
        CaseFacade c = mock(CaseFacade.class, Mockito.RETURNS_DEEP_STUBS);
        when(c.getId()).thenReturn(id);
        when(c.getAssignment().getCreatedAt()).thenReturn(createdAt);
        return c;
    }

    private String extractSection(String report, String header) {
        int start = report.indexOf(header);
        if (start < 0) return "";
        int end = report.indexOf("\n\n", start);
        if (end < 0) end = report.length();
        return report.substring(start, end).replaceAll("[^0-9]", "");
    }

    // --- Testcases ---

    @Test
    void getCaseSummaryReport_shouldListClosedCasesWithResolution() {
        var c1 = mockClosedCase(101L, "RESOLVED_CUSTOMER_FAVOUR");
        var c2 = mockClosedCase(102L, "RESOLVED_BANK_FAVOUR");

        String report = reportingService.getCaseSummaryReport(List.of(c1, c2));

        assertNotNull(report);
        assertTrue(report.contains("closed cases:"));
        assertTrue(report.contains("101"));
        assertTrue(report.contains("102"));
        assertTrue(report.contains("RESOLVED_CUSTOMER_FAVOUR"));
        assertTrue(report.contains("RESOLVED_BANK_FAVOUR"));
    }

    @Test
    void getCaseAgingReport_currentLogicProducesInconsistentBuckets() {
        LocalDateTime now = LocalDateTime.now();

        // Create three cases at different ages
        var lessThanADay = mockCaseForAging(501L, now.minusHours(12));   // ~0.5 days
        var twoDays = mockCaseForAging(502L, now.minusDays(2));          // 2 days
        var fiveDays = mockCaseForAging(503L, now.minusDays(5));         // 5 days
        var tenDays = mockCaseForAging(504L, now.minusDays(10));         // 10 days

        String report = reportingService.getCaseAgingReport(List.of(lessThanADay, twoDays, fiveDays, tenDays));

        assertNotNull(report);
        assertTrue(report.contains("Report by caseID:"));

        assertTrue(report.contains("501"));
        assertTrue(report.contains("502"));
        assertTrue(report.contains("503"));
        assertTrue(report.contains("504"));

    }
    @Test
    void getCaseSummaryReport_noClosedCases_returnsEmptyMapString() {
        var open = mockClosedCase(500L, "IGNORED");
        when(open.isOpen()).thenReturn(true); // force open case

        String report = reportingService.getCaseSummaryReport(List.of(open));

        assertTrue(report.contains("closed cases"), "Should still print header");
        assertFalse(report.contains("500"));
    }

    @Test
    void getCaseSummaryReport_mixedOpenAndClosed_onlyClosedAppear() {
        var closed = mockClosedCase(600L, "RESOLVED");
        var open = mockClosedCase(601L, "IGNORED");
        when(open.isOpen()).thenReturn(true);

        String report = reportingService.getCaseSummaryReport(List.of(closed, open));

        assertTrue(report.contains("600"));
        assertFalse(report.contains("601"));
    }

    // test for no overdue and no nearly due
    @Test
    void getCaseBacklogReport_noOverdueNoNearlyDue_allFutureBeyond3Days() {
        LocalDateTime now = LocalDateTime.now();
        var far = mockCaseForBacklog(800L, false, now.plusDays(10));

        String report = reportingService.getCaseBacklogReport(List.of(far));

        String nearly = extractSection(report, "Nearly overdue Cases:");
        String overdue = extractSection(report, "Overdue cases:");

        assertFalse(nearly.contains("800"));
        assertFalse(overdue.contains("800"));
    }

    // test for all overdue
    @Test
    void getCaseBacklogReport_allOverdue() {
        LocalDateTime now = LocalDateTime.now();
        var c1 = mockCaseForBacklog(810L, true, now.minusDays(1));
        var c2 = mockCaseForBacklog(811L, true, now.minusHours(10));

        String report = reportingService.getCaseBacklogReport(List.of(c1, c2));

        String overdue = extractSection(report, "Overdue cases:");
        assertTrue(overdue.contains("810"));
        assertTrue(overdue.contains("811"));

        String nearly = extractSection(report, "Nearly overdue Cases:");
        assertFalse(nearly.contains("810"));
        assertFalse(nearly.contains("811"));
    }

    // test for boundary exactly 3 days
    @Test
    void getCaseBacklogReport_dueExactlyThreeDaysAway_includedAsNearlyDue() {
        LocalDateTime now = LocalDateTime.now();
        var c = mockCaseForBacklog(830L, false, now.plusDays(3));

        String report = reportingService.getCaseBacklogReport(List.of(c));

        String nearly = extractSection(report, "Nearly overdue Cases:");
        assertTrue(nearly.contains("830"));
    }

    // test for boundary 4 days away
    @Test
    void getCaseBacklogReport_dueMoreThanThreeDaysAway_notIncludedAsNearlyDue() {
        LocalDateTime now = LocalDateTime.now();
        var c = mockCaseForBacklog(840L, false, now.plusDays(4).plusHours(1));

        String report = reportingService.getCaseBacklogReport(List.of(c));

        String nearly = extractSection(report, "Nearly overdue Cases:");
        assertFalse(nearly.contains("840"));
    }

    // test for empty list
    @Test
    void getCaseBacklogReport_emptyList_returnsEmptySections() {
        String report = reportingService.getCaseBacklogReport(List.of());

        String overdue = extractSection(report, "Overdue cases:");
        String nearly = extractSection(report, "Nearly overdue Cases:");

        assertEquals("", overdue.trim());
        assertEquals("", nearly.trim());
    }

    // test only < 1 day
    @Test
    void getCaseAgingReport_onlyLessThanOneDay() {
        LocalDateTime now = LocalDateTime.now();
        var c = mockCaseForAging(900L, now.minusHours(6));

        String report = reportingService.getCaseAgingReport(List.of(c));

        assertTrue(report.contains("900"));
    }

    // test for only 1 - 3 day case
    @Test
    void getCaseAgingReport_oneToThreeDays() {
        LocalDateTime now = LocalDateTime.now();
        var c = mockCaseForAging(910L, now.minusDays(2));

        String report = reportingService.getCaseAgingReport(List.of(c));

        assertTrue(report.contains("910"));
    }

    // test for only 3 - 7 days
    @Test
    void getCaseAgingReport_threeToSevenDays() {
        LocalDateTime now = LocalDateTime.now();
        var c = mockCaseForAging(920L, now.minusDays(5));

        String report = reportingService.getCaseAgingReport(List.of(c));

        assertTrue(report.contains("920"));
    }

    // test for only > 7 days
    @Test
    void getCaseAgingReport_greaterThanSevenDays() {
        LocalDateTime now = LocalDateTime.now();
        var c = mockCaseForAging(930L, now.minusDays(12));

        String report = reportingService.getCaseAgingReport(List.of(c));

        assertTrue(report.contains("930"));
    }

    // test for boundary exactly 1, 3, 7 days
    @Test
    void getCaseAgingReport_exactBoundaries() {
        LocalDateTime now = LocalDateTime.now();

        var d1 = mockCaseForAging(940L, now.minusDays(1));
        var d3 = mockCaseForAging(941L, now.minusDays(3));
        var d7 = mockCaseForAging(942L, now.minusDays(7));

        String report = reportingService.getCaseAgingReport(List.of(d1, d3, d7));

        assertTrue(report.contains("940"));
        assertTrue(report.contains("941"));
        assertTrue(report.contains("942"));
    }

    // test for empty input list
    @Test
    void getCaseAgingReport_emptyList_includesHeadersOnly() {
        String report = reportingService.getCaseAgingReport(List.of());

        assertTrue(report.contains("0-1 Days"));
        assertTrue(report.contains("1-3 Days"));
        assertTrue(report.contains("3-7 Days"));
        assertTrue(report.contains("7+ Days"));
    }

    // test for Null CreatedAt
    @Test
    void getCaseAgingReport_nullCreatedAt_throwsException() {
        var c = mockCaseForAging(950L, null);

        assertThrows(NullPointerException.class,
                () -> reportingService.getCaseAgingReport(List.of(c)));
    }
}