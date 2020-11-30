package cache;

import types.Configuration;

public class SimpleCacheFactory{
	/**
	* Empty constructor for cache factory
	*/
	public SimpleCacheFactory(){

	}

	public SimpleCache getCache(Configuration conf){//, SimpleCache nextLevel){
		SimpleCache cache = null;
		for(int i = conf.getNumberOfCaches()-1; i >= 0; i--){
			cache = new OmniCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i),conf.getAssociativity(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache );
/*
			if(conf.getAssociativity(i) == SimpleCache.DIRECT_MAP)
				//cache = new DirectMappedCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache);
				cache = new NAssociativeCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache, conf.getAssociativity(i));
			else if(conf.getAssociativity(i) == SimpleCache.FULLY_ASSOCIATIVE)
				cache = new FullyAssociativeCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache);
			else if(conf.getAssociativity(i) > -1)
				cache = new NAssociativeCache(conf.getCacheName(i), conf.getCacheSize(i), conf.getBlockSize(i), conf.getWritePolicy(i), conf.getReplacementPolicy(i), cache, conf.getAssociativity(i));*/
		}
		return cache;
	}
}