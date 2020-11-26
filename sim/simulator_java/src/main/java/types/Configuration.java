package types;

import com.google.gson.Gson;

import java.io.*;
import java.lang.ArrayIndexOutOfBoundsException;
import java.util.List;

public class Configuration{
	private ConfigurationInternal configurationInternal;

	private int nCaches; 
	private String[] cacheNames;
	private int[] cacheSize;
	private int[] blockSize;
	private short[] associativity;
	private short[] writePolicy;
	private short[] replacementPolicy;

	public Configuration(int nCaches, String[] cacheNames, int[] cacheSize, int[] blockSize, short[] writePolicy, short[] replacementPolicy, short[] associativity){
		this.nCaches = nCaches;
		this.cacheNames = cacheNames;
		this.cacheSize = cacheSize;
		this.blockSize = blockSize;
		this.writePolicy = writePolicy;
		this.replacementPolicy = replacementPolicy;
		this.associativity = associativity;
	}

	private Configuration(ConfigurationInternal configurationInternal) {
		this.configurationInternal = configurationInternal;
	}


	public static Configuration createConfigFromFile(String fileName) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.err.println("Configuration file not found. Exiting.");
			System.exit(-1);
		}
		Gson gson = new Gson();
		ConfigurationInternal config = gson.fromJson(bufferedReader,ConfigurationInternal.class);
		if(config == null){
			return null;
		}
		return new Configuration(config);
	}

	private class ConfigurationInternal {
		private List<CacheSpec> caches;

		private class CacheSpec {
			public String name;
			public Integer cache_size;
			public Integer block_size;
			public Integer write_policy;
			public Integer set_assoc;
			public Integer rep_policy;
		}
	}

	public static Configuration giveBasicConfig() throws IOException {
		return new Configuration(1, new String[]{"L1"}, new int[]{16}, new int[]{6}, new short[]{0}, new short[]{0}, new short[]{1});
	}

	public int getNumberOfCaches(){
		return configurationInternal.caches.size();
		//return nCaches;
	}

	public String getCacheName(int n){
		try{
			return configurationInternal.caches.get(n).name;
			//return cacheNames[n];
		}catch(ArrayIndexOutOfBoundsException e){
			return "unnamed cache";	
		}
	}

	public int getCacheSize(int n){
		try{
			return configurationInternal.caches.get(n).cache_size;
			//return cacheSize[n];
		}catch(ArrayIndexOutOfBoundsException e){
			return 0;	
		}
	}

	public int getBlockSize(int n){
		try{
			return configurationInternal.caches.get(n).block_size;
			//return blockSize[n];
		}catch(ArrayIndexOutOfBoundsException e){
			return 0;
		}
	}

	public short getWritePolicy(int n){
		try{
			return configurationInternal.caches.get(n).write_policy.shortValue();
			//return writePolicy[n];
		}catch(ArrayIndexOutOfBoundsException e){
			return (short) 0;
		}
	}

	public short getAssociativity(int n){
		try{
			return configurationInternal.caches.get(n).set_assoc.shortValue();
			//return associativity[n];
		}catch(ArrayIndexOutOfBoundsException e){
			return (short) 0;
		}
	}

	public short getReplacementPolicy(int n){
		try{
			return configurationInternal.caches.get(n).rep_policy.shortValue();
			//return replacementPolicy[n];
		}catch(ArrayIndexOutOfBoundsException e){
			return (short) 1;
		}
	}
}