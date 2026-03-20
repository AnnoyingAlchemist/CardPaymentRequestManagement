package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.service.CaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "admin", roles = {"SYSTEM"})
public class DeleteCaseSteps {

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
    private CaseFacade testCase1;
    private CaseFacade testCase2;
    private CaseFacade resultCase;
    private Exception caughtException;


    @Before
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Given("A case with target id exists in the database to be deleted")
    public void aCaseWithExistsInTheDatabaseToBeDeleted() {
        testCase1 = buildValidCase(CaseTypeCode.CARD_STATUS,
                "gw43","customer1");
        caseRepository.save(testCase1);

        testCase2 = buildValidCase(CaseTypeCode.CARD_STATUS,
                "w3h","customer1");
        caseRepository.save(testCase2);
    }

    @When("I make a DELETE request with the case id to the cases target id endpoint")
    public void iMakeADELETERequestWithTheCaseIdToTheCasesEndpoint() {
        caughtException = null;
        try {
            mockMvc.perform(delete("/v1/cases/{id}",testCase1.getId()))
                    .andExpect(status().isOk()).andReturn();

            mockMvc.perform(delete("/v1/cases/{id}",testCase2.getId()))
                    .andExpect(status().isOk()).andReturn();

        }
        catch (Exception e) {
            caughtException = e;
        }
    }
    @Then("the application should delete the case with target id in the database")
    public void theApplicationShouldDeleteTheCaseInTheDatabase() {
        assertThat(caughtException).isNull();
        assertThat(caseRepository.existsById(testCase1.getId())).isFalse();
        assertThat(caseRepository.existsById(testCase2.getId())).isFalse();
    }

    @Given("A case with the {long} does not exist")
    public void aCaseWithTheDoesNotExist(Long arg0) {
        try {
            caseService.getCase(arg0);
        }
        catch (Exception e) {
            caughtException = e;
        }
        assertThat(caughtException.getMessage()).contains("Case with ID " + arg0 + " not found");
    }

    @When("I make a DELETE request with the updated case to the cases {long} endpoint")
    public void iMakeADELETERequestWithTheUpdatedCaseToTheCasesEndpoint(Long arg0) {
        caughtException = null;
        try {
            MvcResult mvcResult = mockMvc.perform(delete("/v1/cases/{id}",arg0))
                    .andExpect(status().is4xxClientError()).andReturn();

            responseStatus = mvcResult.getResponse().getStatus();
        }
        catch (Exception e) {
            caughtException = e;
        }
    }
    @Then("the application should fail to delete and return status code {int}")
    public void theApplicationShouldFailToDeleteAndReturnStatusCode(int arg0) {
        assertThat(responseStatus).isEqualTo(arg0);
    }


    private CaseFacade buildValidCase(CaseTypeCode caseType, String txnId, String customerId) {
        CaseFacade c = new CaseFacade();

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
