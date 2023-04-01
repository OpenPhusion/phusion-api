package cloud.phusion;

import cloud.phusion.application.Application;
import cloud.phusion.integration.Integration;
import cloud.phusion.integration.Transaction;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpServer;
import cloud.phusion.integration.IntegrationDefinition;
import cloud.phusion.storage.DBStorage;
import cloud.phusion.storage.FileStorage;
import cloud.phusion.storage.KVStorage;

import java.util.Date;

/**
 * The Runtime Engine.
 *
 * It must be thread-safe.
 */
public interface Engine {

    // 1. Fundamental methods
    // ******************************************************

    /**
     * Get version of the engine.
     */
    String getVersion();

    /**
     * Get ID of the engine.
     *
     * It should be unique in the cluster.
     */
    String getId();

    /**
     * Manipulate the engine.
     */
    void start(Context ctx) throws Exception;
    void stop(Context ctx) throws Exception;
    ExecStatus getStatus();

    /**
     * Manipulate Java module.
     *
     * If the module is loaded already, refresh it.
     *
     * The jars should have all dependencies packaged together with mvn assembly:single.
     *
     * @param moduleId
     * @param filePaths paths to the .jar files
     */
    void loadJavaModule(String moduleId, String[] filePaths, Context ctx) throws Exception;
    void unloadJavaModule(String moduleId, Context ctx) throws Exception;
    boolean doesJavaModuleExist(String moduleId);
    Object createClassInstance(String moduleId, String className, Context ctx) throws Exception;

    /**
     * Manipulate JavaScript module.
     *
     * If the module is loaded already, it will be refreshed.
     *
     * For node.js modules, the node, npm, package.json should be installed beforehand.
     * The modules will be installed in the directory where the application is started.
     *
     * When installing, nodejsModule can be "module_name" or "module_name@version", e.g. "md5-node@@2.1.0",
     * but when uninstalling, the version should not be specified.
     *
     * @param moduleId
     * @param filePath path to the .js file
     */
    void loadJavaScriptModule(String moduleId, String filePath, Context ctx) throws Exception;
    void unloadJavaScriptModule(String moduleId, Context ctx) throws Exception;
    boolean doesJavaScriptModuleExist(String moduleId);
    void installNodeJSModule(String nodejsModule, Context ctx) throws Exception;
    void uninstallNodeJSModule(String nodejsModule, Context ctx) throws Exception;
    DataObject listNodeJSModules(Context ctx) throws Exception;
    boolean doesNodeJSModuleExist(String nodejsModule);
    void runJavaScriptWithTransaction(String moduleId, Transaction trx) throws Exception;
    void runJavaScriptWithTransaction(String moduleId, Transaction trx, boolean async) throws Exception;

    /**
     * The script must have "exports._run=function(){ return \"some text\" };"
     */
    String runJavaScriptFile(String filePath, boolean async, boolean reload, Context ctx) throws Exception;

    /**
     * The script must return a string, such as "(function(){ return \"some text\" })()"
     */
    String runJavaScript(String script, Context ctx) throws Exception;



    // 2. Services
    // ******************************************************

    long generateUniqueId(Context ctx) throws Exception;

    KVStorage getKVStorageForApplication(String applicationId) throws Exception;
    KVStorage getKVStorageForIntegration(String integrationId) throws Exception;
    KVStorage getKVStorageForClient(String clientId) throws Exception;
    FileStorage getFileStorageForApplication(String applicationId) throws Exception;
    FileStorage getFileStorageForIntegration(String integrationId) throws Exception;
    FileStorage getFileStorageForClient(String clientId) throws Exception;
    DBStorage getDBStorageForApplication(String applicationId) throws Exception;
    DBStorage getDBStorageForIntegration(String integrationId) throws Exception;
    DBStorage getDBStorageForClient(String clientId) throws Exception;

    /**
     * Get HTTP/1.1 Client.
     */
    HttpClient createHttpClient() throws Exception;

    /**
     * Manipulate HTTP/1.1 services.
     *
     * @param path related URL to provide service. URLs can have parameters, such as "/object/{id}"
     */
    void registerHttpServer(String path, HttpServer server, DataObject config, Context ctx) throws Exception;
    void registerHttpServer(String path, HttpServer server, Context ctx) throws Exception;
    void unregisterHttpServer(String path, Context ctx) throws Exception;
    boolean doesHttpServerExist(String path);

    /**
     * Schedule CRON tasks.
     *
     * CRON Expression:
     * 1. Parts: Secs Mins Hours MonthDay Month WeekDay Year, where the Year is optional.
     * 2. Value range of each part: Secs 0-59, Mins 0-59, Hours 0-23, MonthDay 1-31, Month 0-11, WeekDay 1-7 (1 is Sunday), Year (4 digits).
     * 3. Multiple values: WeekDay=2-4,7 means Monday, Tuesday, Wednesday, Saturday.
     * 4. *: any. MonthDay and WeekDay can not be "*" at the same time, change one to "?".
     * 5. n/m: from n, each step add m. By default, n=0, e.g. /m = 0/m.
     * 6. L: the last day. MonthDay=L, the last day of the month. L-3, 3 days before the last day; WeekDay=1L, the last Sunday.
     * 7. #: WeekDay=1#3, the 3rd Sunday.
     *
     * Examples:
     * 0 0/5 * * * ? (every 5 minutes)
     * 0 0 12 ? * 4 (12:00:00, every wednesday)
     * 0 30 10-13 ? * * (10:30, 11:30, 12:30, 13:30 every day)
     * 0 0/30 8-9 5,20 * ? 2022-2025 (08:00, 08:30, 09:00, 09:30, on the 5th and 20th day of every month, from 2022 to 2025)
     *
     * @param clustered whether to run the task in cluster mode, the default is true
     */
    void scheduleTask(String taskId, ScheduledTask task, String cron, boolean clustered, Context ctx) throws Exception;
    void scheduleTask(String taskId, ScheduledTask task, String cron, Context ctx) throws Exception;

    /**
     * Schedule periodic tasks.
     *
     * @param startTime if null, start at once
     * @param repeatCount 0 indicates repeat forever
     * @param clustered whether to run the task in cluster mode, the default is false
     */
    void scheduleTask(String taskId, ScheduledTask task, Date startTime, long intervalInSeconds, long repeatCount, boolean clustered, Context ctx) throws Exception;
    void scheduleTask(String taskId, ScheduledTask task, Date startTime, long intervalInSeconds, long repeatCount, Context ctx) throws Exception;
    void scheduleTask(String taskId, ScheduledTask task, long intervalInSeconds, long repeatCount, Context ctx) throws Exception;
    void scheduleTask(String taskId, ScheduledTask task, long intervalInSeconds, Context ctx) throws Exception;
    void removeScheduledTask(String taskId, Context ctx) throws Exception;

    void clearAllScheduledTasks(Context ctx) throws Exception;
    boolean doesTaskExist(String taskId);



    // 3. Lifecycle of Application and Integration
    // ******************************************************

    /**
     * Manipulate applications.
     *
     * @param moduleId ID of the Java module
     * @param appClassName Java Class Name which implements the Application interface
     */
    String registerApplication(String applicationId, String moduleId, String appClassName, DataObject config, Context ctx) throws Exception;
    String registerApplication(String applicationId, String appClassName, DataObject config, Context ctx) throws Exception;
    void removeApplication(String applicationId, Context ctx) throws Exception;
    Application getApplication(String applicationId) throws Exception;
    ExecStatus getApplicationStatus(String applicationId);

    /**
     * Manipulate the integration.
     */
    void registerIntegration(String integrationId, IntegrationDefinition idef, DataObject config, Context ctx) throws Exception;
    void registerIntegration(String integrationId, String clientId, IntegrationDefinition idef, DataObject config, Context ctx) throws Exception;
    void removeIntegration(String integrationId, Context ctx) throws Exception;
    Integration getIntegration(String integrationId) throws Exception;
    ExecStatus getIntegrationStatus(String integrationId);

    /**
     * data = {expression, vars, msg, config}
     */
    boolean evaluateCondition(DataObject data) throws Exception;
    boolean evaluateCondition(String condition, String msg, String config) throws Exception;

}
