package cloud.phusion.protocol.http;

import cloud.phusion.Context;
import cloud.phusion.DataObject;

public interface HttpClient {

    HttpClient get(String url);
    HttpClient post(String url);
    HttpClient put(String url);
    HttpClient delete(String url);

    HttpClient header(String header, String value);
    HttpClient body(DataObject body);
    HttpClient body(String body);
    HttpClient context(Context ctx);

    HttpResponse send() throws Exception;

}
