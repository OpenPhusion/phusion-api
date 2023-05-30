package cloud.phusion.application;

import cloud.phusion.*;
import cloud.phusion.integration.Direction;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.protocol.http.HttpServer;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Framework (template) to ease the implemenation of Application inteface
 */
public abstract class HttpBaseApplication implements Application, HttpServer {
    private static final String _position = HttpBaseApplication.class.getName();

    private class _InboundEndpoint {
        public String httpPath;
        public String connectionKeyInReqeust;
        public Method method;

        public _InboundEndpoint(String httpPath, String connectionKeyInReqeust, Method method) {
            super();
            this.httpPath = httpPath;
            this.method = method;
            this.connectionKeyInReqeust = connectionKeyInReqeust;
        }
    }

    private JSONObject appConfig;
    private boolean stopped;

    // Endpoint Name -> Endpont Method
    private ConcurrentHashMap<String, Method> outboundEndpoints;
    // Endpoint Name -> Endpont Object
    private ConcurrentHashMap<String, _InboundEndpoint> inboundEndpoints;
    private String connectionKeyInConfig = null;

    // HTTP Path -> Endpoint ID
    private ConcurrentHashMap<String, String> pathToEndpointMap;

    // Connection ID -> Connection config and status
    private ConcurrentHashMap<String, JSONObject> connections;
    private ConcurrentHashMap<String, ConnectionStatus> connsStatus;

    // Connection key -> Connection ID
    private ConcurrentHashMap<String, String> connectionKeyToIdMap;

    // endpointId + integrationId -> connectionId and config
    private ConcurrentHashMap<String, String> integraionToConnMap;
    private ConcurrentHashMap<String, JSONObject> integraionToConfigMap;

    // integarionId -> number of bound endpoints. When the count is 0, the entry will be removed from the list
    private ConcurrentHashMap<String, Integer> integraionToCountMap;

    // endpointId + integrationKey -> integrationId
    private ConcurrentHashMap<String, String> integrationKeyToIdMap;

    public HttpBaseApplication() {
        super();

        appConfig = null;
        stopped = true;
        outboundEndpoints = null;
        inboundEndpoints = null;
        pathToEndpointMap = null;
        connections = null;
        connsStatus = null;
        connectionKeyToIdMap = null;
        integraionToConnMap = null;
        integraionToConfigMap = null;
        integraionToCountMap = null;
        integrationKeyToIdMap = null;
        appId = null;
    }

    private String appId = null;

    @Override
    public String getId() {
        return appId;
    }

    @Override
    public void setId(String id) {
        this.appId = id;
    }

    public JSONObject getApplicationConfig() {
        return appConfig;
    }

    @Override
    public void init(DataObject config, Context ctx) throws Exception {
        if (ctx != null)
            ctx.logInfo(_position, "Initializing application", "config="+(config==null?"":config.getString()));

        appConfig = config==null ? null : config.getJSONObject();
        connections = new ConcurrentHashMap<String, JSONObject>();
        connsStatus = new ConcurrentHashMap<String, ConnectionStatus>();
        outboundEndpoints = new ConcurrentHashMap<String, Method>();
        inboundEndpoints = new ConcurrentHashMap<String, _InboundEndpoint>();
        connectionKeyToIdMap = new ConcurrentHashMap<String, String>();
        integraionToConnMap = new ConcurrentHashMap<String, String>();
        integraionToConfigMap = new ConcurrentHashMap<String, JSONObject>();
        integraionToCountMap = new ConcurrentHashMap<String, Integer>();
        integrationKeyToIdMap = new ConcurrentHashMap<String, String>();

        // Get all endpoints by reflection

        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            Annotation[] anns = method.getAnnotations();
            if (anns==null || anns.length==0) continue;

            for (Annotation ann : anns) {
                if (ann instanceof InboundEndpoint) {
                    _InboundEndpoint endpoint = new _InboundEndpoint(
                            ((InboundEndpoint)ann).address(),
                            ((InboundEndpoint)ann).connectionKeyInReqeust(),
                            method
                    );
                    inboundEndpoints.put(method.getName(), endpoint);

                    // All inbound endpoints must have the same "connectionKeyInConfig" value, or the earlier will be overwritten
                    connectionKeyInConfig = ((InboundEndpoint)ann).connectionKeyInConfig();
                    if (connectionKeyInConfig!=null && connectionKeyInConfig.length()==0) connectionKeyInConfig = null;
                }
                else if (ann instanceof OutboundEndpoint) {
                    outboundEndpoints.put(method.getName(), method);
                }
            }
        }

        onInit(appConfig, ctx);

        if (ctx != null) ctx.logInfo(_position, "Application initialized");
    }

    protected void onInit(JSONObject config, Context ctx) throws Exception {}

    @Override
    public void start(Context ctx) throws Exception {
        if (! stopped) return;
        if (ctx != null) ctx.logInfo(_position, "Starting application");

        // Register HTTP server for all inbound endpoints

        String appRootUrl = "/"+getId();
        Engine engine = ctx!=null ? ctx.getEngine() : null;
        if (pathToEndpointMap == null) pathToEndpointMap = new ConcurrentHashMap<String, String>();

        Set<String> endpointIds = inboundEndpoints.keySet();
        for (String endpointId: endpointIds) {
            _InboundEndpoint endpoint = inboundEndpoints.get(endpointId);

            pathToEndpointMap.put(appRootUrl + endpoint.httpPath, endpointId);

            if (engine != null) {
                if (ctx != null) ctx.setContextInfo("endpointId", endpointId);
                engine.registerHttpServer(appRootUrl + endpoint.httpPath, this, ctx);
                if (ctx != null) ctx.removeContextInfo("endpointId");
            }
        }

        onStart(ctx);
        stopped = false;

        // Connect all connections.
        // If a connection is manually stopped, it still will be re-connected

        String[] connIds = getConnectionIds(false);
        if (connIds != null) {
            for (int i = 0; i < connIds.length; i++) {
                if (connsStatus.get(connIds[i]) == ConnectionStatus.Unconnected) {
                    if (ctx != null) ctx.setContextInfo("connectionId", connIds[i]);
                    connect(connIds[i], ctx);
                    if (ctx != null) ctx.removeContextInfo("connectionId");
                }
            }
        }

        if (ctx != null) ctx.logInfo(_position, "Application started");
    }

    protected void onStart(Context ctx) throws Exception {}

    @Override
    public void stop(Context ctx) throws Exception {
        if (stopped) return;
        if (ctx != null) ctx.logInfo(_position, "Stopping application");

        Engine engine = ctx!=null ? ctx.getEngine() : null;

        // Check whether all bound integrations are stopped before the application

        String[] its = getRelativeIntegrations();

        if (its!=null && its.length>0) {
            for (int i=0; i<its.length; i++) {
                if (engine != null) {
                    if (engine.getIntegrationStatus(its[i]) == ExecStatus.Running) {
                        throw new PhusionException("APP_REL_IT", "Failed to stop application", "integrationId="+its[i], ctx);
                    }
                }
            }
        }

        // Unregister all inbound endpoints from the HTTP server

        if (pathToEndpointMap != null) {
            Enumeration<String> paths = pathToEndpointMap.keys();

            String path;
            while (paths.hasMoreElements()) {
                path = paths.nextElement();
                if (engine != null) engine.unregisterHttpServer(path, ctx);
            }

            pathToEndpointMap = null;
        }

        // Disconnect all connections

        String[] connIds = getConnectionIds(true);
        if (connIds != null) {
            for (int i = 0; i < connIds.length; i++) {
                if (ctx != null) ctx.setContextInfo("connectionId", connIds[i]);
                disconnect(connIds[i], ctx);
                if (ctx != null) ctx.removeContextInfo("connectionId");
            }
        }

        onStop(ctx);

        stopped = true;
        if (ctx != null) ctx.logInfo(_position, "Application stopped");
    }

    protected void onStop(Context ctx) throws Exception {}

    @Override
    public void destroy(Context ctx) throws Exception {
        stop(ctx);

        if (ctx != null) ctx.logInfo(_position, "Destroying application");
        onDestroy(ctx);
        if (ctx != null) ctx.logInfo(_position, "Application destroyed");
    }

    protected void onDestroy(Context ctx) throws Exception {}

    @Override
    public ExecStatus getStatus() {
        return stopped ? ExecStatus.Stopped : ExecStatus.Running;
    }

    @Override
    public void createConnection(String connectionId, DataObject config, Context ctx) throws Exception {
        JSONObject objConfig = config==null ? null : config.getJSONObject();
        if (objConfig == null) objConfig = new JSONObject();
        connections.put(connectionId, objConfig);
        connsStatus.put(connectionId, ConnectionStatus.Unconnected);

        String connKey = getConnectionKeyFromConfig(connectionId, objConfig);
        if (connKey!=null && connKey.length()>0) connectionKeyToIdMap.put(connKey, connectionId);
    }

    @Override
    public void connect(String connectionId, Context ctx) throws Exception {
        if (ctx!=null && stopped) {
            throw new PhusionException("APP_STOP", "Failed to connect", ctx);
        }
        if (ctx!=null && ! connections.containsKey(connectionId)) {
            throw new PhusionException("CONN_NONE", "Failed to connect", ctx);
        }

        if (ctx != null) {
            ctx.setContextInfo("connectionId", connectionId);
            ctx.logInfo(_position, "Connecting");
        }

        onConnect(connectionId, connections.get(connectionId), ctx);
        connsStatus.put(connectionId, ConnectionStatus.Connected);

        if (ctx != null) {
            ctx.logInfo(_position, "Connected");
            ctx.removeContextInfo("connectionId");
        }
    }

    protected void onConnect(String connectionId, JSONObject config, Context ctx) throws Exception {}

    @Override
    public void disconnect(String connectionId, Context ctx) throws Exception {
        if (ctx!=null && stopped) {
            throw new PhusionException("APP_STOP", "Failed to disconnect", ctx);
        }
        if (ctx!=null && ! connections.containsKey(connectionId)) {
            throw new PhusionException("CONN_NONE", "Failed to disconnect", ctx);
        }

        if (ctx != null) {
            ctx.setContextInfo("connectionId", connectionId);
            ctx.logInfo(_position, "Disconnecting");
        }

        onDisconnect(connectionId, connections.get(connectionId), ctx);

        connsStatus.put(connectionId, ConnectionStatus.Unconnected);

        if (ctx != null) {
            ctx.logInfo(_position, "Disconnected");
            ctx.removeContextInfo("connectionId");
        }
    }

    protected void onDisconnect(String connectionId, JSONObject config, Context ctx) throws Exception {}

    @Override
    public void removeConnection(String connectionId, Context ctx) throws Exception {
        if (ctx!=null && connsStatus.get(connectionId) == ConnectionStatus.Connected) {
            throw new PhusionException("CONN_RUN", "Failed to remove connection", ctx);
        }

        connections.remove(connectionId);
        connsStatus.remove(connectionId);

        Enumeration<String> keys = connectionKeyToIdMap.keys();
        String key;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            if (connectionId.equals(connectionKeyToIdMap.get(key))) connectionKeyToIdMap.remove(key);
        }
    }

    public String[] getConnectionIds(boolean activeConnectionOnly) {
        if (connections == null) return null;
        else {
            String[] result;

            if (activeConnectionOnly) {
                ArrayList<String> arr = new ArrayList<String>();
                Set<String> keys = connsStatus.keySet();
                for (String key : keys) {
                    if (connsStatus.get(key) == ConnectionStatus.Connected) arr.add(key);
                }

                result = new String[arr.size()];
                arr.toArray(result);
            }
            else {
                result = new String[connections.keySet().size()];
                connections.keySet().toArray(result);
            }

            return result;
        }
    }

    public JSONObject getConnectionConfig(String connectionId) {
        if (connections == null) return null;
        else return connections.get(connectionId);
    }

    public JSONObject getIntegrationEndpointConfig(String endpointId, String integrationId) {
        if (endpointId==null || integrationId==null) return null;
        else return integraionToConfigMap.get(endpointId+integrationId);
    }

//    public void setConnectionConfig(String connectionId, Object config) {
//        JSONObject objConfig = JSON.parseObject((String)config);
//        if (objConfig == null) objConfig = new JSONObject();
//        connections.put(connectionId, objConfig);
//    }

    @Override
    public ConnectionStatus getConnectionStatus(String connectionId) {
        if (connsStatus == null) return ConnectionStatus.None;

        if (connsStatus.containsKey(connectionId)) return connsStatus.get(connectionId);
        else return ConnectionStatus.None;
    }

    @Override
    public void addEndpointForIntegration(String endpointId, String integrationId, String connectionId, DataObject config) throws Exception {
        JSONObject objConfig = config==null ? null : config.getJSONObject();
        if (objConfig == null) objConfig = new JSONObject();
        integraionToConnMap.put(endpointId+integrationId, connectionId);
        integraionToConfigMap.put(endpointId+integrationId, objConfig);

        Integer count = integraionToCountMap.get(integrationId);
        if (count == null) count = new Integer(0);
        count = count + 1;
        integraionToCountMap.put(integrationId, count);

        String key = getIntegrationKeyFromConfig(endpointId, integrationId, connectionId, objConfig);
        if (key != null) integrationKeyToIdMap.put(endpointId+key, integrationId);
    }

    protected String getConnectionKeyFromConfig(String connectionId, JSONObject config) {
        if (connectionKeyInConfig == null) return connectionId;
        else return config.getString(connectionKeyInConfig);
    }

    protected String getIntegrationKeyFromConfig(String endpointId, String integrationId, String connectionId, JSONObject config) {
        return integrationId;
    }

    @Override
    public boolean hasEndpointForIntegration(String endpointId, String integrationId) {
        return integraionToConnMap.containsKey(endpointId+integrationId);
    }

    @Override
    public void removeEndpointForIntegration(String endpointId, String integrationId) throws Exception {
        integraionToConnMap.remove(endpointId+integrationId);
        integraionToConfigMap.remove(endpointId+integrationId);

        Integer count = integraionToCountMap.get(integrationId);
        count = count - 1;
        if (count > 0) integraionToCountMap.put(integrationId, count);
        else integraionToCountMap.remove(integrationId);

        Enumeration<String> keys = integrationKeyToIdMap.keys();
        String key;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            if (integrationId.equals(integrationKeyToIdMap.get(key))) integrationKeyToIdMap.remove(key);
        }
    }

    @Override
    public String[] getRelativeIntegrations() {
        if (integraionToCountMap == null) return null;
        else {
            String[] result = new String[integraionToCountMap.keySet().size()];
            integraionToCountMap.keySet().toArray(result);
            return result;
        }
    }

    @Override
    public DataObject callOutboundEndpoint(String endpointId, String integrationId, DataObject msg, Context ctx) throws Exception {
        if (ctx!=null && stopped) {
            throw new PhusionException("APP_STOP", "Failed to call outbound endpoint", ctx);
        }

        String connectionId = integraionToConnMap.get(endpointId+integrationId);
        if (connectionId!=null && connectionId.length()==0) connectionId = null;

        if (ctx != null) {
            ctx.setContextInfo("applicationId", appId);
            ctx.setContextInfo("endpointId", endpointId);
            ctx.setContextInfo("integrationId", integrationId);
            ctx.setContextInfo("connectionId", connectionId == null ? "" : connectionId);

            ctx.logInfo(_position, "Calling outbound endpoint", "msg="+(msg==null?"":msg.getString(500)));
        }

        if (ctx!=null && connectionId!=null && getConnectionStatus(connectionId) != ConnectionStatus.Connected) {
            throw new PhusionException("CONN_NONE_STOP", "Failed to call outbound endpoint", ctx);
        }

        long t1 = System.nanoTime();

        DataObject result = null;
        try {
            result = onCallOutboundEndpoint(msg, endpointId, integrationId, connectionId, ctx);
        } catch (Exception ex) {
            if (ctx != null)
                throw new PhusionException("EP_FAIL", "Failed to call outbound endpoint", ctx, ex);
        }

        long t2 = System.nanoTime();

        if (ctx != null) {
            ctx.logInfo(_position, "Outbound endpoint called", String.format("result=%s, time=%.1fms",
                    result==null?"":result.getString(500), (t2 - t1) / 100000 / 10.0));

            ctx.removeContextInfo("applicationId");
            ctx.removeContextInfo("endpointId");
//            ctx.removeContextInfo("integrationId");
            ctx.removeContextInfo("connectionId");
        }
        return result;
    }

    protected DataObject onCallOutboundEndpoint(DataObject msg, String endpointId, String integrationId,
                                            String connectionId, Context ctx) throws Exception {
        Method method = outboundEndpoints.get(endpointId);

        if (method == null) throw new PhusionException("EP_NONE", "Failed to execute outbound endpoint");

        return (DataObject) method.invoke(this, msg, integrationId, connectionId, ctx);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, Context ctx) throws Exception {
        if (ctx!=null && stopped)
            throw new PhusionException("APP_STOP", "Failed to handle HTTP request", ctx);

        // Match path to find out the corresponding endpoint

        String path = request.getRelativeUrl();
        String endpointId = pathToEndpointMap.get(path);

        if (ctx!=null && endpointId==null) {
            throw new PhusionException("EP_NONE", "Failed to process inbound endpoint", "path="+path, ctx);
        }

        // Get connection ID from the HTTP request

        String connKey = getConnectionKeyFromHttpRequest(endpointId, request);
        String connectionId = (connKey==null) ? null : connectionKeyToIdMap.get(connKey);

        // Get integration ID from the HTTP request

        String integrationKey = getIntegrationKeyFromHttpRequest(endpointId, request);
        String integrationId = (integrationKey==null) ? null : integrationKeyToIdMap.get(endpointId+integrationKey);
        if (integrationId!=null && integrationId.length()==0) integrationId = null;

        if (integrationId!=null && connectionId==null) connectionId = integraionToConnMap.get(endpointId+integrationId);

        if (ctx != null) {
            ctx.setContextInfo("applicationId", appId);
            ctx.setContextInfo("endpointId", endpointId);
        }

        if (connectionId!=null && connectionId.length()>0) {
            if (ctx != null) ctx.setContextInfo("connectionId", connectionId);

            if (getConnectionStatus(connectionId) != ConnectionStatus.Connected) {
                throw new PhusionException("CONN_NONE_STOP", "Failed to process inbound endpoint", ctx);
            }

            if (integrationId != null) {
                if (ctx != null) ctx.setContextInfo("integrationId", integrationId);
                String[] integrationIds = new String[]{integrationId};

                onCallInboundEndpoint(request, response, endpointId, integrationIds, connectionId, ctx);

//                if (ctx != null) ctx.removeContextInfo("integrationId");
            }
            else {
                // From connection ID to locate the integration

                ArrayList<String> arr = new ArrayList<String>();
                int len = endpointId.length();

                Enumeration<String> keys = integraionToConnMap.keys();
                String key;
                while (keys.hasMoreElements()) {
                    key = keys.nextElement();

                    // "startsWith" is not accurate enough, it may match the wrong endpoint, e.g. order and orderList
                    if (key.startsWith(endpointId) && connectionId.equals(integraionToConnMap.get(key))) {
                        arr.add( key.substring(len) );
                    }
                }

                if (arr.size() == 0) {
                    // No integration bound to the endpoint
                    onCallInboundEndpoint(request, response, endpointId, null, connectionId, ctx);
                }
                else {
                    // Multiple integrations bound to the endpoint
                    String[] integrationIds = arr.toArray(new String[]{});
                    if (ctx != null) ctx.setContextInfo("integrationId", Arrays.toString(integrationIds));

                    onCallInboundEndpoint(request, response, endpointId, integrationIds, connectionId, ctx);

//                    if (ctx != null) ctx.removeContextInfo("integrationId");
                }
            }

            if (ctx != null) ctx.removeContextInfo("connectionId");
        }
        else {
            if (integrationId != null) {
                if (ctx != null) ctx.setContextInfo("integrationId", integrationId);
                String[] integrationIds = new String[]{integrationId};

                onCallInboundEndpoint(request, response, endpointId, integrationIds, null, ctx);

//                if (ctx != null) ctx.removeContextInfo("integrationId");
            }
            else {
                onCallInboundEndpoint(request, response, endpointId, null, null, ctx);
            }
        }

        if (ctx != null) {
            ctx.removeContextInfo("applicationId");
            ctx.removeContextInfo("endpointId");
        }
    }

    protected String getConnectionKeyFromHttpRequest(String endpointId, HttpRequest request) {
        _InboundEndpoint endpoint = inboundEndpoints.get(endpointId);
        return endpoint==null ? null : request.getParameter(endpoint.connectionKeyInReqeust);
    }

    protected String getIntegrationKeyFromHttpRequest(String endpointId, HttpRequest request) {
        return null;
    }

    protected void onCallInboundEndpoint(HttpRequest request, HttpResponse response,
                                           String endpointId, String[] integrationIds,
                                           String connectionId, Context ctx) throws Exception {
        _InboundEndpoint endpoint = inboundEndpoints.get(endpointId);

        if (endpoint == null) throw new PhusionException("EP_NONE", "Failed to execute inbound endpoint");

        endpoint.method.invoke(this, request, response, integrationIds, connectionId, ctx);
    }

}
