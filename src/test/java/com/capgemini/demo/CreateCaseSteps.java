package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date.*;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest
public class CreateCaseSteps {

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
    private CaseFacade resultCase;
    private Exception caughtException;


    @Before
    public void setUp() {
        //caseRepository.deleteAll();
        objectMapper.registerModule(new JavaTimeModule());
    }



    @Given("I have a case with attributes that match the database design")
    public void iHaveACaseWithAttributesThatMatchTheDatabaseDesign() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"1243","customer1");
    }

    @WithMockUser(username = "admin", roles = {"SYSTEM"})

    @When("I make a POST request with my case to the case controller")
    public void iMakeAPOSTRequestWithMyCaseToTheCaseController() {
        try {
            MvcResult mvcResult = mockMvc.perform(post("/v1/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andExpect(status().isOk())
                .andReturn();
            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }


    }

    @Then("the application should create the case in the database and return status code {int}")
    public void theApplicationShouldCreateTheCaseInTheDatabaseAndReturnStatusCode(int expectedStatus) throws JsonProcessingException {
        assertThat(caughtException).isNull();
        assertThat(responseStatus).isEqualTo(expectedStatus);
    }

    @Given("I have a case with attributes that do not match the database design or constraints")
    public void iHaveACaseWithAttributesThatDoNotMatchTheDatabaseDesignOrConstraints() {
        testCase = buildValidCase("INVALID_CASE_TYPE","1243","customer2");
        testCase.getClassification().setStatus("INCORRECT_STATUS");
        testCase.getClassification().setPriority("INCORRECT_PRIORITY");
    }

    @When("I make POST request with my case to the case controller")
    public void iMakePOSTRequestWithMyCaseToTheCaseController() {
        try {
            MvcResult mvcResult = mockMvc.perform(post("/v1/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCase)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should not create the case in the database and return an error")
    public void theApplicationShouldNotCreateTheCaseInTheDatabaseAndReturnAnError() {
        assertThat(caughtException).isNotNull();
        //assertThat(responseStatus).isEqualTo(400);
    }

    @Given("I have a case in the database with a transaction id and type")
    public void iHaveACaseInTheDatabaseWithATransactionIdAndType() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"5005","customer3");
        caseService.createCase(testCase);
    }

    @When("I make a request to the case service to create another OPEN case of the same type")
    public void iMakeARequestToTheCaseServiceToCreateAnotherOPENCaseOfTheSameType() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"5005","customer4");
        testCase.getClassification().setStatus("OPEN");
        testCase.setId(2L);
        caughtException = null;
        try {
            caseService.createCase(testCase);
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should return an error and not create the case")
    public void theApplicationShouldReturnAnErrorAndNotCreateTheCase() {
        assertThat(caughtException).isNotNull();
        assertThat(caughtException.getMessage()).contains("A case already exists");
        assertThat(!caseRepository.existsById(testCase.getId()));
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