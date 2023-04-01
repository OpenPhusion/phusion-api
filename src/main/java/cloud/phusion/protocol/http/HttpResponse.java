package cloud.phusion.protocol.http;

import cloud.phusion.DataObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Value Object for HTTP response information.
 *
 * It is not thread-safe.
 */
public class HttpResponse {

    private int statusCode = 200;
    private Map<String, String> headers = new HashMap<String,String>();
    private DataObject body = null;

    public HttpResponse() {
        super();
    }

    /**
     * HTTP Status Code. By default, 200
     */
    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    /**
     * Manipulate HTTP Headers
     */
    public String getHeader(String header) {
        return headers.get(header);
    }
    public void setHeader(String header, String value) {
        headers.put(header, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public DataObject getBody() {
        return body;
    }
    public void setBody(DataObject body) {
        this.body = body;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("status=").append(statusCode)
                .append(" headers=").append(headers.toString())
                .append(" body=").append(body.getString());

        return result.toString();
    }

}
