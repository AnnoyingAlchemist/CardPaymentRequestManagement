Feature: Delete cases from database
  As a Card operations Manager or Agent,
  I want to be able to delete certain cases from the database

  Scenario Outline: Update case
    Given The case with "<target id>" exists in the database
    When I make a DELETE request with the updated case to the cases "<target id>" endpoint
    Then the application should delete the case in the database
    Examples:
      |target id|
      |  22  |
      |  30  |

  Scenario Outline: Case does not exist
    Given A case with the "<target id>" does not exist in the database
    When I make a DELETE request with the updated case to the cases "<target id>" endpoint
    Then the application should return status code 404
    Examples:
      |target id|
      |  56345  |
      |  324    |