package com.aerotrack.common;

import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.DockerVolume;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.BundlingOutput.ARCHIVED;

public class InfraUtils {
    public static BundlingOptions.Builder getLambdaBuilderOptions() {
        Map<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN")); // Retrieve the token from an environment variable

        return BundlingOptions.builder()
                .image(Runtime.JAVA_17.getBundlingImage())
                .volumes(singletonList(
                        DockerVolume.builder()
                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                .containerPath("/root/.m2/")
                                .build()
                ))
                .environment(environmentVariables)
                .user("root")
                .outputType(ARCHIVED);
    }

    public static String getLambdaRequestHandler(String lambdaName) {
        return switch (lambdaName) {
            case Constants.FLIGHTS_REFRESH_LAMBDA -> "com.aerotrack.lambda.FlightRefreshRequestHandler::handleRequest";
            case Constants.AIRPORTS_REFRESH_LAMBDA -> "com.aerotrack.lambda.AirportsRefreshRequestHandler::handleRequest";
            case Constants.FETCH_AIRPORTS_LAMBDA -> "com.aerotrack.lambda.FetchAirportsRequestHandler::handleRequest";
            case Constants.QUERY_LAMBDA -> "com.aerotrack.lambda.QueryRequestHandler::handleRequest";
            default -> throw new IllegalStateException("Unexpected value: " + lambdaName);
        };
    }

    public static String getResourceName(String resource) {
        String devEnvironment = System.getenv("AEROTRACK_DEV");
        if (devEnvironment == null || devEnvironment.isEmpty()) {
            return resource;
        }
        return devEnvironment + "-" + resource;
    }

    public static boolean isPersonalDeployment() {
        String devEnvironment = System.getenv("AEROTRACK_DEV");
        return devEnvironment != null && !devEnvironment.isEmpty();
    }

    public static List<String> getLambdaPackagingInstructions(String lambda) {
        return Arrays.asList(
                "/bin/sh",
                "-c",
                String.format("cd %s ", lambda) +
                        "&& echo '<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                "xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 " +
                                "http://maven.apache.org/xsd/settings-1.0.0.xsd\">" +
                                    "<servers>" +
                                        "<server>" +
                                            "<id>github</id>" +
                                            String.format("<username>%s</username>", Constants.GITHUB_USERNAME) +
                                            "<password>${GITHUB_TOKEN}</password>" +
                                        "</server>" +
                                    "</servers>" +
                                "</settings>' > ~/.m2/settings.xml " +
                        "&& mvn clean install " +
                        String.format("&& cp /asset-input/%s/target/%s-1.0-SNAPSHOT.jar /asset-output/", lambda, lambda)
        );
    }
}
