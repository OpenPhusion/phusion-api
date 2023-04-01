package cloud.phusion.test.util;

import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.Context;
import cloud.phusion.application.InboundEndpoint;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.integration.Direction;
import cloud.phusion.integration.Integration;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;

public class ExampleApp extends HttpBaseApplication {

//    @Override
//    protected void onInit(JSONObject config, Context ctx) throws Exception {
//        System.out.println("init...");
//    }
//
//    @Override
//    protected void onStart(Context ctx) throws Exception {
//        System.out.println("start...");
//    }
//
//    @Override
//    protected void onStop(Context ctx) throws Exception {
//        System.out.println("stop...");
//    }
//
//    @Override
//    protected void onDestroy(Context ctx) throws Exception {
//        System.out.println("destroy...");
//    }

//    @Override
//    protected void onConnect(String connectionId, JSONObject config, Context ctx) throws Exception {
//        System.out.println("Connect "+connectionId+" ("+config.toJSONString()+")...");
//    }
//
//    @Override
//    protected void onDisconnect(String connectionId, JSONObject config, Context ctx) throws Exception {
//        System.out.println("Disonnect "+connectionId+" ("+config.toJSONString()+")...");
//    }

    @OutboundEndpoint
    public DataObject queryOrders(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        return new DataObject("{\"status\":\"OK\"}");
    }

    @InboundEndpoint(address="/order", connectionKeyInReqeust="user")
    public void notifyOrder(HttpRequest request, HttpResponse response, String[] integrationIds,
                                         String connectionId, Context ctx) throws Exception {

        if (ctx!=null && integrationIds!=null && integrationIds.length>0) {
            Engine engine = ctx.getEngine();
            for (int i = 0; i < integrationIds.length; i++) {
                Integration it = engine.getIntegration(integrationIds[i]);
                it.execute(null, ctx);
            }
        }

        response.setStatusCode(200);
        response.setBody(new DataObject("{\"status\":\"OK\"}"));
    }

}
