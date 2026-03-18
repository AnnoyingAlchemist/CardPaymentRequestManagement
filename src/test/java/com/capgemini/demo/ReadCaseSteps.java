package com.capgemini.demo;

import com.capgemini.demo.repository.CaseRepository;
import com.capgemini.demo.service.CaseService;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ReadCaseSteps {
    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private CaseService caseService;




    @Given("There exist cases in the database")
    public void thereExistCasesInTheDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I send a GET request to cases endpoint to read a case")
    public void iSendAGETRequestToCasesEndpointToReadACase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should return a list of all cases")
    public void theApplicationShouldReturnAListOfAllCases() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("There do not exist any cases in the database")
    public void thereDoNotExistAnyCasesInTheDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make a GET request to cases to read cases from an empty database")
    public void iMakeAGETRequestToCasesToReadCasesFromAnEmptyDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should return status code {int}")
    public void theApplicationShouldReturnStatusCode(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("A case with the {string} exists in the database")
    public void aCaseWithTheExistsInTheDatabase(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I send a GET request to cases endpoint to read a certain case")
    public void iSendAGETRequestToCasesEndpointToReadACertainCase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should return the case that matches the {string}")
    public void theApplicationShouldReturnTheCaseThatMatchesThe(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("A case with the {string} does not exist in the database")
    public void aCaseWithTheDoesNotExistInTheDatabase(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make a GET request to cases endpoint for a non-existent case")
    public void iMakeAGETRequestToCasesEndpointForANonExistentCase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
