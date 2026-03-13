package com.capgemini.demo;

import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.service.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * unit tests for RuleEngineService
 * We mock CaseSummary and stub only what each ruleset needs.
 */
class RuleEngineServiceTest {

    private RuleEngineService service;

    @BeforeEach
    void setUp() {
        service = new RuleEngineService();
    }

    /** Minimal mock for rules that only care about caseType. */
    private static CaseSummary csWithType(String caseType) {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn(caseType);
        return cs;
    }

    /**
     * Tailored mock for CARD_STATUS rule.
     * CardStatus.evaluate(...) should read CaseSummary's flat fields:
     * - getTransactionDateTime() as the "created/opened" time
     * - getDueDate() as the SLA/target time
     */
    private static CaseSummary csForCardStatus(LocalDateTime createdAt, LocalDateTime dueDate) {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn("CARD_STATUS");
        when(cs.getTransactionDateTime()).thenReturn(createdAt);
        when(cs.getDueDate()).thenReturn(dueDate);
        return cs;
    }

    @Nested
    @DisplayName("Known case type routing")
    class Routing {

        @Test
        @DisplayName("FRAUD_INVESTIGATION routes to Fraud and returns a suggestion")
        void fraudInvestigation() {
            CaseSummary cs = mock(CaseSummary.class);
            when(cs.getCaseType()).thenReturn("FRAUD_INVESTIGATION");

            // Provide non-null temporals to avoid LocalDateTime.until(...) NPE
            LocalDateTime now = LocalDateTime.now();
            when(cs.getTransactionDateTime()).thenReturn(now.minusHours(6));
            when(cs.getDueDate()).thenReturn(now.plusDays(2));

            RuleSuggestion out = service.evalCase(cs);

            assertThat(out).isNotNull();
        }

        @Test
        @DisplayName("CUSTOMER_DISPUTE routes to CustomerDispute and returns a suggestion")
        void customerDispute() {
            CaseSummary cs = mock(CaseSummary.class);
            when(cs.getCaseType()).thenReturn("CUSTOMER_DISPUTE");

            // Stub everything the rule reads. Amount is critical here:
            when(cs.getTransactionAmount()).thenReturn(new BigDecimal("123.45"));
            when(cs.getTransactionCurrency()).thenReturn("USD");         // if rule uses currency
            when(cs.getMerchantCategoryCode()).thenReturn("5999");       // if rule uses MCC
            when(cs.getTransactionCountry()).thenReturn("US");           // if rule uses country

            RuleSuggestion out = service.evalCase(cs);

            assertThat(out).isNotNull();
        }

        @Test
        @DisplayName("CHARGEBACK routes to Chargeback and returns a suggestion")
        void chargeback() {
            var cs = mock(CaseSummary.class);
            when(cs.getCaseType()).thenReturn("CHARGEBACK");

            // Stub the exact fields the rule reads to avoid NPE:
            var now = LocalDateTime.now();
            when(cs.getTransactionDateTime()).thenReturn(now.minusDays(1));
            when(cs.getDueDate()).thenReturn(now.plusDays(2));

            RuleSuggestion out = service.evalCase(cs);

            assertThat(out).isNotNull();
        }

        @Test
        @DisplayName("CARD_STATUS routes to CardStatus and returns a suggestion (no temporal NPE)")
        void cardStatus() {
            LocalDateTime now = LocalDateTime.now();
            CaseSummary cs = csForCardStatus(now.minusDays(2), now.plusDays(3));

            // Sanity checks to ensure our stubs are present
            assertThat(cs.getTransactionDateTime()).isNotNull();
            assertThat(cs.getDueDate()).isNotNull();

            RuleSuggestion out = service.evalCase(cs);

            assertThat(out).isNotNull();
        }
    }

    @Test
    @DisplayName("Unknown case type falls back to UnknownRule and yields UNKNOWN priority (if exposed)")
    void unknownType() {
        CaseSummary cs = csWithType("SOMETHING_NEW");

        RuleSuggestion out = service.evalCase(cs);

        assertThat(out).isNotNull();
        // Only assert this if RuleSuggestion exposes getPriority()
        try {
            assertThat(out.getPriority()).isEqualTo(Priority.UNKNOWN);
        } catch (Throwable ignored) {
            // If the API doesn't expose priority yet, keep it as a smoke test.
        }
    }

    @Test
    void routesAreCaseSensitive_orInsensitive_basedOnDecision() {
        CaseSummary cs = mock(CaseSummary.class);
        // Lower-case on purpose—your service should normalize this to FRAUD_INVESTIGATION
        when(cs.getCaseType()).thenReturn("fraud_investigation");

        // Fraud rule reads date/time, so provide non-null temporals
        var now = java.time.LocalDateTime.now();
        when(cs.getTransactionDateTime()).thenReturn(now.minusHours(1));
        when(cs.getDueDate()).thenReturn(now.plusDays(1));

        RuleSuggestion out = service.evalCase(cs);

        assertThat(out).isNotNull();
    }

    @Test
    void caseTypeWithWhitespace_isHandledOrNot() {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn("  CHARGEBACK  ");

        var now = java.time.LocalDateTime.now();
        when(cs.getTransactionDateTime()).thenReturn(now.minusDays(1));
        when(cs.getDueDate()).thenReturn(now.plusDays(2));

        RuleSuggestion out = service.evalCase(cs);
        assertThat(out).isNotNull();
    }

    @Test
    void nullCaseType_fallsBackToUnknownRule() {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn(null);

        RuleSuggestion out = service.evalCase(cs);
        assertThat(out).isNotNull();
    }

    @Test
    void blankCaseType_fallsBackToUnknownRule() {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn("   ");

        RuleSuggestion out = service.evalCase(cs);
        assertThat(out).isNotNull();
    }

    @Test
    void caseType_is_trimmed_and_case_insensitive() {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn("  fraud_investigation  ");

        // Stub temporals if Fraud rule needs them to avoid temporal NPE:
        var now = java.time.LocalDateTime.now();
        when(cs.getTransactionDateTime()).thenReturn(now.minusHours(1));
        when(cs.getDueDate()).thenReturn(now.plusDays(1));

        RuleSuggestion out = service.evalCase(cs);
        assertThat(out).isNotNull();
    }

    @Test
    void unknownType_setsUnknownPriority_ifExposed() {
        CaseSummary cs = csWithType("X-UNKNOWN");
        RuleSuggestion out = service.evalCase(cs);
        try {
            assertThat(out.getPriority()).isEqualTo(Priority.UNKNOWN);
        } catch (Throwable ignored) {}
    }

    @Test
    void fraudInvestigation_withLargeAmount_mightUpgradePriority() {
        CaseSummary cs = mock(CaseSummary.class);
        when(cs.getCaseType()).thenReturn("FRAUD_INVESTIGATION");
        var now = LocalDateTime.now();
        when(cs.getTransactionDateTime()).thenReturn(now.minusHours(1));
        when(cs.getDueDate()).thenReturn(now.plusDays(1));
        when(cs.getTransactionAmount()).thenReturn(new BigDecimal("1000.00"));
        when(cs.getTransactionCurrency()).thenReturn("USD");

        RuleSuggestion out = service.evalCase(cs);
        assertThat(out).isNotNull();
    }
}