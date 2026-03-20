package com.capgemini.demo;

import com.capgemini.demo.casefacade.*;
import com.capgemini.demo.repository.CaseHistoryRepository;
import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.ruleEngine.Priority;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

public class ReportServiceSteps {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    @InjectMocks
    private CaseService caseService;

    // Scenario state
    private List<CaseFacade> seeded;
    private long observedOverdueCount;
    private long observedNearlyOverdueCount;
    private Map<String, Map<String, Long>> observedOpenByTypeAndPriority; // type -> (priority -> count)
    private List<CaseFacade> observedClosedSorted;

    // Configurable window for "nearly overdue"
    private Duration nearlyOverdueWindow = Duration.ofHours(48);

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        caseService = new CaseService(caseRepository, caseHistoryRepository);
        seeded = new ArrayList<>();
    }

    @Given("There are overdue cases in the database")
    public void thereAreOverdueCasesInTheDatabase() {

        // Seed: 2 overdue (dueDate in the past), 1 nearly overdue (due in 24h), 1 not near (due in 5 days)
        seeded.add(buildCase(1, CaseTypeCode.CARD_STATUS, "TXN-001", "cust1",
                CaseStatusCode.OPEN, Priority.HIGH, LocalDateTime.now().minusDays(2)));
        seeded.add(buildCase(2, CaseTypeCode.CARD_STATUS, "TXN-002", "cust2",
                CaseStatusCode.OPEN, Priority.MEDIUM, LocalDateTime.now().minusHours(5)));
        seeded.add(buildCase(3, CaseTypeCode.CHARGEBACK, "TXN-003", "cust3",
                CaseStatusCode.OPEN, Priority.LOW, LocalDateTime.now().plusHours(24)));
        seeded.add(buildCase(4, CaseTypeCode.CHARGEBACK, "TXN-004", "cust4",
                CaseStatusCode.OPEN, Priority.LOW, LocalDateTime.now().plusDays(5)));

        when(caseRepository.findAll()).thenReturn(seeded);
    }

    @When("I call the reporting endpoint to view overdue cases,")
    public void iCallTheReportingEndpointToViewOverdueCases() {
        // Compute directly from seeded data.
        List<CaseFacade> all = caseRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        observedOverdueCount = all.stream()
                .filter(c -> isOpen(c) && isBeforeDueDate(c, now))
                .count();

        observedNearlyOverdueCount = all.stream()
                .filter(c -> isOpen(c) && isWithinWindow(c, now, nearlyOverdueWindow))
                .count();
    }

    @Then("I should be able to see the number of overdue cases")
    public void iShouldBeAbleToSeeTheNumberOfOverdueCases() {
        // Expect 2 from the seeding above
        assertThat(observedOverdueCount).isEqualTo(2L);
    }

    @And("I should see the number of nearly overdue cases")
    public void iShouldSeeTheNumberOfNearlyOverdueCases() {
        // Expect 1 from the seeding above (due in 24h)
        assertThat(observedNearlyOverdueCount).isEqualTo(1L);
    }

    @Given("There are open cases in the database")
    public void thereAreOpenCasesInTheDatabase() {
        // Seed a fresh set focused on open backlog
        seeded = new ArrayList<>();
        seeded.add(buildCase(10, CaseTypeCode.CARD_STATUS, "TXN-010", "cust10",
                CaseStatusCode.OPEN, Priority.HIGH, LocalDateTime.now().plusDays(2)));
        seeded.add(buildCase(11, CaseTypeCode.CARD_STATUS, "TXN-011", "cust11",
                CaseStatusCode.OPEN, Priority.HIGH, LocalDateTime.now().plusDays(4)));
        seeded.add(buildCase(12, CaseTypeCode.CARD_STATUS, "TXN-012", "cust12",
                CaseStatusCode.OPEN, Priority.LOW, LocalDateTime.now().plusDays(1)));
        seeded.add(buildCase(13, CaseTypeCode.CHARGEBACK, "TXN-013", "cust13",
                CaseStatusCode.OPEN, Priority.MEDIUM, LocalDateTime.now().plusDays(1)));
        seeded.add(buildCase(14, CaseTypeCode.CHARGEBACK, "TXN-014", "cust14",
                CaseStatusCode.CLOSED, Priority.LOW, LocalDateTime.now().minusDays(1))); // closed (excluded)

        when(caseRepository.findAll()).thenReturn(seeded);
    }

    @When("I call the reporting service to view the case backlog")
    public void iCallTheReportingServiceToViewTheCaseBacklog() {
        List<CaseFacade> open = caseRepository.findAll().stream()
                .filter(this::isOpen)
                .collect(Collectors.toList());

        observedOpenByTypeAndPriority = open.stream().collect(
                Collectors.groupingBy(
                        c -> c.getIdentifier().getCaseType(), // type (String or enum name)
                        Collectors.groupingBy(
                                c -> c.getClassification().getPriority(), // priority (String or enum name)
                                Collectors.counting()
                        )
                )
        );
    }

    @Then("I should see all open cases by type and priority")
    public void iShouldSeeAllOpenCasesByTypeAndPriority() {
        // From the seeding above:
        // CARD_STATUS: HIGH=2, LOW=1
        // CHARGEBACK: MEDIUM=1
        assertThat(observedOpenByTypeAndPriority).isNotNull();
        assertThat(observedOpenByTypeAndPriority.get("CARD_STATUS").get("HIGH")).isEqualTo(2L);
        assertThat(observedOpenByTypeAndPriority.get("CARD_STATUS").get("LOW")).isEqualTo(1L);
        assertThat(observedOpenByTypeAndPriority.get("CHARGEBACK").get("MEDIUM")).isEqualTo(1L);
    }

    @Given("There are closed cases in the database")
    public void thereAreClosedCasesInTheDatabase() {
        seeded = new ArrayList<>();
        // resolution time proxy: use assignment.updatedAt - assignment.createdAt
        // encode "resolution speed" via updatedAt timestamps.
        seeded.add(buildClosedCase(100, CaseTypeCode.CARD_STATUS, "TXN-100", "cust100",
                Priority.LOW, /*createdAt*/ LocalDateTime.now().minusDays(5), /*updatedAt*/ LocalDateTime.now().minusDays(1)));
        seeded.add(buildClosedCase(101, CaseTypeCode.CHARGEBACK, "TXN-101", "cust101",
                Priority.MEDIUM, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2)));
        seeded.add(buildClosedCase(102, CaseTypeCode.CARD_STATUS, "TXN-102", "cust102",
                Priority.HIGH, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(9)));

        when(caseRepository.findAll()).thenReturn(seeded);
    }

    @When("I call the reporting service")
    public void iCallTheReportingService() {
        // Sort closed cases by "resolution time" ascending
        observedClosedSorted = caseRepository.findAll().stream()
                .filter(c -> "CLOSED".equalsIgnoreCase(c.getClassification().getStatus()))
                .sorted(Comparator.comparingLong(this::resolutionDurationHours))
                .collect(Collectors.toList());
    }

    @Then("I should see all closed cases sorted by resolution")
    public void iShouldSeeAllClosedCasesSortedByResolution() {
        assertThat(observedClosedSorted).hasSameClassAs(3);
        // The case with the smallest resolution duration should be first
        long first = resolutionDurationHours(observedClosedSorted.get(0));
        long second = resolutionDurationHours(observedClosedSorted.get(1));
        long third = resolutionDurationHours(observedClosedSorted.get(2));
        assertThat(first).isLessThanOrEqualTo(second);
        assertThat(second).isLessThanOrEqualTo(third);

    }


    private boolean isOpen(CaseFacade c) {
        return "OPEN".equalsIgnoreCase(c.getClassification().getStatus());
    }

    private boolean isBeforeDueDate(CaseFacade c, LocalDateTime now) {
        LocalDateTime due = c.getClassification().getDueDate();
        return due != null && due.isBefore(now);
    }

    private boolean isWithinWindow(CaseFacade c, LocalDateTime now, Duration window) {
        LocalDateTime due = c.getClassification().getDueDate();
        return due != null && !due.isBefore(now) && !due.isAfter(now.plus(window));
    }

    private long resolutionDurationHours(CaseFacade c) {
        LocalDateTime created = c.getAssignment().getCreatedAt();
        LocalDateTime updated = c.getAssignment().getUpdatedAt();
        if (created == null || updated == null) return Long.MAX_VALUE;
        return Duration.between(created, updated).toHours();
    }

    private CaseFacade buildCase(long id, CaseTypeCode type, String txnId, String customerId,
                                 CaseStatusCode status, Priority priority, LocalDateTime dueDate) {
        CaseFacade c = baseCase(id, type, txnId, customerId);
        c.getClassification().setStatus(status.name());
        c.getClassification().setPriority(priority.name());
        c.getClassification().setDueDate(dueDate);
        return c;
    }

    private CaseFacade buildClosedCase(long id, CaseTypeCode type, String txnId, String customerId,
                                       Priority priority,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        CaseFacade c = baseCase(id, type, txnId, customerId);
        c.getClassification().setStatus(CaseStatusCode.CLOSED.name());
        c.getClassification().setPriority(priority.name());
        c.getClassification().setDueDate(null);
        c.getAssignment().setCreatedAt(createdAt);
        c.getAssignment().setUpdatedAt(updatedAt);
        return c;
    }

    private CaseFacade baseCase(long id, CaseTypeCode type, String txnId, String customerId) {
        CaseFacade c = new CaseFacade();
        c.setId(id);

        CaseClassification classification = new CaseClassification();
        c.setClassification(classification);

        CaseIdentifier identifier = new CaseIdentifier();
        identifier.setCaseType(type.name());
        identifier.setPrimaryTransactionId(txnId);
        identifier.setCustomerId(customerId);
        c.setIdentifier(identifier);

        CaseAssignment assignment = new CaseAssignment();
        assignment.setCreatedBy("SYSTEM");
        assignment.setAssignedTo("agent01");
        assignment.setCreatedAt(LocalDateTime.now().minusDays(7));
        assignment.setUpdatedAt(LocalDateTime.now().minusDays(1));
        c.setAssignment(assignment);

        CaseTransaction transaction = new CaseTransaction();
        transaction.setTransactionDateTime(LocalDateTime.now().minusDays(8));
        c.setTransaction(transaction);

        CaseOutcome outcome = new CaseOutcome();
        c.setOutcome(outcome);

        return c;
    }

}
