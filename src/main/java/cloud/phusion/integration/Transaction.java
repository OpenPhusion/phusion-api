package cloud.phusion.integration;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Transaction: instance of an integration.
 *
 * It is not thread-safe.
 */
public class Transaction {

    private String id;
    private String integrationId;
    private String clientId;
    private String step; // The current step
    private String stepFrom; // The previous step
    private DataObject msg;
    private Map<String, Object> properties;
    private DataObject config;
    private boolean failed = false;
    private Context ctx;
    private long timestamp;

    public Transaction(String integrationId, String id, String step, String stepFrom, Context ctx) {
        super();

        this.id = id;
        this.integrationId = integrationId;
        this.properties = new HashMap<String, Object>();
        this.step = step;
        this.stepFrom = stepFrom;
        this.msg = null;
        this.ctx = ctx;
        this.timestamp = System.nanoTime();

        if (IntegrationDefinition.EXCEPTION_STEP_ID.equals(step)) failed = true;
    }

    public Transaction(String integrationId, String id, String step, Context ctx) {
        this(integrationId, id, step, null, ctx);
    }

    public double getTimeInMilliseconds() {
        long t1 = this.timestamp;
        long t2 = System.nanoTime();
        this.timestamp = t2;
        return (t2-t1)/100000/10.0; // Use String.format("%.1fms",ms) to print the value
    }

    /**
     * ID of the transaction
     */
    public String getId() {
        return id;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String id) {
        clientId = id;
    }

    /**
     * null indicates the transaction is finished.
     */
    public String getCurrentStep() {
        return step;
    }

    /**
     * null indicates the transaction is just started.
     */
    public String getPreviousStep() {
        return stepFrom;
    }

    public void moveToStep(String step) {
        this.stepFrom = this.step;
        this.step = step;

        if (IntegrationDefinition.EXCEPTION_STEP_ID.equals(step)) failed = true;
    }

    /**
     * Some exception occurred, move the transaction into "exception" step.
     */
    public void moveToException(String exceptionMsg) {
        this.stepFrom = this.step;
        this.step = IntegrationDefinition.EXCEPTION_STEP_ID;
        failed = true;
        properties.put(IntegrationDefinition.EXCEPTION_STEP_ID, exceptionMsg);
    }

    /**
     * Finish the transaction.
     */
    public void moveToEnd() {
        this.stepFrom = this.step;
        this.step = null;
    }

    public Object getProperty(String property) {
        return properties.get(property);
    }

    public void setProperty(String property, Object value) {
        properties.put(property, value);
    }

    public Map<String, Object> getProperties() { return properties; }

    public DataObject getMessage() {
        return msg;
    }
    public void setMessage(DataObject msg) {
        this.msg = msg;
    }

    public DataObject getIntegrationConfig() {
        return config;
    }
    public void setIntegrationConfig(DataObject config) {
        this.config = config;
    }

    public Context getContext() {
        return ctx;
    }

    /**
     * Whether an exception has occurred.
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Whether the transaction is finished.
     */
    public boolean isFinished() {
        return this.step == null;
    }

    public String toJSONString() {
        JSONObject objProps = new JSONObject();
        if (properties!=null && properties.size()>0) objProps.putAll(properties);

        StringBuilder result = new StringBuilder();

        result.append("{");
        if (clientId != null)
            result.append("\"clientId\":\"").append(clientId).append("\",");
        if (step != null)
            result.append("\"step\":\"").append(step).append("\",");
        if (stepFrom != null)
            result.append("\"stepFrom\":\"").append(stepFrom).append("\",");
        if (msg != null)
            result.append("\"msg\":").append(msg.getString()).append(",");
        if (config != null)
            result.append("\"config\":").append(config.getString()).append(",");
        if (objProps.size() > 0)
            result.append("\"properties\":").append(objProps.toJSONString()).append(",");

        result.append("\"failed\":").append(failed).append(",");
        result.append("\"integrationId\":\"").append(integrationId).append("\",");
        result.append("\"id\":\"").append(id).append("\"");
        result.append("}");

        return result.toString();
    }

    /**
     * Read status data from JSON string.
     */
    public void updateFromJSONString(String strDoc) throws Exception {
        JSONObject doc = JSON.parseObject(strDoc);

        this.step = doc.getString("step");
        this.stepFrom = doc.getString("stepFrom");

        this.failed = doc.getBooleanValue("failed", false);

        JSONObject objm = doc.getJSONObject("msg");
        this.msg = objm==null ? null : new DataObject(objm);

        JSONObject objc = doc.getJSONObject("config");
        this.config = objc==null ? null : new DataObject(objc);

        properties.clear();

        JSONObject objp = doc.getJSONObject("properties");
        if (objp != null) {
            Set<String> props = objp.keySet();
            for (String prop : props) {
                Object v = objp.get(prop);
                v = (v instanceof BigDecimal) ? new Double(((BigDecimal) v).doubleValue()) : v;
                properties.put(prop, v);
            }
        }
    }

    public void updateAll(String step, String stepFrom, boolean failed, DataObject msg, DataObject config, Map<String, Object> properties) throws Exception {
        this.step = step;
        this.stepFrom = stepFrom;
        this.failed = failed;
        this.msg = msg;
        this.config = config;

        this.properties.clear();
        if (properties != null) this.properties.putAll(properties);
    }

}
