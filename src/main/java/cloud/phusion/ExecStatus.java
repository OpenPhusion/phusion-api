package cloud.phusion;

public enum ExecStatus {
    None, // Not registered
    Stopped, // Not running
    Running,
    Error // Running, but unavailable because of errors
}
