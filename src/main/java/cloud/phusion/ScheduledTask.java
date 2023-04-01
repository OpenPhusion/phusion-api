package cloud.phusion;

public interface ScheduledTask {

    void run(String taskId, Context ctx);

}
