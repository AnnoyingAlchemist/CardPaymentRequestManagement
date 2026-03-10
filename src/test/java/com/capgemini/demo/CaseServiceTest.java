package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.casehelper.CaseHistory;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    CaseRepository repository;

    @Mock
    CaseHistoryRepository historyRepository;

    @Captor
    ArgumentCaptor<CaseFacade> caseCaptor;

    @Captor
    ArgumentCaptor<CaseHistory> historyCaptor;

    CaseService service;

    @BeforeEach
    void setup() {
        service = new CaseService(repository, historyRepository);
    }

    // Helpers
    private static CaseFacade buildCase(String status, String caseType, String txnId, String customerId, String assignee) {
        CaseFacade c = new CaseFacade();

        // classification
        CaseClassification cls = new CaseClassification();
        // If the service expects uppercase statuses, normalize here
        cls.setStatus(status != null ? status.toUpperCase() : null);
        c.setClassification(cls);

        // identifier
        CaseIdentifier id = new CaseIdentifier();
        id.setCaseType(caseType);
        id.setPrimaryTransactionId(txnId);
        id.setCustomerId(customerId);
        c.setIdentifier(id);

        // transaction
        CaseTransaction t = new CaseTransaction();
        t.setTransactionId(txnId);
        t.setTransactionAmount(new BigDecimal("123.45")); // any non-null test value
        t.setCurrency("USD");
        c.setTransaction(t);

        // optional assignment
        if (assignee != null) {
            CaseAssignment assignment = new CaseAssignment();
            assignment.setAssignedTo(assignee);
            assignment.setCreatedAt(LocalDateTime.now().minusHours(2));
            c.setAssignment(assignment);
        }

        return c;
    }

    private static CaseFacade savedCase(long id, String status, String caseType, String txnId, String customerId, String assignee) {
        CaseFacade c = buildCase(status, caseType, txnId, customerId, assignee);
        c.setId(id);
        return c;
    }

    // testcase for validations, conflict, happy path
    @Test
    void createCase_missingCustomerId_400() {
        CaseFacade c = buildCase("open", "FRAUD", "TXN-1", null, "agentA");
        c.getIdentifier().setCustomerId(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createCase(c));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getReason()).contains("Customer ID required");
        verifyNoInteractions(repository, historyRepository);
    }

    @Test
    void createCase_missingCaseTypeTxnOrStatus_400() {
        CaseFacade c = new CaseFacade();
        CaseIdentifier id = new CaseIdentifier();
        id.setCustomerId("C1");
        c.setIdentifier(id);
        c.setClassification(new CaseClassification()); // missing status

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createCase(c));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getReason()).contains("CaseType, TransactionId and Status are required");
        verifyNoInteractions(repository, historyRepository);
    }

    @Test
    void createCase_duplicateConflict_409() {
        CaseFacade c = buildCase("open", "FRAUD", "TXN-1", "C1", "agentA");
        when(repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                "FRAUD", "TXN-1", "OPEN")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createCase(c));
        assertThat(ex.getStatusCode().value()).isEqualTo(409);
        assertThat(ex.getReason()).contains("A case already exists");
        verify(repository, never()).save(any());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void createCase_happyPath_savesAndWritesHistory() {
        CaseFacade c = buildCase("open", "FRAUD", "TXN-2", "C2", "agentA");

        when(repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                "FRAUD", "TXN-2", "OPEN")).thenReturn(false);
        when(repository.save(any(CaseFacade.class))).thenAnswer(inv -> {
            CaseFacade cf = inv.getArgument(0);
            cf.setId(101L);
            return cf;
        });

        CaseFacade out = service.createCase(c);

        assertThat(out.getId()).isEqualTo(101L);
        verify(repository).save(caseCaptor.capture());
        assertThat(caseCaptor.getValue().getClassification().getStatus()).isEqualTo("OPEN");

        verify(historyRepository).save(historyCaptor.capture());
        CaseHistory h = historyCaptor.getValue();
        assertThat(h.getOldStatus()).isNull();
        assertThat(h.getNewStatus()).isEqualTo("OPEN");
        assertThat(h.getComment()).isEqualTo("created");
    }

    // testcase for state machine, duplicates, no-op
    @Test
    void updateStatus_blankOrIllegal_400() {
        // blank
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> service.updateStatus(1L, "   "));
        assertThat(ex1.getStatusCode().value()).isEqualTo(400);

        // illegal
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> service.updateStatus(2L, "NOT_A_STATE"));
        assertThat(ex2.getStatusCode().value()).isEqualTo(400);
        assertThat(ex2.getReason()).contains("Illegal status");
    }

    @Test
    void updateStatus_invalidTransition_409() {
        CaseFacade c = savedCase(3L, "PENDING_CUSTOMER", "FRAUD", "TX", "C", "agentA");
        when(repository.findById(3L)).thenReturn(Optional.of(c));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateStatus(3L, "RESOLVED_BANK_FAVOUR"));
        assertThat(ex.getStatusCode().value()).isEqualTo(409);
        assertThat(ex.getReason()).contains("Invalid transition");
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void updateStatus_noop_sameState_noSaveNoHistory() {
        CaseFacade c = savedCase(4L, "IN_REVIEW", "FRAUD", "TX", "C", "agentA");
        when(repository.findById(4L)).thenReturn(Optional.of(c));

        CaseFacade out = service.updateStatus(4L, " in_review ");
        assertThat(out).isSameAs(c);
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void updateStatus_duplicateCombination_409() {
        CaseFacade c = savedCase(5L, "IN_REVIEW", "FRAUD", "TX-5", "C", "agentA");
        when(repository.findById(5L)).thenReturn(Optional.of(c));
        when(repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                "FRAUD", "TX-5", "PENDING_CUSTOMER"
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateStatus(5L, "pending_customer"));
        assertThat(ex.getStatusCode().value()).isEqualTo(409);
        assertThat(ex.getReason()).contains("A case already exists");
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void updateStatus_validTransition_writesHistoryAndSaves() {
        CaseFacade c = savedCase(6L, "IN_REVIEW", "FRAUD", "TX-6", "C", "agentA");
        when(repository.findById(6L)).thenReturn(Optional.of(c));
        when(repository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                "FRAUD", "TX-6", "PENDING_CUSTOMER"
        )).thenReturn(false);
        when(repository.save(any(CaseFacade.class))).thenAnswer(inv -> inv.getArgument(0));

        CaseFacade out = service.updateStatus(6L, " pending_customer ");

        assertThat(out.getClassification().getStatus()).isEqualTo("PENDING_CUSTOMER");
        verify(historyRepository).save(historyCaptor.capture());
        CaseHistory h = historyCaptor.getValue();
        assertThat(h.getOldStatus()).isEqualTo("IN_REVIEW");
        assertThat(h.getNewStatus()).isEqualTo("PENDING_CUSTOMER");
        assertThat(h.getComment()).isEqualTo("status change");
        verify(repository).save(any(CaseFacade.class));
    }

    @Test
    void updateStatus_initialOnlyOpenAllowed_409() {
        CaseFacade c = savedCase(7L, null, "FRAUD", "TX-7", "C", "agentA");
        c.setClassification(new CaseClassification()); // no status set
        when(repository.findById(7L)).thenReturn(Optional.of(c));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateStatus(7L, "CLOSED"));
        assertThat(ex.getStatusCode().value()).isEqualTo(409);
        assertThat(ex.getReason()).contains("Only OPEN is allowed as the first state");
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // testcase fore normalization and date bounds (including swap)
    @Test
    void searchCases_normalizesAndUsesDefaultBounds() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        when(repository.searchCases(anyString(), anyString(), anyString(), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(Page.empty());

        service.searchCases(" open  ", " FRAUD ", " high ", "  alice  ", null, null, pageable);

        ArgumentCaptor<String> s1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s3 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s4 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDateTime> d1 = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> d2 = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(repository).searchCases(s1.capture(), s2.capture(), s3.capture(), s4.capture(), d1.capture(), d2.capture(), eq(pageable));

        assertThat(s1.getValue()).isEqualTo("OPEN");   // status upper
        assertThat(s2.getValue()).isEqualTo("FRAUD");  // caseType trimmed
        assertThat(s3.getValue()).isEqualTo("HIGH");   // priority upper
        assertThat(s4.getValue()).isEqualTo("alice");  // assignee trimmed
        assertThat(d1.getValue()).isEqualTo(LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0));
        assertThat(d2.getValue()).isEqualTo(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59, 59));
    }

    @Test
    void searchCases_invertedDates_areSwapped() {
        Pageable pageable = PageRequest.of(0, 5);
        LocalDateTime from = LocalDateTime.of(2025, 1, 10, 10, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 0);
        when(repository.searchCases(any(), any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(Page.empty());

        service.searchCases("OPEN", "FRAUD", "HIGH", "alice", from, to, pageable);

        ArgumentCaptor<LocalDateTime> d1 = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> d2 = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).searchCases(anyString(), anyString(), anyString(), anyString(), d1.capture(), d2.capture(), eq(pageable));

        assertThat(d1.getValue()).isEqualTo(to);
        assertThat(d2.getValue()).isEqualTo(from);
    }

    // testcase for deletes case and its history
    @Test
    void deleteCase_deletesCaseAndHistory() {
        when(repository.findById(20L)).thenReturn(Optional.of(savedCase(20L, "OPEN", "FRAUD", "TX", "C", "x")));
        service.deleteCase(20L);
        verify(repository).deleteById(20L);
        verify(historyRepository).deleteByCaseId(20L);
    }

    // testcase for 404 (and simple found check)
    @Test
    void getCase_404_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getCase(99L));
        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        assertThat(ex.getReason()).contains("not found");
    }

    @Test
    void getCase_found_returnsEntity() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedCase(1L, "OPEN", "FRAUD", "TX-1", "C1", "a")));
        CaseFacade c = service.getCase(1L);
        assertThat(c.getId()).isEqualTo(1L);
        assertThat(c.getClassification().getStatus()).isEqualTo("OPEN");
    }
}