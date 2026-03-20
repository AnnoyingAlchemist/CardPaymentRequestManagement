package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.ReportingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportServiceSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private CaseHistoryRepository caseHistoryRepository;

    @Autowired
    private CaseService caseService;

    @Autowired
    ReportingService reportingService;

    private int responseStatus;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String requestBody;
    private CaseFacade testCase;
    private CaseFacade overdueCase;
    private CaseFacade nearDueCase;
    private Exception caughtException;
    private List<CaseFacade> allCases;

    @Before
    public void setUp() {
        overdueCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"4aegtreah3","customer1");
        overdueCase.getClassification().setDueDate(LocalDateTime.now().minusDays(1));
        nearDueCase = buildValidCase(CaseTypeCode.CARD_STATUS.name(),"kaejyth","customer2");
        nearDueCase.getClassification().setDueDate(LocalDateTime.now().plusDays(1));
        testCase = buildValidCase(CaseTypeCode.CHARGEBACK.name(),"80dg08sf5","customer3");
        testCase.getClassification().setDueDate(LocalDateTime.now().plusDays(10));
        objectMapper.registerModule(new JavaTimeModule());
        allCases = caseService.getAllCases();
    }

    @Given("There are overdue cases in the database")
    public void thereAreOverdueCasesInTheDatabase() {
        caseService.createCase(testCase);
        caseService.createCase(overdueCase);
        caseService.createCase(nearDueCase);
        assertThat(caseRepository.existsById(overdueCase.getId()));
    }

    @When("I call the reporting endpoint to view overdue cases,")
    public void iCallTheReportingEndpointToViewOverdueCases() {
        assertThat(!reportingService.getCaseBacklogReport(allCases).isEmpty());
    }

    @Then("I should be able to see the number of overdue cases")
    public void iShouldBeAbleToSeeTheNumberOfOverdueCases() {
        List<Map<String,Integer>> backlog = reportingService.getCaseBacklogReport(allCases);
        assertThat(backlog.get(1).get("OVERDUE").equals(1));
    }

    @And("I should see the number of nearly overdue cases")
    public void iShouldSeeTheNumberOfNearlyOverdueCases() {
        List<Map<String,Integer>> backlog = reportingService.getCaseBacklogReport(allCases);
        assertThat(backlog.get(1).get("NearDUE").equals(1));
    }

    @Given("There are open cases in the database")
    public void thereAreOpenCasesInTheDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I call the reporting service to view the case backlog")
    public void iCallTheReportingServiceToViewTheCaseBacklog() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("I should see all open cases by type and priority")
    public void iShouldSeeAllOpenCasesByTypeAndPriority() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("There are closed cases in the database")
    public void thereAreClosedCasesInTheDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I call the reporting service")
    public void iCallTheReportingService() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("I should see all closed cases sorted by resolution")
    public void iShouldSeeAllClosedCasesSortedByResolution() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
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
        transaction.setTransactionAmount(BigDecimal.valueOf(100.00));
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        outcome.setResolution("RESOLVED");
        c.setOutcome(outcome);

        return c;
    }

}
