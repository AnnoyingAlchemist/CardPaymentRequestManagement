Feature: Update cases from database
  As a Card operations Manager or Agent,
  I want to be able to change details about the cases from the database
  To update case details when they change

  Scenario: Update case
    Given The case with target id exists in the database
    And The case status transition is allowed
    When I make a PUT request to the cases endpoint
    Then the application should update the case in the database

  Scenario: Illegal case status transition
    Given Case with target id exists in the database
    But The case status transition is not allowed
    When I make a PUT request to the cases target id endpoint
    Then the application return an error

  Scenario Outline: Case does not exist
    Given A case with the <target id> does not exist to be updated
    When I make a PUT request with the updated case to the cases <target id> endpoint
    Then the update attempt should return status code 404
    Examples:
      |target id|
      |  56345  |
      |  324    |