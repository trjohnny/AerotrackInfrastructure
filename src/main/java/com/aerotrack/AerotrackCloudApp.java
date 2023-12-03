package com.aerotrack;

import com.aerotrack.infrastructure.InfraStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class AerotrackCloudApp {
    public static String PIPILINE_STACK = "PipelineStack";
    public static String INFRA_STACK = "InfraStack";

    public static void main(final String[] args) {
        App app = new App();

        String devEnvironment = System.getenv("AEROTRACK_DEV");
        if (devEnvironment != null && !devEnvironment.isEmpty()) {
            String stackName = devEnvironment + "-" + INFRA_STACK;
            // To deploy the personal stack set the environmental variable AEROTRACK_DEV to your name
            new InfraStack(app, stackName, StackProps.builder()
                    .env(Environment.builder()
                            .account("073873382417")
                            .region("eu-west-1")
                            .build())
                    .build());
        }

        new PipelineStack(app, PIPILINE_STACK, StackProps.builder()
                .env(Environment.builder()
                        .account("789827607242")
                        .region("eu-west-1")
                        .build())
                .build());

        app.synth();
    }
}
