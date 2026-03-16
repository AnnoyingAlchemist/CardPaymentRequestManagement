Feature: Read cases from database
  As a Card operations Manager or Agent,
  I want to be able to access the cases from the database
  To read about case details

  Scenario: Reading all cases
    Given There exist cases in the database
    When I send a GET request to cases endpoint to read a case
    Then the application should return a list of all cases

  Scenario: No cases in database
    Given There do not exist any cases in the database
    When I make a GET request to cases to read cases from an empty database
    Then the application should return status code 404

  Scenario Outline: Reading a specific case
    Given A case with the "<target id>" exists in the database
    When I send a GET request to cases endpoint to read a certain case
    Then the application should return the case that matches the "<target id>"
    Examples:
      |target id|
      |22       |
      |23       |

  Scenario Outline: Case does not exist
    Given A case with the "<target id>" does not exist in the database
    When I make a GET request to cases endpoint for a non-existent case
    Then the application should return status code 404
    Examples:
      |target id|
      |  56345  |
      |  324    |