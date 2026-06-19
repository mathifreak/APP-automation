package com.pice.api;

import io.restassured.response.Response;
import lombok.Getter;

/**
 * Wrapper for REST Assured Response.
 * Simplifies assertions and logging for API test responses.
 */
@Getter
public class ApiResponse {

    private final int statusCode;
    private final String bodyAsString;
    private final Response originalResponse;

    public ApiResponse(Response response) {
        this.originalResponse = response;
        this.statusCode = response.getStatusCode();
        this.bodyAsString = response.getBody().asString();
    }

    /**
     * Deserialize the response body to a generic POJO class.
     *
     * @param responseClass the class type to deserialize into
     * @param <T>           the type parameter
     * @return deserialized object
     */
    public <T> T as(Class<T> responseClass) {
        return originalResponse.as(responseClass);
    }
}
