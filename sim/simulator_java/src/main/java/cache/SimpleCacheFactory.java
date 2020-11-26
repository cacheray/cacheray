package cache;

import types.Configuration;

public class SimpleCacheFactory{
	/**
	* Empty constructor for cache factory
	*/
	public SimpleCacheFactory(){

	}
	/**
	* Returns appropriate cache based on associativity
	*
	* @param cacheSize - Desired size of the cache in powers of 2
	* @param blockSize - Desired size of cache blocks/lines in power of 2 
	* @param writePolicy - the write policy //TODO: add elternatives
	* @param replacementPloicy - the replacement policy //TODO: add elternatives
	* @param associativity - associativity: 0 = fully associative, 
	*						1 = direct mapped, 
	*						n = n-set associative
	* @return - returns a cache based on associativity
	*/
	public SimpleCache getCache(Configuration conf){//, SimpleCache nextLevel){
		SimpleCache cache = null;
		for(int i = conf.getNumberOfCaches()-1; i >= 0; i--){
			if(conf.getAssociativity(i) == SimpleCache.DIRECT_MAP)
				cache = new DirectMappedCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache);
			else if(conf.getAssociativity(i) == SimpleCache.FULLY_ASSOCIATIVE)
				cache = new FullyAssociativeCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache);
			else if(conf.getAssociativity(i) > -1)
				cache = new NAssociativeCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache, conf.getAssociativity(i));
		}
		return cache;
	}
}