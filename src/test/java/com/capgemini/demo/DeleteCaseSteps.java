package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DeleteCaseSteps {
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

    @BeforeEach
    public void initCase(){
        caseService.createCase(testCase);
    }

    @When("I make a DELETE request with the case id to the cases {string} endpoint")
    public void iMakeADELETERequestWithTheCaseIdToTheCasesEndpoint(String arg0) {
        try {
            caseService.deleteCase(testCase.getId());
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should delete the case in the database")
    public void theApplicationShouldDeleteTheCaseInTheDatabase() {
        assertThat(caseService.getCase(testCase.getId())).isNull();
    }

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

        return c;}


}
