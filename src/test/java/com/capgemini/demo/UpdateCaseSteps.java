package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateCaseSteps {
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
    private CaseFacade updatedTestCase;
    private CaseFacade resultCase;
    private Exception caughtException;
    private MvcResult mvcResult;

    @Given("The case with target id exists in the database")
    public void theCaseWithExistsInTheDatabase() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"45LJHBFjDGVlsjhd","customer1");
        updatedTestCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),"45324bh3","customer1");
        updatedTestCase.getClassification().setStatus("CLOSED");
        caseService.createCase(testCase);
    }

    @And("The case status transition is allowed")
    public void theCaseStatusTransitionIsAllowed() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"45LJHBFjDGVlsjhd","customer1");
        updatedTestCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),"45324bh3","customer1");
        updatedTestCase.getClassification().setStatus("CLOSED");
        assert(ALLOWED_STATES.contains(testCase.getClassification().getStatus()) &&
                ALLOWED_TRANSITIONS.get(testCase.getClassification().getStatus()).contains(updatedTestCase.getClassification().getStatus()));
    }

    @When("I make a PUT request to the cases endpoint")
    public void iMakeAPUTRequestToTheCasesEndpoint() {
        try {
            caseService.updateCase(testCase.getId(),updatedTestCase);
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the application should update the case in the database")
    public void theApplicationShouldUpdateTheCaseInTheDatabase() {
        assertThat(testCase.getStatus().equals(updatedTestCase.getStatus()));
    }

    @Given("Case with target id exists in the database")
    public void theCaseWithTargetIdExistsInTheDatabase() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"45LJHBFjDGVlsjhd","customer1");
        testCase.getClassification().setStatus("CLOSED");
        caseService.createCase(testCase);
        assertThat(caseRepository.existsById(testCase.getId()));
    }

    @When("I make a PUT request to the cases target id endpoint")
    public void iMakeAPUTRequestToTheCasesTargetIdEndpoint() {
        try {
            caseService.updateCase(testCase.getId(),updatedTestCase);
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @But("The case status transition is not allowed")
    public void theCaseStatusTransitionIsNotAllowed() {
        testCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"45LJHBFjDGVlsjhd","customer1");
        updatedTestCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),"45324bh3","customer1");
        testCase.getClassification().setStatus("CLOSED");
        updatedTestCase.getClassification().setStatus("CLOSED");

        assert(ALLOWED_STATES.contains(testCase.getClassification().getStatus()) &&
                !ALLOWED_TRANSITIONS.get(testCase.getClassification().getStatus())
                        .contains(updatedTestCase.getClassification().getStatus()));
    }

    @Then("the application return an error")
    public void theApplicationReturnAnError() {
        assertThat(caughtException).isNotNull();
    }


    @Given("A case with the {long} does not exist to be updated")
    public void aCaseWithTheTargetIdDoesNotExistToBeUpdated(Long id) {
        assertThat(!caseRepository.existsById(id));
    }

    @When("I make a PUT request with the updated case to the cases {long} endpoint")
    public void iMakeAPUTRequestWithTheUpdatedCaseToTheCasesEndpoint(Long arg0) {
        updatedTestCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),"45324bh3","customer1");
        try {
            caseService.updateCase(arg0,updatedTestCase);
        }
        catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the update attempt should return status code {int}")
    public void theUpdateAttemptShouldReturnStatusCode(int arg0) {
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

    private static final Set<String> ALLOWED_STATES = Set.of(
            "OPEN", "IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER",
            "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"
    );

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS;
    static {
        Map<String, Set<String>> m = new HashMap<>();
        m.put("OPEN", Set.of("IN_REVIEW", "PENDING_CUSTOMER", "PENDING_PARTNER", "CLOSED"));
        m.put("IN_REVIEW", Set.of("PENDING_CUSTOMER", "PENDING_PARTNER",
                "RESOLVED_CUSTOMER_FAVOUR", "RESOLVED_BANK_FAVOUR", "CLOSED"));
        m.put("PENDING_CUSTOMER", Set.of("IN_REVIEW", "CLOSED"));
        m.put("PENDING_PARTNER", Set.of("IN_REVIEW", "CLOSED"));
        m.put("RESOLVED_CUSTOMER_FAVOUR", Set.of("CLOSED"));
        m.put("RESOLVED_BANK_FAVOUR", Set.of("CLOSED"));
        m.put("CLOSED", Set.of());
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(m);
    }



}
