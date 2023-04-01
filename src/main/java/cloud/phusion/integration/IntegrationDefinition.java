package cloud.phusion.integration;

import cloud.phusion.DataObject;
import cloud.phusion.PhusionException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Definition of an integration.
 *
 * It is thread-safe.
 */
public class IntegrationDefinition {

    public static final String EXCEPTION_STEP_ID = "exception";

    private Step firstStep;
    private Step[] steps;
    private Map<String, Step> stepsMap; // Step ID -> Step Object
    private Map<String, Step[]> nextSteps; // Step ID -> Next steps

    private boolean clustered;
    private boolean periodic;
    private long intervalInSeconds;
    private long repeatCount;
    private Date startTime;
    private boolean cronScheduled;
    private String cron;
    private Object startCondition;

    public IntegrationDefinition() {
        super();

        this.periodic = false;
        this.clustered = false;
        this.cronScheduled = false;
        this.startCondition = null;
    }

    /**
     * Schedule the integration as periodic task.
     *
     * Can not be periodic and CRON at the same time.
     *
     * @param repeatCount 0 indicates repeat forever
     * @param clustered whether run in cluster mode. For periodic task, false by default; for CRON task, true by default
     */
    public void setPeriodicSchedule(long intervalInSeconds, long repeatCount, Date startTime, boolean clustered) throws Exception {
        if (this.cronScheduled) throw new PhusionException("SCH_P_CRON", "Can not set periodic schedule");

        this.intervalInSeconds = intervalInSeconds;
        this.repeatCount = repeatCount;
        this.startTime = startTime;
        this.periodic = true;
        this.clustered = clustered;
    }

    public void setPeriodicSchedule(long intervalInSeconds, long repeatCount) throws Exception {
        setPeriodicSchedule(intervalInSeconds, repeatCount, null, false);
    }

    /**
     * Schedule the integration as CRON task.
     *
     * Can not be periodic and CRON at the same time.
     */
    public void setCronSchedule(String cron, boolean clustered) throws Exception {
        if (this.periodic) throw new PhusionException("SCH_P_CRON", "Can not set CRON schedule");

        this.cron = cron;
        this.cronScheduled = true;
        this.clustered = clustered;
    }

    public void setCronSchedule(String cron) throws Exception {
        setCronSchedule(cron, true);
    }

    public boolean isPeriodic() {
        return periodic;
    }

    public boolean isCronScheduled() {
        return cronScheduled;
    }

    public boolean isClustered() {
        return clustered;
    }

    public long getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public long getRepeatCount() {
        return repeatCount;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getCron() {
        return cron;
    }

    /**
     * Read in the workflow definition.
     *
     * Format：
     * [
     *   {
     *     "id": String, // Step ID, unique in the integration
     *     "desc": String, // Description of the step. Optional
     *     "from": String | [ ], // ID of the previous step. null indicates it is the first step currently
     *
     *     "type": "direct", // Direct step
     *     "msg": Object,
     *
     *     "type": "processor", // JavaScript step
     *     "subtype": "javascript",
     *     "script": String, // Module ID of the script
     *     "async": Boolean, // Whether run asynchronously
     *
     *     "type": "processor", // Java step
     *     "subtype": "java",
     *     "module": String, // Module ID of Java package
     *     "class": String, // Name of the Java class implementing the cloud.phusion.integration.Processor interface
     *
     *     "type": "endpoint", // Endpoint step
     *     "direction": "in" | "out",
     *     "app": String, // Application ID
     *     "endpoint": String, // Endpoint ID
     *     "connection": String, // Connection ID
     *     "config": Object, // Config for the endpoint. Optional
     *
     *     "type": "forEach",
     *     "type": "collect"
     *   }
     * ]
     *
     * @param doc：JSON string, or path to the .json file
     */
    public void setWorkflow(String doc) throws Exception {
        if (doc==null || doc.length()<10)
            _initWorkflowDefinition( new Step[]{} );
        else {
            String c = doc.substring(0,1);
            if (!c.equals("[") && !c.equals("{")) {
                // Here, doc is the file path
                doc = _readWorkflowDefinitionFromFile(doc);
            }

            JSONArray docSteps = JSON.parseArray(doc);
            Step[] steps = _parseWorkflowDefinition(docSteps);
            _initWorkflowDefinition(steps);
        }
    }

    public void setWorkflow(JSONArray docSteps) {
        Step[] steps = _parseWorkflowDefinition(docSteps);
        _initWorkflowDefinition(steps);
    }

    public void setWorkflow(Step[] steps) {
        _initWorkflowDefinition(steps);
    }

    /**
     * Condition to start (match) the integration.
     *
     * Sometimes it is better to load condition as JSONObject, so String and JSONObject are both acceptable
     */
    public void setStartCondition(Object condition) {
        this.startCondition = condition;
    }

    public Object getStartCondition() {
        return startCondition;
    }

    /**
     * Get first step of the integration.
     */
    public Step getFirstStep() {
        return firstStep;
    }

    /**
     * Get the step to handle exceptions.
     */
    public Step getExceptionStep() { return stepsMap.get(IntegrationDefinition.EXCEPTION_STEP_ID); }

    /**
     * Get all steps in the integration.
     */
    public Step[] getSteps() {
        return steps;
    }

    public Step getStepById(String stepId) {
        return stepsMap.get(stepId);
    }

    /**
     * Get all possible next steps. Null indicates the currect step is the last one.
     */
    public Step[] getNextSteps(String stepId) {
        return nextSteps.get(stepId);
    }

    /**
     * Find the paired Collect step for a ForEach step.
     */
    public Step getNextCollectStep(String foreachStepId) {
        Step[] steps = nextSteps.get(foreachStepId);
        // All steps in a loop should always flow to the Collect step, so take the 0-th item only
        Step step = (steps!=null && steps.length>0) ? steps[0] : null;

        while (step!=null && !(step instanceof StepCollect)) {
            steps = nextSteps.get(step.getId());
            step = (steps!=null && steps.length>0) ? steps[0] : null;
        }

        return step;
    }

    //***************************************************************************************

    private void _initWorkflowDefinition(Step[] steps) {
        this.steps = steps;
        this.stepsMap = new ConcurrentHashMap<String, Step>();
        this.nextSteps = new ConcurrentHashMap<String, Step[]>();

        ConcurrentHashMap<String, List<Step>> nextStepsMap = new ConcurrentHashMap<String, List<Step>>();

        for (Step step : steps) {
            this.stepsMap.put(step.getId(), step);
            String[] fromSteps = step.getFromSteps();

            if (fromSteps == null || fromSteps.length == 0) {
                // No previous step and not exception step, then this is the first step
                if (! IntegrationDefinition.EXCEPTION_STEP_ID.equals(step.getId())) this.firstStep = step;
            }
            else {
                // Based on fromSteps (previous steps) to fill out nextSteps

                for (String fromStep : fromSteps) {
                    List<Step> list = nextStepsMap.get(fromStep);
                    if (list == null) {
                        list = new ArrayList<Step>();
                        nextStepsMap.put(fromStep, list);
                    }
                    list.add(step);
                }
            }
        }

        // Transfer Map<String, List<Step>> to Map<String, Step[]>

        for (String key : nextStepsMap.keySet()) {
            Step[] arr = new Step[ nextStepsMap.get(key).size() ];
            nextStepsMap.get(key).toArray(arr);
            this.nextSteps.put(key, arr);
        }
    }

    private String _readWorkflowDefinitionFromFile(String file) throws Exception {
        FileInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            is = new FileInputStream(file);
            os = new ByteArrayOutputStream();

            byte[] buff = new byte[1024];

            int len;
            while ((len = is.read(buff)) > 0) {
                os.write(buff, 0, len);
            }

            return os.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private Step[] _parseWorkflowDefinition(JSONArray docSteps) {
        ArrayList<Step> listSteps = new ArrayList<Step>();

        for (int i=0; i<docSteps.size(); i++) {
            JSONObject objStep = docSteps.getJSONObject(i);
            Step step = null;
            String stepId = objStep.getString("id");
            String[] stepFrom = _parseStepFrom( objStep.getString("from") );

            switch (objStep.getString("type")) {
                case "direct":
                    step = new StepDirect(stepId, stepFrom, new DataObject(objStep.getString("msg")));
                    break;
                case "processor":
                    switch (objStep.getString("subtype")) {
                        case "javascript":
                            step = new StepJavaScript(stepId,stepFrom,objStep.getString("script"),objStep.getBooleanValue("async",false));
                            break;
                        case "java":
                            step = new StepJava(stepId,stepFrom,objStep.getString("module"),objStep.getString("class"));
                            break;
                    }
                    break;
                case "endpoint":
                    step = new StepEndpoint(
                            stepId,
                            stepFrom,
                            objStep.getString("app"),
                            objStep.getString("endpoint"),
                            "in".equals(objStep.getString("direction")) ? Direction.In : Direction.Out,
                            objStep.getString("connection"),
                            new DataObject(objStep.getString("config"))
                    );
                    break;
                case "forEach":
                    step = new StepForEach(stepId, stepFrom);
                    break;
                case "collect":
                    step = new StepCollect(stepId, stepFrom);
                    break;
            }

            if (step != null) listSteps.add(step);
        }

        Step[] arrSteps = new Step[listSteps.size()];
        listSteps.toArray(arrSteps);
        return arrSteps;
    }

    private String[] _parseStepFrom(String from) {
        // "from" can a normal string, or a JSON string of an array

        if (from==null || from.length()==0)
            return null;
        else if (from.indexOf('[') == 0) {
            JSONArray arr = JSON.parseArray(from);
            String[] result = new String[arr.size()];
            for (int i=0; i<arr.size(); i++) result[i] = arr.getString(i);
            return result;
        }
        else
            return new String[]{from};
    }
}
