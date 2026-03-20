Feature: Delete cases from database
  As a Card operations Manager or Agent,
  I want to be able to delete certain cases from the database

  Scenario: Delete case that exists in the database
    Given A case with target id exists in the database to be deleted
    When I make a DELETE request with the case id to the cases target id endpoint
    Then the application should delete the case with target id in the database

  Scenario Outline: Try to delete case that does not exist
    Given A case with the <target id> does not exist
    When I make a DELETE request with the updated case to the cases <target id> endpoint
    Then the application should fail to delete and return status code 404
    Examples:
      |target id|
      |  56345  |
      |  32412  |