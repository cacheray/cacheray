package core;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.lang.UnsupportedOperationException;

import cache.SimpleCache;
import cache.SimpleCacheFactory;
import org.apache.commons.cli.*;
import types.*;
import com.opencsv.CSVReader;

// TODO: identify the direction in which the stack grows

public class SimpleSim{
	private final SimpleCache cache;
	private final RTTAContainer rttaContainer;
	private final TypeData typeData;
	private final TraceFileReader traceFileReader;
	private final BufferedWriter writer;
	private final List<CacheAccess> cacheAccesses;
	private int nCaches;
	private StringBuilder logBuilder;
	private long event_index;
	private Map<String, Map<String, Long>> remap;

	//colors taken from this stackoverflow thread: https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	/**
	 * This is the proper way of instancing the simulator
	 * @param traceFileReader - the tracefile to run the simulation on
	 * @param typeData - the type-data accompanying the trace-data
	 * @param configuration - the cache configuration to use
	 */
	public SimpleSim(TraceFileReader traceFileReader, TypeData typeData, Configuration configuration, BufferedWriter write, int nCaches) {
		this.traceFileReader = traceFileReader;
		this.typeData = typeData;
		this.writer = write;
		this.nCaches = nCaches;
		rttaContainer = new RTTAContainer();

		// Setup the cache
		SimpleCacheFactory cacheFactory = new SimpleCacheFactory();
		cache = cacheFactory.getCache(configuration);
		logBuilder = new StringBuilder();
		cacheAccesses = new ArrayList<>();
		event_index = 0;
		remap = null; // null until set outside
	}

	public void loadStaticStructs(List<RTTA> structList) {
		structList.forEach(rttaContainer::addRTTA);
	}

	public void loadStructRemap(Map<String, Map<String, Long>> remap) {

	}

	/**
	 * Get the list of structs for testing purposes.
	 *
	 * Will most likely get removed in a future version.
	 * @return a list of Struct
	 * @deprecated
	 */
	public List<RTTA> getRTTAList() {
		return rttaContainer.getRTTAList(); // TODO: remove?
	}

	// TODO: Actually print a user guide
	/**
	* Prints complete instructions on how to use the simulator
	*/
	public static void printUserGuide(){
		System.out.println("totally printing helpful stuff");
		System.exit(0);
	}

	/**
	* Prints hte most basic way to use the simulator, should be helpful
	* NOTE: THIS METHOD IS VERY OUTDATED!!! TODO: UPDATE
	*/
	public static void printInstruction(){
		System.out.println(ANSI_YELLOW + "In order to run the simulator with a " + ANSI_CYAN + "JSON" + ANSI_YELLOW + " based trace file, include the " + ANSI_CYAN + "-j" + ANSI_YELLOW + " flag." + ANSI_RESET);
		System.out.println(ANSI_YELLOW + "If instead you want to run the simulator with a " + ANSI_GREEN + "byte" + ANSI_YELLOW + " format, no flag is needed." + ANSI_RESET);
		System.out.println(ANSI_YELLOW + "In both cases the Trace file must obviously be specified.\n" +ANSI_RESET);
		System.out.println(ANSI_YELLOW + "For full instructions on how to use the simulator, use " +ANSI_WHITE + "-h, --help" + ANSI_RESET);
	}

	/**
	* Sends on the proper command to the cache.
	*
	* @param cache - the cache that recieves the op
	* @param mem - specifies the op and address.
	*/
	private void executeMemoryOp(SimpleCache cache, MemEvent mem, BufferedWriter writer, boolean silent) throws UnsupportedOperationException{
	    int et = TraceType.strip(mem.getType());
	    Address addr = mem.getAddr();
	    CacheAccess ca = new CacheAccess(nCaches, CacheAccess.READ);
	    CacheAccess caWrite = new CacheAccess(nCaches, CacheAccess.WRITE);
	    Address changedAddr;
	    switch(et){
		    case 0:
				changedAddr = rttaContainer.getChangedAddress(addr);
		        cache.read(changedAddr, mem.getSize(), writer, silent, ca);
				rttaContainer.access(addr, mem.getSize(), ca);
		        break;
		    case 1:
				changedAddr  = rttaContainer.getChangedAddress(addr);
		        cache.write(changedAddr, mem.getSize(), writer, silent);
				rttaContainer.access(addr, mem.getSize(), caWrite);
		        break;
		    default:
		        throw new UnsupportedOperationException(ANSI_RED + "Unknown opperation attempted, op-code: " + et + ANSI_RESET);
		}
		// cacheAccesses.add(ca);
	}

	//TODO: Actually register structs
	/**
	* Unfinished method for registering structs
	* @param structEvent - the struct event to register with the simulator
	*/
	private void registerRTTA(MallocEvent structEvent) {
		StructType type = typeData.getStructByName(structEvent.getStructName());
		//String name = structEvent.getStructName();
		rttaContainer.addRTTA(new RTTA(structEvent.getAddr(), type, structEvent.getElemCount()));
	}

	private void freeStruct(FreeEvent freeEvent) {
		// TODO: actually free the memory, how hard can it be?
		return;
	}


	/**
	* Given the name of a binary trace file this function will execute the 
	* cache simulation (initiating a cache, reading the trace, and producing 
	* a hit/miss result).
	*
	*/
	public void simulateTrace(boolean supressOutput){
		System.out.println(ANSI_YELLOW + "STARTING BINARY TRACE SIMULATION");
		System.out.println(ANSI_RESET);

		logBuilder.setLength(0); // reset StringBuilder
		Timer timer = new Timer();
		if (!supressOutput) {
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					System.out.print("Working on events: " + event_index + "                 \r");
				}
			}, 0, 1000);
		}
		event_index = 1;

		for(TraceEvent te : this.traceFileReader) {
			if(te.getType() == TraceType.RTTA_ADD){
				// Do something to structs
				// System.out.println("RTTA_ADDjk");
				registerRTTA((MallocEvent)te);
			} else if(te.getType() == TraceType.RTTA_REMOVE){
				// do something with free
				freeStruct((FreeEvent)te);
			} else {
				executeMemoryOp(this.cache, (MemEvent)te, writer, /*parser.silent()*/ true);
			}
			logBuilder.append(te.getEventAsString()).append("\n");
			event_index++;
		}

		timer.cancel();

		try{
			//writer.write(logBuilder.toString());
			writer.flush();
			writer.close();
		}catch(IOException e){
			//I really dont know what could cause a problem here...
			e.printStackTrace();
		}
		System.out.println(ANSI_YELLOW + "TRACE SIMULATION FINISHED" + ANSI_RESET);
		System.out.println("Processed " + (event_index-1) + " events");


		// DEBUG: print struct stats
		// will move this to some other place at some point
		/*for (Map.Entry<String, StructType> stringStructTypeEntry : typeData.getStructMap().entrySet()) {
			System.out.println(stringStructTypeEntry.getValue().getStats());
		}*/

		//System.out.println(structs.getStatsAsString());
		rttaContainer.printStats(nCaches);
		cache.printStatistics(1);
	}

	private void printUsingWriter(String s) {
		try {
			writer.write(s);
		} catch (IOException e) {
			System.err.println("Could not print due to IO Error.");
		}
	}

	public void printSetup() {
		printUsingWriter("SIMPLE SIM ver 0.2.0\nPrinting cache info\n");
		printUsingWriter(cache.getFormattedCacheConfiguration());
	}

	public String getTraceLog() {
		return logBuilder.toString();
	}

	public static CommandLine initOptions(String[] args) {
		Options options = new Options();

		/*
		Option traceFileInput = new Option("i", "input", true, "trace-file input");
		traceFileInput.setRequired(true);
		options.addOption(traceFileInput);
		 */

		Option traceFiles = new Option("i", "input", true, "trace-files to use");
		traceFiles.setRequired(true);
		traceFiles.setArgs(Option.UNLIMITED_VALUES);
		traceFiles.setValueSeparator(',');
		options.addOption(traceFiles);

		Option dwarfFileInput = new Option("d", "dwarf", true, "dwarf-file input");
		dwarfFileInput.setRequired(false);
		options.addOption(dwarfFileInput);

		Option configFileInput = new Option("c", "config", true, "config-file input");
		configFileInput.setRequired(true);
		options.addOption(configFileInput);

		Option stdoutOpt = new Option("s", "stdout", false, "use stdout");
		stdoutOpt.setRequired(false);
		options.addOption(stdoutOpt);

		Option staticStructsOpt = new Option("l", "struct-list", true, "static struct list to use");
		staticStructsOpt.setRequired(false);
		options.addOption(staticStructsOpt);

		Option structRemapOpt = new Option("r", "remap", true, "struct member remap list");
		structRemapOpt.setRequired(false);
		options.addOption(structRemapOpt);

		Option testCaseOpt = new Option("t", "test-case", false, "only print results");
		testCaseOpt.setRequired(false);
		options.addOption(testCaseOpt);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		try {
			return parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);
			System.exit(-1);
		}
		return null;
	}

	public static List<Struct> getStaticStructs(String fileName, TypeData typeData) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		String[] line;
		try {
			//begin_addr, end_addr, typename
			//0x10000000, 0x10ffffff, struct foo
			ArrayList<Struct> structList = new ArrayList<>();
			while ((line = reader.readNext()) != null) {
				// TODO: add structTypes to list even if a single element failed
				Address begin = new Address(line[0]);
				Address end = new Address(line[1]);
				String name = line[2];

				// Get struct info
				StructType structType = typeData.getStructByName(name);

				// Check that we have an actual StructType,
				// and not just a Placeholder
				if (structType == StructType.PLACEHOLDER) {
					// we actually need the info here.
					System.err.println("Struct \"" + name + "\" was not found in the type-data file.");
					return null;
				}

				// Check that the size is set
				if(structType.getSize() <= 0) {
					// Size is not set
					System.err.println("Struct of type \"" + structType.getName() + "\" has no size info.");
					return null;
				}
				long sSize = structType.getSize();
				structList.add(new Struct(begin,sSize,name,structType));
				Address temp = begin.advance(sSize);
				while (temp.lessThan(end) ) {
					structList.add(new Struct(begin.advance(sSize),sSize,name, structType));
					temp = temp.advance(sSize);
				}
			}
			return structList;
		} catch (Exception e) {
			return null;
		}
	}

	public static void rewriteStructTypes(String fileName, TypeData typeData) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		String[] line;
		try {
			while ((line = reader.readNext()) != null) {
				String structName = line[0];
				int[] reIndex = new int[line.length - 1];
				for (int i = 0; i < reIndex.length; i++)
					reIndex[i] = Integer.parseInt(line[i+1]);

			}
		}  catch (Exception e) {

		}
	}

	public static Map<String, Map<String,Long>> readStructRemapFile(String filename) throws IOException{
		Map<String, Map<String, Long>> pack = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			reader.lines().forEach((line) -> {
				// Here we decode the line
				String structName = line.split("=")[0];
				String structMemberInfo = line.split("=")[1];
				String[] changes = structMemberInfo.split(";");
				Map<String, Long> structMember = new HashMap<>();
				for (String member : changes) {
					String[] mem = member.split(",");
					structMember.put(mem[0], Long.decode(mem[1]));
				}
				pack.put(structName, structMember);
			});
		}
		return pack;
	}


	/**
	* Instantiates the simulator and runs the simulation
	*/
	public static void main(String[] args) throws FileNotFoundException {
		SimpleSim sim;
		CommandLine cmd;
		TraceFileReader traceFileReader;
		TypeData td = null;
		BufferedWriter writer;

		List<String> traceFileNames;

		// Parse cmd args
		cmd = initOptions(args);

		// Retrieve all the options
		String traceFileName = cmd.getOptionValue("input");
		String configFileName = cmd.getOptionValue("config");
		boolean stdoutSelected = cmd.hasOption("stdout");
		boolean testCaseSelected = cmd.hasOption("test-case");

		traceFileNames = Arrays.asList(cmd.getOptionValues("input"));
		if (traceFileNames.size() > 1) {
			List<FileInputStream> fileInputStreamList = new ArrayList<>();
			traceFileNames.forEach((name) -> {
				try {
					fileInputStreamList.add(new FileInputStream(name));
				} catch (FileNotFoundException e) {
					System.err.println("Could not find the file " + name + ". Continuing.");
				}
			});
			Queue<TraceFile> traceFileQueue = new LinkedList<>();
			for (FileInputStream f : fileInputStreamList) {
				traceFileQueue.add(new TraceFile(f));
			}
			traceFileReader = new TraceFileReader(traceFileQueue);
		} else {
			LinkedList<TraceFile> tf = new LinkedList<>();
			tf.add(new TraceFile(new FileInputStream(traceFileName)));
			traceFileReader = new TraceFileReader(tf);
		}

		Configuration conf = Configuration.createConfigFromFile(configFileName);

		int numCaches = conf.getNumberOfCaches();
		try {
			if (cmd.hasOption("dwarf")) {
				td = new TypeData(cmd.getOptionValue("dwarf"), numCaches);
			} else {
				td = TypeData.makeEmptyTypeData();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Type data file not found. Exiting.");
			System.exit(-1);
		}


		DateTimeFormatter format = DateTimeFormatter.ofPattern("HHmmss");

		try{
			if (stdoutSelected) {
				System.out.println("stdout selected");
				writer = new BufferedWriter(new OutputStreamWriter(System.out));
			} else writer = new BufferedWriter(new FileWriter(new File(traceFileNames+".SimulationOutput."+(String)format.format(LocalDateTime.now()))));
		}catch(IOException e){
			System.out.println(ANSI_RED+"Could not generate output file for the simulation"+ANSI_RESET);
			writer = new BufferedWriter(new OutputStreamWriter(OutputStream.nullOutputStream()));
		}

		sim = new SimpleSim(traceFileReader, td, conf, writer, numCaches);
/*
		// Load some static structs?
		if (cmd.hasOption("struct-list")) {
			//getStaticStructs(cmd.getOptionValue("l"), sim);
			List<Struct> structList = getStaticStructs(cmd.getOptionValue("struct-list"),td);
			if (structList != null) {
				// Load static structs
				sim.loadStaticStructs(structList);
			}
		}
*/
		// Load remaps
		if (cmd.hasOption("remap")) {
			Map<String, Map<String, Long>> remapMap = null;
			try {
				remapMap = readStructRemapFile(cmd.getOptionValue("remap"));
			} catch (IOException e) {
				System.err.println("Could not load struct remap file...");
			}
			if (remapMap != null) {

				for (StructType structType : td.getStructList()) {
					Map<String, Long> temp = remapMap.get(structType.getName());
					if (temp != null ) {
						System.out.println("Reordering " + structType.getName());
						structType.reorderMemberList(temp);
					} else {
						System.out.println("Could not find " + structType.getName());
					}
				}
			} else {
				System.err.println("Remap not configured... Something went wrong.");
			}
		}

		// Start the simulation
		sim.simulateTrace(testCaseSelected);

		// Print trace log to file
		try {
			BufferedWriter traceLogWriter = new BufferedWriter(new FileWriter("traceLog.txt"));
			traceLogWriter.write(sim.getTraceLog());
			traceLogWriter.close();
		} catch (IOException e) {
			System.err.println("Could not print trace log.");
		}
	}

}
