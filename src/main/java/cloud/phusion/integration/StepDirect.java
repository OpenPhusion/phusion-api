package cloud.phusion.integration;

import cloud.phusion.DataObject;

/**
 * Direct step: emit a message into the integration.
 */
public class StepDirect extends Step {
    private DataObject msg = null;

    public StepDirect(String id, String[] fromSteps) {
        super(id, fromSteps);
    }

    public StepDirect(String id, String[] fromSteps, DataObject msg) {
        super(id, fromSteps);

        this.msg = msg;
    }

    public DataObject getMessage() {
        return msg;
    }
    public void setMessage(DataObject msg) {
        this.msg = msg;
    }
}
