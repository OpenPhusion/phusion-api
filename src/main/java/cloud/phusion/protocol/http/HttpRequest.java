package cloud.phusion.protocol.http;

import cloud.phusion.DataObject;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Value Object for HTTP request information.
 *
 * It is not thread-safe.
 */
public class HttpRequest {

    private HttpMethod method;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private DataObject body;
    private Map<String, InputStream> files;

    /**
     * @param method HTTP Method
     * @param relativeUrl HTTP url (relative to the service root)
     * @param headers HTTP Headers
     * @param parameters HTTP parameters in URL, query string or body
     * @param body HTTP Body
     */
    public HttpRequest(HttpMethod method, String relativeUrl, Map<String, String> headers, Map<String, String> parameters, DataObject body) {
        super();

        this.method = method;
        this.url = relativeUrl;
        this.headers = headers;
        this.parameters = parameters;
        this.body = body;
        this.files = null;
    }

    /**
     * Multiple files uploaded
     */
    public HttpRequest(HttpMethod method, String relativeUrl, Map<String, String> headers, Map<String, String> parameters, Map<String, InputStream> files) {
        super();

        this.method = method;
        this.url = relativeUrl;
        this.headers = headers;
        this.parameters = parameters;
        this.body = null;
        this.files = files;
    }

    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Relative URL.
     *
     * If there's parameter value in the path, such as "/object/12345", the relative URL will translate it to parameter name, e.g. "/object/{id}"
     */
    public String getRelativeUrl() {
        return url;
    }

    public String getHeader(String header) {
        return (headers != null) ? headers.get(header) : null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * HTTP parameters:
     * 1. In-path Variable: e.g. "/object/{id}", where "id" is the parameter name.
     * 2. Query String: parameters after "?" in the URL.
     * 3. URL Encoded Form：parameters in the HTTP body of type "application/x-www-form-urlencoded".
     */
    public String getParameter(String parameter) {
        return (parameters != null) ? parameters.get(parameter) : null;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public DataObject getBody() {
        return body;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(method.toString()).append(" ")
                .append(url).append(" headers=")
                .append(headers.toString()).append(" parameters=")
                .append(parameters.toString()).append(" body=")
                .append(body.getString());

        return result.toString();
    }

    public boolean hasFiles() {
        return files!=null && files.size()>0;
    }

    /**
     * It is up to the client and HttpServer to handle the encoding of the file names
     */
    public Set<String> getFileNames() {
        return files!=null ? files.keySet() : null;
    }

    /**
     * Better to close the stream after processing
     */
    public InputStream getFileContent(String fileName) {
        return files!=null ? files.get(fileName) :  null;
    }

}
