import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DeleteCaseSteps {
    @When("I make a DELETE request with the updated case to the cases {string} endpoint")
    public void iMakeADELETERequestWithTheUpdatedCaseToTheCasesEndpoint(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("the application should delete the case in the database")
    public void theApplicationShouldDeleteTheCaseInTheDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
