package cloud.phusion;

public interface Context {

    Engine getEngine();

    /**
     * Set context information for precise logging.
     *
     * The context info should be provided by the caller,
     * after calling, it should be removed
     */
    void setContextInfo(String key, String info);
    void removeContextInfo(String key);
    void clearContextInfo();
    String getContextInfo(); // Return: "param=value, param=value"

    String getId(); // ID of the context. ID can be null

    /**
     * Logging.
     *
     * Data example:
     * String.format("param=%s, param=%d, param=%.fms, param=%tF %tT",
     *      stringValue, integerValue, floatValue, dateValue, dateValue)
     */
    void logInfo(String position, String msg);
    void logInfo(String position, String msg, String data);
    void logError(String position, String msg);
    void logError(String position, String msg, String data);
    void logError(String position, String msg, Throwable t);
    void logError(String position, String msg, String data, Throwable t);

}
