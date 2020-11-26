import core.TraceFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;

public class TestTraceFile {
    private static final String traceFileName = TestTraceFile.class.getResource("big_test/big.trace").getFile();
    private TraceFile traceFile;

    @Before
    public void setUp() {
        try {
            traceFile = new TraceFile(new FileInputStream(traceFileName));
        } catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @After
    public void tearDown() {
        traceFile = null;
    }

    @Test
    public void testBasic() {

    }
}
