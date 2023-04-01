package cloud.phusion.integration;

/**
 * Step of integration
 */
public class Step {
    private String id = null;
    private String[] fromSteps = null;

    public Step(String id, String[] fromSteps) {
        super();

        this.id = id;
        this.fromSteps = fromSteps;
    }

    /**
     * ID of the step. It should be unique in the integration.
     */
    public String getId() {
        return id;
    }

    /**
     * Get previous steps.
     */
    public String[] getFromSteps() {
        return fromSteps;
    }
}
