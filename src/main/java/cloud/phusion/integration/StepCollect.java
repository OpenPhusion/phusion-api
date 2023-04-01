package cloud.phusion.integration;

/**
 * Java step: collect messages after a ForEach step.
 *
 * The output message is a JSON array, whose items are one-one mapped to the message items from a ForEach step.
 */
public class StepCollect extends Step {

    public StepCollect(String id, String[] fromSteps) {
        super(id, fromSteps);
    }

}
