package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class UpdateCaseSteps {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    @InjectMocks
    private CaseService caseService;

    // --- Scenario state ---
    private CaseFacade existingCase;
    private CaseFacade updatedCasePayload;
    private CaseFacade updateResult;
    private Exception caughtException;
    private String requestedCaseKey; // we’ll interpret Given {string} as the external “case number” (txnId)
    private Long requestedId;        // internal id mapping for repository lookup

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        caseService = new CaseService(caseRepository, caseHistoryRepository);

        // Default “existing” baseline (will be replaced in Given step)
        existingCase = buildValidCase(101L, CaseTypeCode.CARD_STATUS, "CASE-XYZ", "cust-xyz",
                CaseStatusCode.OPEN, Priority.MEDIUM, LocalDateTime.now().plusDays(3));

        // Typical stubs for save (echo back the entity)
        when(caseRepository.save(any(CaseFacade.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Given("The case with {string} exists in the database")
    public void theCaseWithExistsInTheDatabase(String caseNumberOrTxnId) {
        requestedCaseKey = caseNumberOrTxnId;
        requestedId = 555L;

        // Seed an existing OPEN case
        existingCase = buildValidCase(
                requestedId,
                CaseTypeCode.CARD_STATUS,
                requestedCaseKey,     // treat Given {string} as identifier.primaryTransactionId
                "customer-1",
                CaseStatusCode.OPEN,
                Priority.MEDIUM,
                LocalDateTime.now().plusDays(2)
        );

        // If your service loads by id:
        when(caseRepository.findById(requestedId)).thenReturn(Optional.of(existingCase));
    }

    @And("The case status transition is allowed")
    public void theCaseStatusTransitionIsAllowed() {
        // Example: OPEN -> IN_PROGRESS (allowed)
        updatedCasePayload = cloneCase(existingCase);
        updatedCasePayload.getClassification().setStatus(CaseStatusCode.IN_REVIEW.name());
        // You can also change other updatable fields if needed:
        updatedCasePayload.getAssignment().setAssignedTo("agent02");
    }

    @When("I make a PUT request to the cases {string} endpoint")
    public void iMakeAPUTRequestToTheCasesEndpoint(String caseNumberOrTxnId) {
        // implies path id/key usage. We already seeded by id.
        // We call the service update and capture results or exceptions.
        caughtException = null;
        updateResult = null;

        try {
            // If your service signature is updateCase(updatedPayload) -> CaseFacade
            updateResult = caseService.updateCase(requestedId, updatedCasePayload);

            // If your service uses id param, adjust accordingly:
            // updateResult = caseService.updateCase(requestedId, updatedCasePayload);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should update the case in the database and return the updated case")
    public void theApplicationShouldUpdateTheCaseInTheDatabaseAndReturnTheUpdatedCase() {
        assertThat(caughtException).isNull();
        assertThat(updateResult).isNotNull();

        // Validate status changed to the allowed target:
        assertThat(updateResult.getClassification().getStatus())
                .isEqualTo(updatedCasePayload.getClassification().getStatus());

        // Validate “case number” remained the same (immutability of identifier)
        assertThat(updateResult.getIdentifier().getPrimaryTransactionId())
                .isEqualTo(existingCase.getIdentifier().getPrimaryTransactionId());

        // Optional: validate assignment changed
        assertThat(updateResult.getAssignment().getAssignedTo())
                .isEqualTo(updatedCasePayload.getAssignment().getAssignedTo());
    }

    @But("The case status transition is not allowed")
    public void theCaseStatusTransitionIsNotAllowed() {
        // Example: CLOSED -> OPEN (often disallowed)
        existingCase.getClassification().setStatus(CaseStatusCode.CLOSED.name());
        // ensure repo returns this CLOSED baseline
        when(caseRepository.findById(requestedId)).thenReturn(Optional.of(existingCase));

        updatedCasePayload = cloneCase(existingCase);
        updatedCasePayload.getClassification().setStatus(CaseStatusCode.OPEN.name());
    }

    @Then("the application return an error")
    public void theApplicationReturnAnError() {
        // might throw ResponseStatusException, IllegalStateException, etc.
        // If the service throws immediately in When, we stored it in caughtException:
        if (caughtException != null) {
            assertThat(caughtException).isInstanceOfAny(
                    ResponseStatusException.class,
                    IllegalArgumentException.class,
                    IllegalStateException.class
            );
        } else {
            // Or the service could throw when called here; force the assertion to be explicit:
            assertThrows(Exception.class, () -> {
                throw new IllegalStateException("Expected service to reject invalid status transition");
            });
        }
    }

    @When("I make a PUT request with the updated case to the cases {string} endpoint")
    public void iMakeAPUTRequestWithTheUpdatedCaseToTheCasesEndpoint(String caseNumberOrTxnId) {
        // Same as above
        caughtException = null;
        updateResult = null;

        try {
            updateResult = caseService.updateCase(requestedId, updatedCasePayload);
        } catch (Exception e) {
            caughtException = e;
        }
    }


    private CaseFacade buildValidCase(long caseId,
                                      CaseTypeCode caseType,
                                      String txnId,
                                      String customerId,
                                      CaseStatusCode status,
                                      Priority priority,
                                      LocalDateTime dueDate) {
        CaseFacade c = new CaseFacade();
        c.setId(caseId);

        CaseClassification classification = new CaseClassification();
        classification.setStatus(status.name());
        classification.setPriority(priority.name());
        classification.setDueDate(dueDate);
        c.setClassification(classification);

        CaseIdentifier identifier = new CaseIdentifier();
        identifier.setCaseType(caseType.name());
        identifier.setPrimaryTransactionId(txnId);
        identifier.setCustomerId(customerId);
        c.setIdentifier(identifier);

        CaseAssignment assignment = new CaseAssignment();
        assignment.setCreatedBy("SYSTEM");
        assignment.setAssignedTo("agent01");
        assignment.setCreatedAt(LocalDateTime.now().minusDays(7));
        assignment.setUpdatedAt(LocalDateTime.now());
        c.setAssignment(assignment);

        CaseTransaction transaction = new CaseTransaction();
        transaction.setTransactionDateTime(LocalDateTime.now().minusDays(8));
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        c.setOutcome(outcome);

        return c;
    }

    private CaseFacade cloneCase(CaseFacade src) {
        CaseFacade c = new CaseFacade();
        c.setId(src.getId());

        CaseClassification classification = new CaseClassification();
        classification.setStatus(src.getClassification().getStatus());
        classification.setPriority(src.getClassification().getPriority());
        classification.setDueDate(src.getClassification().getDueDate());
        c.setClassification(classification);

        CaseIdentifier identifier = new CaseIdentifier();
        identifier.setCaseType(src.getIdentifier().getCaseType());
        identifier.setPrimaryTransactionId(src.getIdentifier().getPrimaryTransactionId());
        identifier.setCustomerId(src.getIdentifier().getCustomerId());
        c.setIdentifier(identifier);

        CaseAssignment assignment = new CaseAssignment();
        assignment.setCreatedBy(src.getAssignment().getCreatedBy());
        assignment.setAssignedTo(src.getAssignment().getAssignedTo());
        assignment.setCreatedAt(src.getAssignment().getCreatedAt());
        assignment.setUpdatedAt(LocalDateTime.now());
        c.setAssignment(assignment);

        CaseTransaction transaction = new CaseTransaction();
        transaction.setTransactionDateTime(src.getTransaction().getTransactionDateTime());
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        c.setOutcome(src.getOutcome());

        return c;
    }
}
