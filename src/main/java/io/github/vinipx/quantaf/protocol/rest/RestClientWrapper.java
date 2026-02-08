package io.github.vinipx.quantaf.protocol.rest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * RestAssured wrapper for API interactions with built-in OAuth2 support
 * and configurable base URL per environment.
 */
public class RestClientWrapper {

    private static final Logger log = LoggerFactory.getLogger(RestClientWrapper.class);

    private final String baseUrl;
    private final OAuth2TokenManager tokenManager;

    public RestClientWrapper(String baseUrl) {
        this(baseUrl, null);
    }

    public RestClientWrapper(String baseUrl, OAuth2TokenManager tokenManager) {
        this.baseUrl = baseUrl;
        this.tokenManager = tokenManager;
        log.info("REST client configured [baseUrl={}]", baseUrl);
    }

    /**
     * Performs a GET request.
     */
    public Response get(String path) {
        return get(path, Map.of());
    }

    /**
     * Performs a GET request with query parameters.
     */
    public Response get(String path, Map<String, String> queryParams) {
        RequestSpecification spec = buildSpec();
        if (queryParams != null && !queryParams.isEmpty()) {
            spec.queryParams(queryParams);
        }
        Response response = spec.get(baseUrl + path);
        log.debug("GET {} -> {} ({}ms)", path, response.statusCode(), response.time());
        return response;
    }

    /**
     * Performs a POST request with a JSON body.
     */
    public Response post(String path, Object body) {
        Response response = buildSpec()
                .contentType("application/json")
                .body(body)
                .post(baseUrl + path);
        log.debug("POST {} -> {} ({}ms)", path, response.statusCode(), response.time());
        return response;
    }

    /**
     * Performs a PUT request with a JSON body.
     */
    public Response put(String path, Object body) {
        Response response = buildSpec()
                .contentType("application/json")
                .body(body)
                .put(baseUrl + path);
        log.debug("PUT {} -> {} ({}ms)", path, response.statusCode(), response.time());
        return response;
    }

    /**
     * Performs a DELETE request.
     */
    public Response delete(String path) {
        Response response = buildSpec().delete(baseUrl + path);
        log.debug("DELETE {} -> {} ({}ms)", path, response.statusCode(), response.time());
        return response;
    }

    /**
     * Builds a base request specification with auth headers if token manager is available.
     */
    private RequestSpecification buildSpec() {
        RequestSpecification spec = RestAssured.given().log().ifValidationFails();
        if (tokenManager != null) {
            String token = tokenManager.getAccessToken();
            if (token != null && !token.isEmpty()) {
                spec.header("Authorization", "Bearer " + token);
            }
        }
        return spec;
    }
}
