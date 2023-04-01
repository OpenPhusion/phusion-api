package cloud.phusion.integration;

/**
 * Java step: loop through each item.
 *
 * The input message must be a JSON array.
 * Against each item in the array, the following steps (until a Collect step) will be executed,
 * but the order of the item executions is not guaranteed.
 */
public class StepForEach extends Step {

    public StepForEach(String id, String[] fromSteps) {
        super(id, fromSteps);
    }

}
