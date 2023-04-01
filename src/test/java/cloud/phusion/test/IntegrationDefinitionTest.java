package cloud.phusion.test;

import static org.junit.Assert.*;

import cloud.phusion.DataObject;
import cloud.phusion.integration.StepEndpoint;
import cloud.phusion.integration.IntegrationDefinition;
import cloud.phusion.integration.StepJavaScript;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.*;

public class IntegrationDefinitionTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testIntegrationDefinition() throws Exception {
        String file = this.getClass().getClassLoader().getResource("").getPath() + "workflow.json";

        IntegrationDefinition it = new IntegrationDefinition();
        it.setWorkflow(file);

        assertEquals( it.getSteps().length, 8 );
        assertEquals( it.getFirstStep().getId(), "01" );
        assertEquals( it.getExceptionStep().getId(), "exception" );
        assertEquals( it.getNextSteps("01").length, 2 );
        assertEquals( it.getNextSteps("02").length, 2 );
        assertEquals( it.getNextSteps("03").length, 1 );
        assertEquals( it.getNextSteps("04").length, 1 );
        assertEquals( it.getNextSteps("05").length, 1 );
        assertEquals( it.getNextSteps("06").length, 1 );
        assertNull( it.getNextSteps("07") );

        StepEndpoint step = (StepEndpoint)it.getStepById("03");
        JSONObject prop = step.getConfig().getJSONObject();
        assertEquals( prop.getInteger("callLimitPerSecond"), new Integer(100) );

        assertEquals( "07", it.getNextCollectStep("05").getId() );

        assertFalse( ((StepJavaScript) it.getStepById("02")).isAsync() );
        assertTrue( ((StepJavaScript) it.getStepById("04")).isAsync() );
    }

    @After
    public void tearDown() {
    }

}
