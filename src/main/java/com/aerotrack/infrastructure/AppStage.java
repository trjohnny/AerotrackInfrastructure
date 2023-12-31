package com.aerotrack.infrastructure;

import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;


public class AppStage extends Stage {
    public AppStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AppStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        new InfraStack(this, "InfraStack");
    }

}