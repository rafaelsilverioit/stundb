Feature: Cache operations

  Background:
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty

  Scenario: Retrieve and remove data
    Given We are able to check that the current value for key "realngnx" is "0100"
    Then We are able to remove an existing record for the key "realngnx"
    And No records are found for the key "realngnx"

  Scenario: Update data
    Given An entry for key "realngnx" and value "1010" is recorded
    And We are able to check that the current value for key "realngnx" is "1010"

  Scenario: Clear the cache
    Given We are able to check that the current value for key "realngnx" is "0100"
    When We clear the cache
    Then The cache is empty
    And No records are found for the key "realngnx"
