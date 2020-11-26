import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import types.Configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestConfiguration {
    private static final String EMPTY_FILE = TestConfiguration.class.getResource("configuration_files/empty.json").getFile();
    private static final String BASIC_FILE = TestConfiguration.class.getResource("configuration_files/basic.json").getFile();
    private static final String TWO_LAYER_FILE = TestConfiguration.class.getResource("configuration_files/two_layer.json").getFile();


    private Configuration configuration;

    @After
    public void reset(){
        configuration = null;
    }

    @Test
    public void testEmpty() {
        configuration = Configuration.createConfigFromFile(EMPTY_FILE);
        assertNull(configuration);
    }

    @Test
    public void testBasic() {
        configuration = Configuration.createConfigFromFile(BASIC_FILE);
        assertEquals("test length of cache list",1, configuration.getNumberOfCaches());
        assertEquals("test cache size",16, configuration.getCacheSize(0));
        // TODO: Add rest of them maybe?
    }

    @Test
    public void testTwoLayer() {
        configuration = Configuration.createConfigFromFile(TWO_LAYER_FILE);
        assertEquals("test length of cache list", 2, configuration.getNumberOfCaches());
        // TODO: Add rest of the tests.
    }
}
