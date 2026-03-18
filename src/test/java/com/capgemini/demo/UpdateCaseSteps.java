package com.capgemini.demo;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.*;

public class UpdateCaseSteps {
    @Given("The case with {string} exists in the database")
    public void theCaseWithExistsInTheDatabase(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("The case status transition is allowed")
    public void theCaseStatusTransitionIsAllowed() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make a PUT request to the cases {string} endpoint")
    public void iMakeAPUTRequestToTheCasesEndpoint(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should update the case in the database and return the updated case")
    public void theApplicationShouldUpdateTheCaseInTheDatabaseAndReturnTheUpdatedCase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @But("The case status transition is not allowed")
    public void theCaseStatusTransitionIsNotAllowed() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application return an error")
    public void theApplicationReturnAnError() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make a PUT request with the updated case to the cases {string} endpoint")
    public void iMakeAPUTRequestWithTheUpdatedCaseToTheCasesEndpoint(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
