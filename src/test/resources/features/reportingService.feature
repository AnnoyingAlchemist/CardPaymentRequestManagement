Feature: Reporting Service
  As a card operations manager,
  I want to be able to view an overview of all cases in the database,
  grouped by certain attributes, and be able to see overdue cases
  as well as new cases.

  Scenario: View Overdue and nearly overdue cases in database
    Given There are overdue cases in the database
    When I call the reporting endpoint to view overdue cases,
    Then I should be able to see the number of overdue cases
    And I should see the number of nearly overdue cases

  Scenario: View case backlog
    Given There are open cases in the database
    When I call the reporting service to view the case backlog
    Then I should see all open cases by type and priority

  Scenario: View closed cases
    Given There are closed cases in the database
    When I call the reporting service
    Then I should see all closed cases sorted by resolution