package com.aerotrack.infrastructure;

import com.aerotrack.infrastructure.constructs.ApiConstruct;
import com.aerotrack.infrastructure.constructs.DataConstruct;
import com.aerotrack.infrastructure.constructs.RefreshConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import static com.aerotrack.common.Constants.API_CONSTRUCT;
import static com.aerotrack.common.Constants.DATA_CONSTRUCT;
import static com.aerotrack.common.Constants.REFRESH_CONSTRUCT;
public class InfraStack extends Stack {
    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);


        DataConstruct data = new DataConstruct(this, DATA_CONSTRUCT);

        new ApiConstruct(this, API_CONSTRUCT, data.getAirportsBucket(), data.getFlightsTable());

        new RefreshConstruct(this, REFRESH_CONSTRUCT, data.getAirportsBucket(), data.getFlightsTable());
    }

}