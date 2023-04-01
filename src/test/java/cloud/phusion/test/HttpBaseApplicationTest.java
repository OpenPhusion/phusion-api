package cloud.phusion.test;

import static org.junit.Assert.*;

import cloud.phusion.DataObject;
import cloud.phusion.application.Application;
import cloud.phusion.application.ConnectionStatus;
import cloud.phusion.ExecStatus;
import cloud.phusion.protocol.http.HttpMethod;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.FileStorage;
import cloud.phusion.test.util.ExampleApp;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;

public class HttpBaseApplicationTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testLifeCycle() throws Exception {
        Application app = new ExampleApp();
        app.init(new DataObject("{}"), null);

        assertEquals(app.getStatus(), ExecStatus.Stopped);

        app.start(null);
        assertEquals(app.getStatus(), ExecStatus.Running);

        app.stop(null);
        assertEquals(app.getStatus(), ExecStatus.Stopped);

        app.start(null);
        app.destroy(null);
        assertEquals(app.getStatus(), ExecStatus.Stopped);
    }

    @Test
    public void testConnection() throws Exception {
        Application app = new ExampleApp();
        app.init(new DataObject("{}"), null);

        app.createConnection("Conn1", new DataObject("{\"id\":1}"), null);
        app.createConnection("Conn2", new DataObject("{\"id\":2}"), null);

        assertEquals(app.getConnectionStatus("Conn1"), ConnectionStatus.Unconnected);
        assertEquals(app.getConnectionStatus("Conn3"), ConnectionStatus.None);

        app.start(null);

        String[] ids =  ((ExampleApp)app).getConnectionIds(false);

        assertEquals(app.getConnectionStatus("Conn1"), ConnectionStatus.Connected);

        app.disconnect("Conn1", null);

        app.removeConnection("Conn1", null);
        assertEquals(app.getConnectionStatus("Conn1"), ConnectionStatus.None);

        app.stop(null);
        assertEquals(app.getConnectionStatus("Conn2"), ConnectionStatus.Unconnected);

        app.start(null);
        assertEquals(app.getConnectionStatus("Conn2"), ConnectionStatus.Connected);
    }

    @Test
    public void testIntegration() throws Exception {
        Application app = new ExampleApp();
        app.init(new DataObject("{}"), null);

        app.addEndpointForIntegration("queryOrders", "ItA", "Conn1", new DataObject("{}"));
        app.addEndpointForIntegration("notifyOrder", "ItA", "Conn1", new DataObject("{}"));
        app.addEndpointForIntegration("notifyOrder", "ItB", "Conn2", new DataObject("{}"));

        assertArrayEquals(new String[]{"ItB","ItA"}, app.getRelativeIntegrations());

        app.removeEndpointForIntegration("notifyOrder", "ItB");
        assertArrayEquals(new String[]{"ItA"}, app.getRelativeIntegrations());

        app.removeEndpointForIntegration("notifyOrder", "ItA");
        assertArrayEquals(new String[]{"ItA"}, app.getRelativeIntegrations());

        app.removeEndpointForIntegration("queryOrders", "ItA");
        assertEquals(0, app.getRelativeIntegrations().length);
    }

    @Test
    public void testOutboundEndpoint() throws Exception {
        Application app = new ExampleApp();
        app.init(new DataObject("{}"), null);

        app.start(null);
        app.addEndpointForIntegration("queryOrders", "ItA", "Conn1", new DataObject("{\"epConfig\":0}"));

        app.createConnection("Conn1", new DataObject("{\"connConfig\":0}"), null);
        app.connect("Conn1", null);

        DataObject result = app.callOutboundEndpoint("queryOrders", "ItA", new DataObject("{}"), null);
        assertEquals("{\"status\":\"OK\"}", result.getString());
    }

    @Test
    public void testInboundEndpointByItKey() throws Exception {
        String appId = "sample";
        ExampleApp app = new ExampleApp();
        app.setId(appId);
        app.init(new DataObject("{}"), null);

        Map<String, String> params = new HashMap<String, String>();
        params.put("itid", "ItA");
        HttpRequest request = new HttpRequest(HttpMethod.POST, "/"+appId+"/order", null, params, new DataObject("{}"));

        HttpResponse response = new HttpResponse();

        app.start(null);
        app.addEndpointForIntegration("botifyOrder", "ItA", "Conn1", new DataObject("{\"epConfig\":0}"));

        app.createConnection("Conn1", new DataObject("{\"connConfig\":0}"), null);
        app.connect("Conn1", null);

        app.handle(request, response, null);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testInboundEndpointByConnKey() throws Exception {
        String appId = "sample";
        ExampleApp app = new ExampleApp();
        app.setId(appId);
        app.init(new DataObject("{}"), null);

        Map<String, String> params = new HashMap<String, String>();
        params.put("user", "luyao");
        HttpRequest request = new HttpRequest(HttpMethod.POST, "/"+appId+"/order", null, params, new DataObject("{}"));

        HttpResponse response = new HttpResponse();

        app.start(null);
        app.addEndpointForIntegration("botifyOrder", "ItA", "Conn1", new DataObject("{\"epConfig\":0}"));

        app.createConnection("Conn1", new DataObject("{\"user\":\"luyao\"}"), null);
        app.connect("Conn1", null);

        app.handle(request, response, null);
        assertEquals(response.getStatusCode(), 200);
    }

    @After
    public void tearDown() {
    }
}
