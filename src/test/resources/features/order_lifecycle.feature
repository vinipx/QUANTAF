Feature: Order Lifecycle
  As a trading platform QA engineer
  I want to validate the full order lifecycle
  So that I can ensure correct trade processing across FIX, MQ, and API

  @smoke @fix
  Scenario: Successful limit order fill
    Given a FIX session is available for version "FIX44"
    When I submit a "BUY" "LIMIT" order for "AAPL" at price 150.00 with quantity 100
    Then the order should be filled at price 150.00
    And the trade should reconcile across all sources

  @smoke @fix
  Scenario: Fat-finger order rejection
    Given a FIX session is available for version "FIX44"
    And the exchange is configured to reject orders for "TSLA" above price 5000.00
    When I submit a "BUY" "LIMIT" order for "TSLA" at price 9999.00 with quantity 100
    Then the order should be rejected with reason "Fat-finger price check"

  @regression @ai
  Scenario: AI-generated order scenario
    Given the AI scenario agent is available
    When I generate an order from intent "Buy 500 shares of MSFT Market On Close"
    Then the generated order should have side "BUY"
    And the generated order should have symbol "MSFT"
    And the generated order should have time in force "AT_CLOSE"

  @regression @reconciliation
  Scenario: Cross-source trade reconciliation
    Given matching trade records exist for order "RECON-001"
      | source | symbol | price  | quantity | settlementDate |
      | FIX    | GOOG   | 175.50 | 200      | 2026-02-10     |
      | MQ     | GOOG   | 175.50 | 200      | 2026-02-10     |
      | API    | GOOG   | 175.50 | 200      | 2026-02-10     |
    When I reconcile the trade "RECON-001"
    Then the reconciliation should pass
    And all fields should match across sources
