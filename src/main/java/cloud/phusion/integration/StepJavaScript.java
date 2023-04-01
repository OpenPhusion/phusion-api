package cloud.phusion.integration;

/**
 * JavaScript step: call a script
 */
public class StepJavaScript extends Step {
    private String scriptId = null;
    private boolean isAsync;

    public StepJavaScript(String id, String[] fromSteps, String scriptId) {
        this(id, fromSteps, scriptId, false);
    }

    public StepJavaScript(String id, String[] fromSteps, String scriptId, boolean isAsync) {
        super(id, fromSteps);

        this.scriptId = scriptId;
        this.isAsync = isAsync;
    }

    /**
     * The moduleId of the script
     */
    public String getScriptId() {
        return scriptId;
    }

    public boolean isAsync() {
        return isAsync;
    }
}
