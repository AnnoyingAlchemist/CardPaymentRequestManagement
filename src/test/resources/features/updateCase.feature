Feature: Update cases from database
  As a Card operations Manager or Agent,
  I want to be able to change details about the cases from the database
  To update case details when they change

  Scenario Outline: Update case
    Given The case with "<target id>" exists in the database
    And The case status transition is allowed
    When I make a PUT request to the cases "<target id>" endpoint
    Then the application should update the case in the database and return the updated case
    Examples:
      |target id|
      |  22  |
      |  30  |

  Scenario Outline: Illegal case status transition
    Given The case with "<target id>" exists in the database
    But The case status transition is not allowed
    When I make a PUT request to the cases "<target id>" endpoint
    Then the application return an error
    Examples:
      |target id|
      |  22  |
      |  30  |

  Scenario Outline: Case does not exist
    Given A case with the "<target id>" does not exist in the database
    When I make a PUT request with the updated case to the cases "<target id>" endpoint
    Then the application should return status code 404
    Examples:
      |target id|
      |  56345  |
      |  324    |