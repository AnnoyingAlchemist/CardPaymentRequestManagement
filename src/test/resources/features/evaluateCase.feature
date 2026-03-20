Feature: automatic priority level evaluation of cases
  As a Card operations user,
  I want cases to automatically be evaluated when I create them,
  being given a priority level and recommended next action
  based on attributes of the case and modular rules.

  Scenario: high value case created
    Given a case is created in the database with a high monetary value
    When the rule engine evaluates the case
    Then the case should be assigned a HIGH priority before being saved to the database

  Scenario: low value case created
    Given a case is created in the database with a low monetary value
    When the rule engine evaluates the case
    Then the case should be assigned a LOW priority before being saved to the database

  Scenario: rule evaluation fails
    Given a case is created in the database with edge case attributes
    When the rule engine evaluates the case details
    Then the case should be assigned an UNKNOWN priority before being saved to the database