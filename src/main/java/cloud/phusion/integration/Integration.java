package cloud.phusion.integration;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.ExecStatus;
import cloud.phusion.application.ConnectionStatus;

import java.util.Map;

/**
 * Integration.
 *
 * It must be thread-safe.
 */
public interface Integration {

    /**
     * ID of the integration.
     */
    String getId();
    void setId(String id);

    String getClientId();
    void setClientId(String id);

    /**
     * Initialize, start, step, destroy the application.
     */
    void init(DataObject config, Context ctx) throws Exception;
    void start(Context ctx) throws Exception;
    void stop(Context ctx) throws Exception;
    void destroy(Context ctx) throws Exception;
    ExecStatus getStatus();

    /**
     * Check whether the incoming message and integration configuration matches the start condition.
     */
    boolean canStart(DataObject msg) throws Exception;

    /**
     * Execute the integration from the beginning.
     */
    DataObject execute(DataObject msg, Context ctx) throws Exception;
    DataObject execute(Context ctx) throws Exception;

    /**
     * Create an instance of the integration.
     */
    Transaction createInstance(DataObject msg, String step, String previousStep, boolean failed, Map<String, Object> properties, Context ctx) throws Exception;
    Transaction createInstance(DataObject msg, Context ctx) throws Exception;
    Transaction createInstance(Context ctx) throws Exception;

    /**
     * Execute the integration from some step.
     */
    DataObject runInstance(Transaction trx) throws Exception;

    /**
     * Execute some step in the integration for testing.
     * When the integration is stopped, it still can be tested.
     *
     * @param moveOn whether execute the integration to the end. By default, it is false, execute just the step
     */
    void probe(Transaction trx, boolean moveOn) throws Exception;
    void probe(Transaction trx) throws Exception;

    /**
     * Update config of the integration.
     */
    void updateConfig(DataObject config, Context ctx) throws Exception;

    /**
     * Update output message of a direct step.
     */
    void updateStepMsg(String stepId, DataObject msg, Context ctx) throws Exception;

}
