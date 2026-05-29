package com.codingkiddo.lambda;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.util.Optional;

public class CustomerRepository {

    private final DynamoDbTable<Customer> table;

    public CustomerRepository(String tableName) {
        this(createDynamoDbClient(), tableName);
    }

    CustomerRepository(DynamoDbClient dynamoDbClient, String tableName) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Customer.class));
    }

    public void save(Customer customer) {
        table.putItem(customer);
    }

    public Optional<Customer> findById(String customerId) {
        Customer customer = table.getItem(Key.builder()
                .partitionValue(customerId)
                .build());

        return Optional.ofNullable(customer);
    }

    public Customer update(Customer customer) {
        return table.updateItem(customer);
    }

    public void deleteById(String customerId) {
        table.deleteItem(Key.builder()
                .partitionValue(customerId)
                .build());
    }

    private static DynamoDbClient createDynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(resolveRegion());

        String endpoint = System.getenv("DYNAMODB_ENDPOINT");

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        try {
            return builder.build();
        } catch (SdkClientException ex) {
            throw new IllegalStateException("Failed to create DynamoDB client", ex);
        }
    }

    private static Region resolveRegion() {
        return Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1"));
    }
}
