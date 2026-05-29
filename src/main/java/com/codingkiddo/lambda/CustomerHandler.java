package com.codingkiddo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class CustomerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CustomerRepository repository;

    public CustomerHandler() {
        this(new CustomerRepository(resolveTableName()));
    }

    CustomerHandler(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String httpMethod = request.getHttpMethod();

            context.getLogger().log("Received request: " + httpMethod + " " + request.getPath());

            return switch (httpMethod) {
                case "POST" -> createCustomer(request);
                case "GET" -> getCustomer(request);
                case "PUT" -> updateCustomer(request);
                case "DELETE" -> deleteCustomer(request);
                default -> ApiResponse.json(405, """
                        {"message":"Method not allowed"}
                        """);
            };
        } catch (IllegalArgumentException ex) {
            context.getLogger().log("Bad request: " + ex.getMessage());
            return ApiResponse.json(400, """
                    {"message":"Bad request"}
                    """);
        } catch (Exception ex) {
            context.getLogger().log("Unexpected error: " + ex.getMessage());
            return ApiResponse.json(500, """
                    {"message":"Internal server error"}
                    """);
        }
    }

    private APIGatewayProxyResponseEvent createCustomer(APIGatewayProxyRequestEvent request) throws Exception {
        CustomerRequest customerRequest = parseRequestBody(request);

        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());
        customer.setName(customerRequest.name());
        customer.setEmail(customerRequest.email());
        customer.setCreatedAt(Instant.now().toString());

        repository.save(customer);

        return ApiResponse.json(201, OBJECT_MAPPER.writeValueAsString(customer));
    }

    private APIGatewayProxyResponseEvent getCustomer(APIGatewayProxyRequestEvent request) throws Exception {
        String customerId = pathParam(request, "customerId");

        return repository.findById(customerId)
                .map(customer -> {
                    try {
                        return ApiResponse.json(200, OBJECT_MAPPER.writeValueAsString(customer));
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to serialize customer", ex);
                    }
                })
                .orElseGet(() -> ApiResponse.json(404, """
                        {"message":"Customer not found"}
                        """));
    }

    private APIGatewayProxyResponseEvent updateCustomer(APIGatewayProxyRequestEvent request) throws Exception {
        String customerId = pathParam(request, "customerId");
        CustomerRequest customerRequest = parseRequestBody(request);

        Customer existing = repository.findById(customerId)
                .orElse(null);

        if (existing == null) {
            return ApiResponse.json(404, """
                    {"message":"Customer not found"}
                    """);
        }

        existing.setName(customerRequest.name());
        existing.setEmail(customerRequest.email());

        Customer updated = repository.update(existing);

        return ApiResponse.json(200, OBJECT_MAPPER.writeValueAsString(updated));
    }

    private APIGatewayProxyResponseEvent deleteCustomer(APIGatewayProxyRequestEvent request) {
        String customerId = pathParam(request, "customerId");

        repository.deleteById(customerId);

        return ApiResponse.noContent();
    }

    private CustomerRequest parseRequestBody(APIGatewayProxyRequestEvent request) throws Exception {
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new IllegalArgumentException("Request body is required");
        }

        CustomerRequest customerRequest = OBJECT_MAPPER.readValue(request.getBody(), CustomerRequest.class);

        if (customerRequest.name() == null || customerRequest.name().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }

        if (customerRequest.email() == null || customerRequest.email().isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        return customerRequest;
    }

    private String pathParam(APIGatewayProxyRequestEvent request, String name) {
        Map<String, String> pathParameters = request.getPathParameters();

        if (pathParameters == null || !pathParameters.containsKey(name)) {
            throw new IllegalArgumentException("Missing path parameter: " + name);
        }

        return pathParameters.get(name);
    }

    private static String resolveTableName() {
        String tableName = System.getenv("TABLE_NAME");

        if (tableName == null || tableName.isBlank()) {
            throw new IllegalStateException("TABLE_NAME environment variable is required");
        }

        return tableName;
    }
}
