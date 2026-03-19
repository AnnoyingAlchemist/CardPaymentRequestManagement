package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.PendingException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class ReadCaseSteps {
    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    @InjectMocks
    private CaseService caseService;


    private List<CaseFacade> seededCases;
    private CaseFacade singleCase;
    private List<CaseFacade> returnedCases;
    private CaseFacade returnedSingleCase;
    private Exception caughtException;
    private Long requestedId;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        caseService = new CaseService(caseRepository, caseHistoryRepository);

        seededCases = new ArrayList<>();
        seededCases.add(buildValidCase(11, CaseTypeCode.CARD_STATUS, "1111", "cust111"));
        seededCases.add(buildValidCase(12, CaseTypeCode.CHARGEBACK, "2222", "cust222"));

        // Stub repository for findAll
        when(caseRepository.findAll()).thenReturn(seededCases);
    }


    @Given("There exist cases in the database")
    public void thereExistCasesInTheDatabase() {
        // Already stubbed in setup
        assertThat(seededCases).isNotEmpty();
    }

    @When("I send a GET request to cases endpoint to read a case")
    public void iSendAGETRequestToCasesEndpointToReadACase() {
        caughtException = null;
        try {
            returnedCases = caseService.getAllCases();
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return a list of all cases")
    public void theApplicationShouldReturnAListOfAllCases() {
        assertThat(caughtException).isNull();
        assertThat(returnedCases).isNotNull();
        assertThat(returnedCases.size()).isEqualTo(seededCases.size());
    }

    @Given("There do not exist any cases in the database")
    public void thereDoNotExistAnyCasesInTheDatabase() {
        seededCases = Collections.emptyList();
        when(caseRepository.findAll()).thenReturn(seededCases);
    }

    @When("I make a GET request to cases to read cases from an empty database")
    public void iMakeAGETRequestToCasesToReadCasesFromAnEmptyDatabase() {
        caughtException = null;
        try {
            returnedCases = caseService.getAllCases();
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return status code {int}")
    public void theApplicationShouldReturnStatusCode(int expectedStatus) {

        if (expectedStatus == 404) {
            assertThrows(ResponseStatusException.class, () -> {
                throw caughtException;
            });
        } else if (expectedStatus == 204) {
            assertThat(returnedCases).isEmpty();
        }

    }

    @Given("A case with the {string} exists in the database")
    public void aCaseWithTheExistsInTheDatabase(String caseNumber) {
        singleCase = buildValidCase(55, CaseTypeCode.CARD_STATUS, caseNumber, "custTest");
        when(caseRepository.findById(55L)).thenReturn(Optional.of(singleCase));
        this.requestedId = 55L;
    }

    @When("I send a GET request to cases endpoint to read a certain case")
    public void iSendAGETRequestToCasesEndpointToReadACertainCase() {
        caughtException = null;
        try {
            returnedSingleCase = caseService.getCase(requestedId);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return the case that matches the {string}")
    public void theApplicationShouldReturnTheCaseThatMatchesThe(String caseNumber) {
        assertThat(caughtException).isNull();
        assertThat(returnedSingleCase).isNotNull();
        assertThat(returnedSingleCase.getIdentifier().getPrimaryTransactionId())
                .isEqualTo(caseNumber);
    }

    @Given("A case with the {string} does not exist in the database")
    public void aCaseWithTheDoesNotExistInTheDatabase(String caseNumber) {
        // store the mapping from the human "case number" string to a test ID
        long nonExistentId = 999L;
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        // Save the id in a field
        this.requestedId = nonExistentId;

    }

    @When("I make a GET request to cases endpoint for a non-existent case")
    public void iMakeAGETRequestToCasesEndpointForANonExistentCase() {
        caughtException = null;
        try {
            returnedSingleCase = caseService.getCase(requestedId);
        } catch (Exception e) {
            caughtException = e;
        }
    }


    private CaseFacade buildValidCase(long caseId, CaseTypeCode caseType, String txnId, String customerId) {
        CaseFacade c = new CaseFacade();

        c.setId(caseId);

        CaseClassification classification = new CaseClassification();
        classification.setStatus(CaseStatusCode.OPEN.name());
        classification.setPriority(Priority.MEDIUM.name());
        classification.setDueDate(LocalDateTime.now().plusDays(1));
        c.setClassification(classification);

        CaseIdentifier identifier = new CaseIdentifier();
        identifier.setCaseType(caseType.name());
        identifier.setPrimaryTransactionId(txnId);
        identifier.setCustomerId(customerId);
        c.setIdentifier(identifier);

        CaseAssignment assignment = new CaseAssignment();
        assignment.setCreatedBy("SYSTEM");
        assignment.setAssignedTo("agent01");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        c.setAssignment(assignment);

        CaseTransaction transaction = new CaseTransaction();
        transaction.setTransactionDateTime(LocalDateTime.now().minusDays(2));
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        c.setOutcome(outcome);

        return c;
    }
}
