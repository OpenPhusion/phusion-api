package cloud.phusion.protocol.http;

public enum HttpMethod {
    GET,
    POST,
    PUT, // Idempotent, so when create an instance, it must be named by the request, not by the server
    DELETE,
    OPTIONS,
    HEAD,
    PATCH,
    TRACE
}
