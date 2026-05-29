# AWS Lambda Java 25 DynamoDB

A production-style serverless CRUD API using:

- Java 25
- AWS Lambda
- API Gateway
- DynamoDB
- AWS SDK for Java v2
- AWS SAM
- LocalStack / Floci support planned

## Status

Initial Maven Java 25 Lambda project setup.

## Build

```bash
mvn clean package
```

## Lambda Handler

```text
com.codingkiddo.lambda.CustomerHandler::handleRequest
```

## Roadmap

- Add DynamoDB Enhanced Client
- Add Customer CRUD APIs
- Add AWS SAM template
- Add LocalStack integration tests
- Add optional Floci integration tests
- Add GitHub Actions CI

