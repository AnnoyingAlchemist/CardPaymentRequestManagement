import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CreateCaseSteps {
    @Given("I have a case with attributes that match the database design")
    public void iHaveACaseWithAttributesThatMatchTheDatabaseDesign() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make a POST request with my case to the case controller")
    public void iMakeAPOSTRequestWithMyCaseToTheCaseController() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should create the case in the database and return status code {int}")
    public void theApplicationShouldCreateTheCaseInTheDatabaseAndReturnStatusCode(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("I have a case with attributes that do not match the database design or constraints")
    public void iHaveACaseWithAttributesThatDoNotMatchTheDatabaseDesignOrConstraints() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make POST request with my case to the case controller")
    public void iMakePOSTRequestWithMyCaseToTheCaseController() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should not create the case in the database and return an error")
    public void theApplicationShouldNotCreateTheCaseInTheDatabaseAndReturnAnError() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("I have a case in the database with a transaction id and type")
    public void iHaveACaseInTheDatabaseWithATransactionIdAndType() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("I make a request to the case service to create another OPEN case of the same type")
    public void iMakeARequestToTheCaseServiceToCreateAnotherOPENCaseOfTheSameType() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should return an error and not create the case")
    public void theApplicationShouldReturnAnErrorAndNotCreateTheCase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
