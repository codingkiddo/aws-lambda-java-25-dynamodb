package com.codingkiddo.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public final class ApiResponse {

    private static final Map<String, String> JSON_HEADERS = Map.of(
            "Content-Type", "application/json"
    );

    private ApiResponse() {
    }

    public static APIGatewayProxyResponseEvent json(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(JSON_HEADERS)
                .withBody(body);
    }

    public static APIGatewayProxyResponseEvent noContent() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(204)
                .withHeaders(JSON_HEADERS)
                .withBody("");
    }
}
