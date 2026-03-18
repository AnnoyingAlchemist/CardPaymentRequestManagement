package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
//@ExtendWith(MockitoExtension.class)
public class DeleteCaseSteps {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    @InjectMocks
    private CaseService caseService;

    private CaseFacade validCase1;
    private CaseFacade validCase2;

    private Exception caughtException;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        caseService = new CaseService(caseRepository, caseHistoryRepository);

        CaseFacade validCase1 = buildValidCase(29, CaseTypeCode.CARD_STATUS, "2345", "customer1");
        CaseFacade validCase2 = buildValidCase(30, CaseTypeCode.CHARGEBACK, "2457", "customer2");

        //caseService.createCase(validCase1);
        //caseService.createCase(validCase2);

        when(caseRepository.findById(29L)).thenReturn(java.util.Optional.of(validCase1));
        when(caseRepository.findById(30L)).thenReturn(java.util.Optional.of(validCase2));
    }

    @Given("A case with {long} exists in the database to be deleted")
    public void aCaseWithExistsInTheDatabaseToBeDeleted(Long caseId) {
        caseRepository.findById(caseId);
        //caseRepository.save(validCase1);
        //caseRepository.save(validCase2);
        //when(caseService.getCase(caseId)).thenReturn(validCase1);
        //Assertions.assertNotNull(caseService.getCase(caseId));
    }

    @When("I make a DELETE request with the case id to the cases {long} endpoint")
    public void iMakeADELETERequestWithTheCaseIdToTheCasesEndpoint(Long caseId) {
        caughtException = null;
        try {
            caseService.deleteCase(caseId);
        } catch (Exception e) {
            caughtException = e;
        }
        assertThat(caughtException).isNull();
    }
    @Then("the application should delete the case in the database")
    public void theApplicationShouldDeleteTheCaseInTheDatabase() {
        caughtException = null;

        try {
            caseRepository.findById(29L);
        }
        catch (Exception e) {
            caughtException = e;
        }
        assertThat(caughtException).isNotNull();
    }

    @Given("A case with the {long} does not exist")
    public void aCaseWithTheDoesNotExist(Long arg0) {
        when(caseRepository.findById(arg0)).thenThrow(ResponseStatusException.class);
    }

    @When("I make a DELETE request with the updated case to the cases {long} endpoint")
    public void iMakeADELETERequestWithTheUpdatedCaseToTheCasesEndpoint(Long arg0) {
        caseRepository.deleteById(arg0);
    }
    @Then("the application should fail to delete and return status code {int}")
    public void theApplicationShouldFailToDeleteAndReturnStatusCode(int arg0) {
        //caseService.deleteCase((long) 324);
        assertThrows(ResponseStatusException.class, () -> caseService.deleteCase((long) 324));
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

        return c;}





}
