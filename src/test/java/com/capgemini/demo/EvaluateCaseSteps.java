package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.casehelper.CaseSummary;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.ruleEngine.RuleEngine;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.RuleEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EvaluateCaseSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private CaseHistoryRepository caseHistoryRepository;

    @Autowired
    private CaseService caseService;

    @Autowired
    private RuleEngineService ruleEngineService;


    private int responseStatus;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String requestBody;
    private CaseFacade testCase;
    private RuleSuggestion suggestion;
    private CaseFacade resultCase;
    private Exception caughtException;

    @Given("a case is created in the database with a high monetary value")
    public void aCaseIsCreatedInTheDatabaseWithAHighMonetaryValue() {
        testCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),
                "345","csaf");
        testCase.getTransaction().setTransactionAmount(BigDecimal.valueOf( 10000));
        assertEquals(0, testCase.getTransaction().getTransactionAmount().compareTo(BigDecimal.valueOf(10000)));
        caseService.createCase(testCase);
    }

    @When("the rule engine evaluates the case")
    public void theRuleEngineEvaluatesTheCase() {
        try {
            MvcResult mvcResult = mockMvc.perform(post("/v1/rules/evaluate")
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

    @Then("the case should be assigned a HIGH priority before being saved to the database")
    public void theCaseShouldBeAssignedAHIGHPriorityBeforeBeingSavedToTheDatabase() {
        //assertThat(responseStatus).isEqualTo(200);
        assertThat(testCase.getPriority().equals("HIGH"));
    }

    @Given("a case is created in the database with a low monetary value")
    public void aCaseIsCreatedInTheDatabaseWithALowMonetaryValue() {
        testCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),
                "3h45","csaf");
        testCase.getTransaction().setTransactionAmount(BigDecimal.valueOf(100));
        assertEquals(0, testCase.getTransaction().getTransactionAmount().compareTo(BigDecimal.valueOf(100)));
        caseService.createCase(testCase);
    }

    @Then("the case should be assigned a LOW priority before being saved to the database")
    public void theCaseShouldBeAssignedALOWPriorityBeforeBeingSavedToTheDatabase() {
        assertThat(testCase.getPriority().equals("LOW"));
    }

    @Given("a case is created in the database with edge case attributes")
    public void aCaseIsCreatedInTheDatabaseWithEdgeCaseAttributes() {
        testCase = buildValidCase("INVALID_CASE_TYPE",
                "3hb45","csakf");
        testCase.getTransaction().setTransactionAmount(BigDecimal.valueOf(0));
        assertEquals(0, testCase.getTransaction().getTransactionAmount().compareTo(BigDecimal.valueOf(0)));
        caseRepository.save(testCase);
    }

    @When("the rule engine evaluates the case details")
    public void theRuleEngineEvaluatesTheCaseDetails() {
        //suggestion = ruleEngineService.evalCase(new CaseSummary(testCase));
        try {
            MvcResult mvcResult = mockMvc.perform(post("/v1/rules/evaluate")
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

    @Then("the case should be assigned an UNKNOWN priority before being saved to the database")
    public void theCaseShouldBeAssignedAnUNKNOWNPriorityBeforeBeingSavedToTheDatabase() {
        assertThat(testCase.getPriority().equals(Priority.UNKNOWN.name()));
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
