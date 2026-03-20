package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ReadCaseSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private CaseHistoryRepository caseHistoryRepository;

    @Autowired
    private CaseService caseService;

    private int responseStatus;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String requestBody;
    private CaseFacade testCase;
    private CaseFacade testCase2;
    private CaseFacade resultCase;
    private Exception caughtException;
    private MvcResult mvcResult;

    @Given("There exist cases in the database")
    public void thereExistCasesInTheDatabase() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"453243","customer1");
        caseService.createCase(testCase);
        testCase2 = buildValidCase(CaseTypeCode.CHARGEBACK.name(),"243688","customer2");
        caseService.createCase(testCase2);

        assertThat(!caseService.getAllCases().isEmpty());
    }

    @When("I send a GET request to cases endpoint to read a case")
    public void iSendAGETRequestToCasesEndpointToReadACase() {
        try {
            mvcResult = mockMvc.perform(get("/v1/cases"))
                    .andExpect(status().isOk())
                    .andReturn();
            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return a list of all cases")
    public void theApplicationShouldReturnAListOfAllCases() throws UnsupportedEncodingException {
        assertThat(caughtException).isNull();
        assertThat(mvcResult.getResponse().getContentAsString().contains("customer1"));
        assertThat(mvcResult.getResponse().getContentAsString().contains("customer2"));
    }

    @Given("There do not exist any cases in the database")
    public void thereDoNotExistAnyCasesInTheDatabase() {
        assertThat(caseService.getAllCases().isEmpty());
    }

    @When("I make a GET request to cases to read cases from an empty database")
    public void iMakeAGETRequestToCasesToReadCasesFromAnEmptyDatabase() {
        try {
            mvcResult = mockMvc.perform(get("/v1/cases"))
                    .andExpect(status().isOk())
                    .andReturn();
            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return empty collection of cases")
    public void theApplicationShouldReturnEmptyCollectionOfCases() {
        assertThat(caseService.getAllCases().isEmpty());
    }

    @Given("A case with the target id exists in the database")
    public void aCaseWithTheExistsInTheDatabase(String arg0) {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"453243","customer1");
        testCase.setId(1L);
        caseRepository.save(testCase);
        assertThat(caseRepository.existsById(testCase.getId()));
    }

    @When("I send a GET request to cases endpoint to read a certain case")
    public void iSendAGETRequestToCasesEndpointToReadACertainCase() {
        try {
            mvcResult = mockMvc.perform(get("/v1/cases/{id}",1L))
                    .andExpect(status().isOk())
                    .andReturn();
            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return the case that matches the target id")
    public void theApplicationShouldReturnTheCaseThatMatchesThe() {
        assertThat(caughtException).isNull();
        assertThat(caseRepository.existsById(1L));
    }

    @Given("A case with the {long} does not exist in the database")
    public void aCaseWithTheDoesNotExistInTheDatabase(Long arg0) {
        assertThat(!caseRepository.existsById(arg0));
    }

    @When("I make a GET request to cases endpoint for a non-existent case")
    public void iMakeAGETRequestToCasesEndpointForANonExistentCase() {
        try {
            mvcResult = mockMvc.perform(get("/v1/cases/{id}",10345890L))
                    .andExpect(status().is4xxClientError())
                    .andReturn();
            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return status code {int}")
    public void theApplicationShouldReturnStatusCode(int arg0) {
        assertThat(responseStatus).isEqualTo(arg0);
    }

    private CaseFacade buildValidCase(String caseType, String txnId, String customerId) {
        CaseFacade c = new CaseFacade();

        CaseClassification classification = new CaseClassification();
        classification.setStatus("OPEN");
        classification.setPriority("MEDIUM");
        classification.setDueDate(LocalDateTime.now().plusDays(1));
        c.setClassification(classification);

        CaseIdentifier identifier = new CaseIdentifier();
        identifier.setCaseType(caseType);
        identifier.setPrimaryTransactionId(txnId);
        identifier.setCustomerId(customerId);
        c.setIdentifier(identifier);

        CaseAssignment assignment = new CaseAssignment();
        assignment.setCreatedBy("SYSTEM");
        assignment.setAssignedTo("agent01");
        assignment.setCreatedAt(LocalDateTime.now());
        c.setAssignment(assignment);

        CaseTransaction transaction = new CaseTransaction();
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        c.setOutcome(outcome);

        return c;
    }


}
