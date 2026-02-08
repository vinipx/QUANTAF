package io.github.vinipx.quantaf.protocol.rest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Manages OAuth2 token lifecycle: acquires, caches, and auto-refreshes
 * access tokens before expiry.
 */
public class OAuth2TokenManager {

    private static final Logger log = LoggerFactory.getLogger(OAuth2TokenManager.class);
    private static final int REFRESH_BUFFER_SECONDS = 60;

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    private String accessToken;
    private Instant tokenExpiry;

    public OAuth2TokenManager(String tokenUrl, String clientId, String clientSecret) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Returns a valid access token, refreshing if necessary.
     */
    public synchronized String getAccessToken() {
        if (accessToken == null || isExpired()) {
            refreshToken();
        }
        return accessToken;
    }

    /**
     * Forces a token refresh.
     */
    public synchronized void refreshToken() {
        log.debug("Requesting OAuth2 token from {}", tokenUrl);
        try {
            Response response = RestAssured.given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("grant_type", "client_credentials")
                    .formParam("client_id", clientId)
                    .formParam("client_secret", clientSecret)
                    .post(tokenUrl);

            if (response.statusCode() == 200) {
                this.accessToken = response.jsonPath().getString("access_token");
                int expiresIn = response.jsonPath().getInt("expires_in");
                this.tokenExpiry = Instant.now().plusSeconds(expiresIn - REFRESH_BUFFER_SECONDS);
                log.info("OAuth2 token acquired (expires in {}s)", expiresIn);
            } else {
                log.error("OAuth2 token request failed: {} {}", response.statusCode(), response.body().asString());
                throw new RuntimeException("OAuth2 token request failed with status: " + response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to acquire OAuth2 token", e);
            throw new RuntimeException("Failed to acquire OAuth2 token from: " + tokenUrl, e);
        }
    }

    /**
     * Checks if the current token is expired or about to expire.
     */
    private boolean isExpired() {
        return tokenExpiry == null || Instant.now().isAfter(tokenExpiry);
    }

    /**
     * Invalidates the current token, forcing a refresh on next access.
     */
    public synchronized void invalidate() {
        this.accessToken = null;
        this.tokenExpiry = null;
        log.info("OAuth2 token invalidated");
    }
}
