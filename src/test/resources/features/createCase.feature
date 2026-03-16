Feature: Create case
  As a Card operations Manager or Agent,
  I want to be able to send a list of case details to the case service,
  To create a new case in my database.

  Scenario: Successful case creation
    Given I have a case with attributes that match the database design
    When I make a POST request with my case to the case controller
    Then the application should create the case in the database and return status code 200

  Scenario: failed creation creation
    Given I have a case with attributes that do not match the database design or constraints
    When I make POST request with my case to the case controller
    Then the application should not create the case in the database and return an error

  Scenario: Open case already exists with same transaction and type
    Given I have a case in the database with a transaction id and type
    When I make a request to the case service to create another OPEN case of the same type
    Then the application should return an error and not create the case