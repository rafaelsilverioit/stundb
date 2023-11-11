Feature: Cache

  Scenario: Set and retrieve data
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty
    Then We are able to retrieve the current value for the key "realngnx"

  Scenario: Set and remove data
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty
    And We are able to retrieve the current value for the key "realngnx"
    Then We are able to remove an existing record for the key "realngnx"
    And No records are found for the key "realngnx"

  Scenario: Set and update data
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty
    Then An entry for key "realngnx" and value "1010" is recorded
    And We are able to retrieve the current value for the key "realngnx"

  Scenario: Check capacity
    Given We are able to retrieve the cache capacity

  Scenario: Clear the cache
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty
    And We are able to retrieve the current value for the key "realngnx"
    When We clear the cache
    Then The cache is empty
    And No records are found for the key "realngnx"

  # TODO: implement multi-node steps
  @wip
  Scenario: Set and remove data - actions are replicated between nodes
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty
    And We are able to retrieve the current value for the key "realngnx" from the same node
    And We are able to retrieve the current value for the key "realngnx" from node with unique id "123456"
    And We are able to retrieve the current value for the key "realngnx" from node with unique id "654321"
    Then We are able to remove an existing record for the key "realngnx"
    And No records are found for the key "realngnx" in any nodes