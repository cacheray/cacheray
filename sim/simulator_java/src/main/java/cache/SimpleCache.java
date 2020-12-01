package cache;

import types.Address;
import types.Block;
import types.CacheAccess;

import java.util.Arrays;
import java.util.Collection;
import java.io.BufferedWriter;
import java.io.IOException;


public abstract class SimpleCache{
	public static final short FILO = 3;
	public static final short LRU = 0;
	public static final short FIFO = 1;
	public static final short LIFO = 2;
	public static final short RANDOM = 4;
	public static final int DIRECT_MAP = 1;
	public static final int FULLY_ASSOCIATIVE = 0;
	protected int cacheSize;
	protected int blockSize;
	protected short associativity;
	protected short writePolicy;
	protected short replacementPolicy;
	protected SimpleCache nextLevel;
	protected String name;

	protected int hits;
	protected int misses;
	protected int accesses;
	protected int reads;

	// TODO: Add bound checking so that cacheSize and blockSize have reasonalbe values
	public SimpleCache(String name, int cacheSize, int blockSize, short writePolicy, short replacementPolicy, SimpleCache nextLevel){
		this.name = name;
		this.cacheSize = 1 << cacheSize;
		this.blockSize = 1 << blockSize;
		this.writePolicy = writePolicy;
		this.replacementPolicy = replacementPolicy;
		this.nextLevel = nextLevel;

		hits = 0;
		misses = 0;
		accesses = 0;
		reads = 0;
	}

	public void read_cache(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca){

	}

	public abstract void read(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca);

	public abstract void write(Address addr, short size, BufferedWriter writer, boolean silent);

    public abstract int getHits();

    public abstract int getMisses();

    public void printStatistics(int n){
    	System.out.println(name + ": ");
		System.out.println(this.getAccesses() + " accesses");
    	System.out.println(this.getHits() + " hits. " + (this.getHits() / (double) getReads()) * 100 + "% hit rate.");
    	System.out.println(this.getMisses() + " misses. " + (this.getMisses() / (double) getReads()) * 100 + "% miss rate.");
    	System.out.println(" ");

    	if(nextLevel != null)
	    	nextLevel.printStatistics(++n);
    }

	private int getAccesses() {
    	return accesses;
	}

	private int getReads() {
    	return reads;
	}

	/**
	* Prints basic information about the cache
    */
	public String getFormattedCacheConfiguration(){
		StringBuilder sb = new StringBuilder();
		sb.append("cache size: ").append(cacheSize).append("\n")
		.append("block size: ").append(blockSize).append("\n")
		.append("write policy: ").append(writePolicy).append("\n")
		.append("replacement policy: ").append(replacementPolicy).append("\n")
		.append("cache type: ").append(getClass().getName()).append("\n");
		return sb.toString();
	}

	//////////////////////////////////////////////////////////
	//														//
	//	In order to write out cache hits/misses/evictions 	//
	//	to an output file (or to stdout for that matter) 	//
	//	the caches need to be able to print and write 		//
	//	whenever a cache event occurs. Hence we have 		//
	//	the following print/write functions					//
	//														//
	//////////////////////////////////////////////////////////

	protected boolean writeHit(BufferedWriter writer, Address addr, short size){
		try{
			writer.append(name + ": HIT " + addr.toString() + ", " + size + " bytes. Tag, index, offset" + Arrays.toString(addr.getTIO(512, 64)) + "\n");
			return true;
		}catch(IOException e){
			return false;
		}	
	}

	protected void printHit(Address addr, short size){
		System.out.println(name + ": HIT " + addr.toString() + ", " + size + " bytes");
	}	

	protected boolean writeMiss(BufferedWriter writer, Address addr, short size){
		try{
			writer.append(name + ": MISS " + addr.toString() + ", " + size + " bytes.  Tag, index, offset" + Arrays.toString(addr.getTIO(512, 64)) + "\n");
			return true;
		}catch(IOException e){
			return false;
		}
	}

	protected void printMiss(Address addr, short size){
		System.out.println(name + ": MISS " + addr.toString() + ", " + size + " bytes");
	}

	protected boolean writeEvicted(BufferedWriter writer, Address addr, Block block){
		try{
			writer.append(name + ": EVICTED content of block " + block.getTag() + ", previously " + block.getAddress().toString() + ", now " + addr.toString() + "\n");
			return true;
		}catch(IOException e){
			return false;
		}
	}

	protected void printEvicted(Address addr, Block block){
		System.out.println(name + ": EVICTED content of block " + block.getTag() + ", previously " + block.getAddress().toString() + ", now " + addr.toString());
	}

	public String getName() {return name;}
}
