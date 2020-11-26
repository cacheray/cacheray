import core.SimpleSim;
import core.TraceFile;
import core.TraceFileReader;
import org.junit.*;
import types.Configuration;
import types.RTTA;
import types.Struct;
import types.TypeData;

import java.io.*;
import java.util.LinkedList;

public class TestSimpleSim{
    private static final String BIG_TRACE_FILE_NAME = TestSimpleSim.class.getResource("big_test/big.trace").getFile();
    private static final String BIG_DWARF_DATA_FILE_NAME = TestSimpleSim.class.getResource("big_test/big.dwarf.json").getFile();
    private static final String BIG_CONFIGURATION_FILE_NAME = TestSimpleSim.class.getResource("big_test/cacheSetup.json").getFile();

    private static final String STRUCTS_TRACE_FILE_NAME = TestSimpleSim.class.getResource("struct_test/cacheray.0.trace").getFile();
    private static final String STRUCTS_DWARF_DATA_FILE_NAME = TestSimpleSim.class.getResource("struct_test/structs.dwarf.json").getFile();
    private static final String STRUCTS_CONFIGURATION_FILE_NAME = TestSimpleSim.class.getResource("struct_test/cacheSetup.json").getFile();

    private SimpleSim sim;
	private TypeData typeData;
	private TraceFile traceFile;
	private Configuration configuration;

    @After
    public void tearDown() {
        sim = null;
    }

    @Test
    public void testBigRun() throws IOException {
        traceFile = new TraceFile(new FileInputStream(BIG_TRACE_FILE_NAME));
        typeData = new TypeData(BIG_DWARF_DATA_FILE_NAME, 1);
        configuration = Configuration.createConfigFromFile(BIG_CONFIGURATION_FILE_NAME);
        //Configuration configuration = Configuration.giveBasicConfig();
        LinkedList<TraceFile> traceFiles = new LinkedList<TraceFile>();
        traceFiles.add(traceFile);
        sim = new SimpleSim(new TraceFileReader(traceFiles), typeData, configuration, new BufferedWriter(new OutputStreamWriter(OutputStream.nullOutputStream())), 1);
        sim.printSetup();

        // do the simulation
        sim.simulateTrace(false);

        System.out.println("PRINTING STRUCT LIST");
        for (RTTA s : sim.getRTTAList()) {
            System.out.println(s.toString());
        }
    }

    @Test
    public void testStructRun() throws IOException {
        traceFile = new TraceFile(new FileInputStream(STRUCTS_TRACE_FILE_NAME));
        typeData = new TypeData(STRUCTS_DWARF_DATA_FILE_NAME, 1);
        configuration = Configuration.createConfigFromFile(STRUCTS_CONFIGURATION_FILE_NAME);
        //Configuration configuration = Configuration.giveBasicConfig();
        LinkedList<TraceFile> traceFiles = new LinkedList<TraceFile>();
        traceFiles.add(traceFile);
        sim = new SimpleSim(new TraceFileReader(traceFiles), typeData, configuration, new BufferedWriter(new OutputStreamWriter(OutputStream.nullOutputStream())), 1);
        sim.printSetup();

        // do the simulation
        sim.simulateTrace(false);

        System.out.println("PRINTING STRUCT LIST");
        for (RTTA s : sim.getRTTAList()) {
            System.out.println(s.toString());
        }
    }

}
