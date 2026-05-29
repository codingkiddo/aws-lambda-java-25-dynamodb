package com.codingkiddo.lambda;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class CustomerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		context.getLogger().log("Received request: " + request.getHttpMethod() + " " + request.getPath());
		return new APIGatewayProxyResponseEvent().withStatusCode(200)
				.withHeaders(Map.of("Content-Type", "application/json")).withBody("""
						  {
						  "message": "Hello from AWS Lambda Java 25"
						}
						""");
	}

}
