package cloud.phusion.protocol.http;

import cloud.phusion.Context;

public interface HttpServer {

    /**
     * Handle the incoming HTTP request, and set the response to return to the requester.
     */
    void handle(HttpRequest request, HttpResponse response, Context ctx) throws Exception;

}
