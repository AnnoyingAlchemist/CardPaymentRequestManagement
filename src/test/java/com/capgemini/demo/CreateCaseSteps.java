package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@CucumberContextConfiguration
@SpringBootTest
public class CreateCaseSteps {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    private CaseService caseService;
    private CaseFacade testCase;
    private CaseFacade resultCase;
    private Exception caughtException;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        caseService = new CaseService(caseRepository, caseHistoryRepository);
    }

    @Given("I have a case with attributes that match the database design")
    public void iHaveACaseWithAttributesThatMatchTheDatabaseDesign() {
        testCase = buildValidCase("CARD_FRAUD", "TXN123456", "CUST001");
    }

    @When("I make a POST request with my case to the case controller")
    public void iMakeAPOSTRequestWithMyCaseToTheCaseController() {
        caughtException = null;
        // No duplicate exists
        when(caseRepository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                any(), any(), eq("OPEN")
        )).thenReturn(false);

        // Repository saves and returns the case with an ID
        when(caseRepository.save(any(CaseFacade.class))).thenAnswer(invocation -> {
            CaseFacade saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        try {
            resultCase = caseService.createCase(testCase);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should create the case in the database and return status code {int}")
    public void theApplicationShouldCreateTheCaseInTheDatabaseAndReturnStatusCode(int expectedStatus) {
        assertThat(caughtException).isNull();
        assertThat(resultCase).isNotNull();
        assertThat(resultCase.getId()).isNotNull();
        assertThat(expectedStatus).isEqualTo(HttpStatus.OK.value());
        verify(caseRepository, times(1)).save(any(CaseFacade.class));
    }

    @Given("I have a case with attributes that do not match the database design or constraints")
    public void iHaveACaseWithAttributesThatDoNotMatchTheDatabaseDesignOrConstraints() {
        // Missing required fields (e.g. no customerId, no caseType)
        testCase = new CaseFacade();
        testCase.setClassification(new CaseClassification());
        testCase.setIdentifier(new CaseIdentifier());
        testCase.setAssignment(new CaseAssignment());
        testCase.setTransaction(new CaseTransaction());
        testCase.setOutcome(new CaseOutcome());
    }

    @When("I make POST request with my case to the case controller")
    public void iMakePOSTRequestWithMyCaseToTheCaseController() {
        caughtException = null;
        try {
            resultCase = caseService.createCase(testCase);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should not create the case in the database and return an error")
    public void theApplicationShouldNotCreateTheCaseInTheDatabaseAndReturnAnError() {
        assertThat(caughtException).isNotNull();
        assertThat(caughtException).isInstanceOf(ResponseStatusException.class);
        verify(caseRepository, never()).save(any(CaseFacade.class));
    }

    @Given("I have a case in the database with a transaction id and type")
    public void iHaveACaseInTheDatabaseWithATransactionIdAndType() {
        testCase = buildValidCase("CARD_FRAUD", "TXN123456", "CUST001");

        // Simulate that an OPEN case with the same type and transaction already exists
        when(caseRepository.existsByIdentifier_CaseTypeAndIdentifier_PrimaryTransactionIdAndClassification_Status(
                eq("CARD_FRAUD"), eq("TXN123456"), eq("OPEN")
        )).thenReturn(true);
    }

    @When("I make a request to the case service to create another OPEN case of the same type")
    public void iMakeARequestToTheCaseServiceToCreateAnotherOPENCaseOfTheSameType() {
        caughtException = null;
        CaseFacade duplicateCase = buildValidCase("CARD_FRAUD", "TXN123456", "CUST001");

        try {
            resultCase = caseService.createCase(duplicateCase);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return an error and not create the case")
    public void theApplicationShouldReturnAnErrorAndNotCreateTheCase() {
        assertThat(caughtException).isNotNull();
        assertThat(caughtException).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException ex = (ResponseStatusException) caughtException;
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(caseRepository, never()).save(any(CaseFacade.class));
    }

    // --- Helper ---

    private CaseFacade buildValidCase(String caseType, String txnId, String customerId) {
        CaseFacade c = new CaseFacade();

        CaseClassification classification = new CaseClassification();
        classification.setStatus("OPEN");
        classification.setPriority("MEDIUM");
        c.setClassification(classification);

        CaseIdentifier identifier = new CaseIdentifier();
        identifier.setCaseType(caseType);
        identifier.setPrimaryTransactionId(txnId);
        identifier.setCustomerId(customerId);
        c.setIdentifier(identifier);

        CaseAssignment assignment = new CaseAssignment();
        assignment.setCreatedBy("SYSTEM");
        assignment.setAssignedTo("agent01");
        c.setAssignment(assignment);

        CaseTransaction transaction = new CaseTransaction();
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        c.setOutcome(outcome);

        return c;
    }
}