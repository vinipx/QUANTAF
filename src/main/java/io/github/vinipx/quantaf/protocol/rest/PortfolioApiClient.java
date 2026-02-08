package io.github.vinipx.quantaf.protocol.rest;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Typed REST client for portfolio management operations.
 * Delegates to {@link RestClientWrapper} for HTTP interactions.
 */
public class PortfolioApiClient {

    private static final Logger log = LoggerFactory.getLogger(PortfolioApiClient.class);

    private final RestClientWrapper restClient;

    public PortfolioApiClient(RestClientWrapper restClient) {
        this.restClient = restClient;
    }

    /**
     * Gets the portfolio position for a given account.
     */
    public PortfolioPosition getPortfolioPosition(String accountId) {
        Response response = restClient.get("/portfolios/" + accountId + "/positions");
        if (response.statusCode() == 200) {
            return response.as(PortfolioPosition.class);
        }
        log.error("Failed to get portfolio position for {}: {} {}",
                accountId, response.statusCode(), response.body().asString());
        throw new RuntimeException("Portfolio position request failed: " + response.statusCode());
    }

    /**
     * Gets the portfolio position for a specific symbol within an account.
     */
    public PortfolioPosition getPositionForSymbol(String accountId, String symbol) {
        Response response = restClient.get("/portfolios/" + accountId + "/positions",
                Map.of("symbol", symbol));
        if (response.statusCode() == 200) {
            return response.as(PortfolioPosition.class);
        }
        log.error("Failed to get position for {}/{}: {}", accountId, symbol, response.statusCode());
        throw new RuntimeException("Position request failed: " + response.statusCode());
    }

    /**
     * Checks if a trade was settled for the given order.
     */
    public boolean isTradeSettled(String accountId, String orderId) {
        Response response = restClient.get("/portfolios/" + accountId + "/trades/" + orderId);
        return response.statusCode() == 200 &&
                "SETTLED".equalsIgnoreCase(response.jsonPath().getString("status"));
    }

    /**
     * Portfolio position POJO.
     */
    public static class PortfolioPosition {
        private String accountId;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal averagePrice;
        private BigDecimal marketValue;
        private String currency;
        private String lastUpdated;

        public PortfolioPosition() {
        }

        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
        public BigDecimal getMarketValue() { return marketValue; }
        public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

        @Override
        public String toString() {
            return String.format("PortfolioPosition{account='%s', symbol='%s', qty=%s, avgPx=%s, mktVal=%s}",
                    accountId, symbol, quantity, averagePrice, marketValue);
        }
    }
}
