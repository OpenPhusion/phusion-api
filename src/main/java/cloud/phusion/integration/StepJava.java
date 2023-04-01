package cloud.phusion.integration;

/**
 * Java step: call a Processor
 */
public class StepJava extends Step {

    private String moduleId = null;
    private String processorClassName = null;

    public StepJava(String id, String[] fromSteps, String moduleId, String processorClassName) {
        super(id, fromSteps);

        this.moduleId = moduleId;
        this.processorClassName = processorClassName;
    }

    /**
     * Java class implementing the cloud.phusion.integration.Processor interface
     */
    public String getProcessorClass() {
        return processorClassName;
    }

    public String getModuleId() {
        return moduleId;
    }
}
