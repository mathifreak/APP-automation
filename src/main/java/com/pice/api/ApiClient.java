package com.pice.api;

import com.pice.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Assured wrapper to handle API requests and integrate with ExtentReports.
 */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);

    private final RequestSpecification requestSpec;
    private final Map<String, String> headers;
    private final Map<String, Object> queryParams;

    public ApiClient() {
        this(ConfigManager.get("api.base.url", ""));
    }

    public ApiClient(String baseUrl) {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();

        // Default headers
        this.headers.put("Accept", "application/json");

        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .build();
    }

    public ApiClient addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public ApiClient addHeaders(Map<String, String> multipleHeaders) {
        headers.putAll(multipleHeaders);
        return this;
    }

    public ApiClient addQueryParam(String key, Object value) {
        queryParams.put(key, value);
        return this;
    }

    public ApiClient addQueryParams(Map<String, Object> multipleQueryParams) {
        queryParams.putAll(multipleQueryParams);
        return this;
    }

    private RequestSpecification buildRequest() {
        return RestAssured.given()
                .spec(requestSpec)
                .headers(headers)
                .queryParams(queryParams)
                .log().ifValidationFails();
    }

    private void logRequest(String method, String endpoint, Object body) {
        String message = String.format("➡️ %s Request to: %s", method, endpoint);
        log.info(message);
        
        if (body != null) {
            log.info("Request Body: \n{}", body.toString());
        }
    }

    private void logResponse(Response response) {
        String message = String.format("⬅️ Response Status: %d", response.getStatusCode());
        log.info(message);
        
        String responseBody = response.getBody().asPrettyString();
        if (responseBody != null && !responseBody.isEmpty()) {
            log.info("Response Body: \n{}", responseBody);
        }
    }

    public ApiResponse get(String endpoint) {
        logRequest("GET", endpoint, null);
        Response response = buildRequest().get(endpoint);
        logResponse(response);
        return new ApiResponse(response);
    }

    public ApiResponse post(String endpoint, Object body) {
        logRequest("POST", endpoint, body);
        Response response = buildRequest().body(body).post(endpoint);
        logResponse(response);
        return new ApiResponse(response);
    }

    public ApiResponse put(String endpoint, Object body) {
        logRequest("PUT", endpoint, body);
        Response response = buildRequest().body(body).put(endpoint);
        logResponse(response);
        return new ApiResponse(response);
    }

    public ApiResponse delete(String endpoint) {
        logRequest("DELETE", endpoint, null);
        Response response = buildRequest().delete(endpoint);
        logResponse(response);
        return new ApiResponse(response);
    }
}
