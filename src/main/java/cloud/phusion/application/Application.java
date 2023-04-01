package cloud.phusion.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.ExecStatus;

/**
 * Application.
 *
 * It must be thread-safe.
 */
public interface Application {

    /**
     * ID (also codename) of the application.
     */
    String getId();
    void setId(String id);

    /**
     * Initialize, start, step, destroy the application.
     */
    void init(DataObject config, Context ctx) throws Exception;
    void start(Context ctx) throws Exception;
    void stop(Context ctx) throws Exception;
    void destroy(Context ctx) throws Exception;
    ExecStatus getStatus();

    /**
     * Create and connect the connection.
     */
    void createConnection(String connectionId, DataObject config, Context ctx) throws Exception;
    void connect(String connectionId, Context ctx) throws Exception;
    void disconnect(String connectionId, Context ctx) throws Exception;
    void removeConnection(String connectionId, Context ctx) throws Exception;
    ConnectionStatus getConnectionStatus(String connectionId);

    /**
     * Add (bind) or remove an integration to an endpoint of the application.
     */
    void addEndpointForIntegration(String endpointId, String integrationId, String connectionId, DataObject config) throws Exception;
    boolean hasEndpointForIntegration(String endpointId, String integrationId);
    void removeEndpointForIntegration(String endpointId, String integrationId) throws Exception;

    /**
     * Get all integrations having beed added to some endpoint of the application.
     */
    String[] getRelativeIntegrations();

    DataObject callOutboundEndpoint(String endpointId, String integrationId, DataObject msg, Context ctx) throws Exception;

}
