package cloud.phusion.integration;

import cloud.phusion.DataObject;

/**
 * Endpoint step: bound to an endpoint.
 */
public class StepEndpoint extends Step {

    private String applicationId;
    private String endpointId;
    private Direction direction;
    private String connectionId;
    private DataObject config;

    public StepEndpoint(String id, String[] fromSteps, String applicationId, String endpointId, Direction direction, String connectionId, DataObject config) {
        super(id, fromSteps);

        this.applicationId = applicationId;
        this.endpointId = endpointId;
        this.direction = direction;
        this.connectionId = connectionId;
        this.config = config;
    }

    public void setConnectionId(String id) {
        connectionId = id;
    }

    public String getApplication() {
        return applicationId;
    }

    public String getEndpoint() {
        return endpointId;
    }

    /**
     * Direction of the endpoint.
     */
    public Direction getDirection() {
        return direction;
    }

    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Config of the endpoint.
     */
    public DataObject getConfig() {
        return config;
    }

}
