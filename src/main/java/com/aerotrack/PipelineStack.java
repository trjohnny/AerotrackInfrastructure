package com.aerotrack;

import com.aerotrack.common.Constants;
import com.aerotrack.infrastructure.AppStage;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.pipelines.*;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariable;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;
import java.util.Map;


public class PipelineStack extends Stack {

    private String createMavenSettings() {
        return "mkdir -p ~/.m2 &&" +
                "echo '<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" " +
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
                        "</settings>' > ~/.m2/settings.xml";
    }

    public PipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        CodeBuildOptions buildOptions = CodeBuildOptions.builder()
                .buildEnvironment(BuildEnvironment.builder()
                        .environmentVariables(Map.of(
                                "GITHUB_TOKEN", BuildEnvironmentVariable.builder()
                                        .value(Secret.fromSecretNameV2(this, "GitHubToken", "github-token")
                                                .getSecretValue()
                                                .unsafeUnwrap())
                                        .build()))
                        .build())
                .build();

        CodePipeline pipeline = CodePipeline.Builder.create(this, "AerotrackPipeline")
                .pipelineName("AerotrackPipeline")
                .codeBuildDefaults(buildOptions)
                .publishAssetsInParallel(false)
                .crossAccountKeys(true)
                .synth(ShellStep.Builder.create("Synth")
                        .input(CodePipelineSource.gitHub("trjohnny/AerotrackInfrastructure", "mainline"))
                        .commands(Arrays.asList(
                                createMavenSettings(),
                                "npm install -g aws-cdk",
                                "cdk synth"
                        ))
                        .build())
                .build();

        /*StageDeployment alphaStage = pipeline.addStage(new AppStage(this, "Alpha", StageProps.builder()
                .env(Environment.builder()
                        .account("073873382417")
                        .region("eu-west-1")
                        .build())
                .build()));

        // Integration testing step
        ShellStep integrationTestStep = ShellStep.Builder.create("IntegrationTests")
                .commands(Arrays.asList(
                        createMavenSettings(),
                        "mvn verify"
                ))
                .build();


        // Add integration tests as post-deployment steps for Alpha stage
        alphaStage.addPost(integrationTestStep);*/

        StageDeployment prodStage = pipeline.addStage(new AppStage(this, "Prod", StageProps.builder()
                .env(Environment.builder()
                        .account("715311622639")
                        .region("eu-west-1")
                        .build())
                .build()));

        //prodStage.addPre(new ManualApprovalStep("approval"));
    }
}
