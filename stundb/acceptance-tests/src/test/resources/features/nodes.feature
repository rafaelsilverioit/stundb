Feature: Nodes are able to communicate between themselves

  Background:
    Given We clear the cache
    And The cache is empty
    Then The node 66508730 is elected as the leader node

  @timed
  Scenario: Set and remove data - actions are replicated between nodes
    Given An entry for key "realngnx" and value "0100" is recorded
    And The cache is not empty
    And We are able to retrieve the current value for the key "realngnx" from node 66508730
    And We are able to retrieve the current value for the key "realngnx" from node 3574777063
    When We are able to remove an existing record for the key "realngnx"
    Then No records are found for the key "realngnx" in any nodes
    And The cache is empty

  @cluster
  Scenario: Node registration
    Given Another node running on port 8888 joins the cluster
    And We are able to talk to the leader node and verify that node was added to the cluster

  @timed @cluster
  Scenario: Cluster leader election
    When We are able to trigger an election
    And We are able to talk to the leader node
    And The node 3574777063 becomes the leader node
    Then All nodes agree that node 3574777063 is the leader node
