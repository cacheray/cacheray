package cache;

import types.Address;
import types.Block;
import types.CacheAccess;

import java.io.IOException;
import java.io.BufferedWriter;

import java.util.HashMap;
import java.util.Collection;
import java.util.Map;

public class NAssociativeCache extends SimpleCache{
	private final int nCacheBlocks;
	private final int nSets;
	private final int setSize;
	private Map<Integer, Collection<Block>> cache;
	private CacheReplacementManager crm;

	public NAssociativeCache(String name, int cacheSize, int blockSize, short writePolicy, short replacementPolicy, SimpleCache nextLevel, int n){
		super(name, cacheSize, blockSize, writePolicy, replacementPolicy, nextLevel);
		associativity = (short)n;
		nCacheBlocks = super.cacheSize / super.blockSize;
		nSets = nCacheBlocks / n;
		setSize = associativity;
		crm = new CacheReplacementManager(replacementPolicy);
		cache = new HashMap<>();
		
		for(int i = 0; i < nSets; i++)
			cache.put(i, crm.initBlocks(setSize, blockSize, associativity));
		System.out.println("Created cache " + name +" with block size " + super.blockSize + " sets " + nSets + " n_blocks " + nCacheBlocks + " assoc " + n + " cache size " + super.cacheSize);

	}

	public void read(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca){
		Address alignedAddr = addr.aligned(blockSize); // the address that marks the start of the block in which this "addr" is located
		Address topAddr = addr.advance(size);

		// for loop in case we stradle a cache line
		for(; alignedAddr.lessThan(topAddr); alignedAddr = alignedAddr.advance(super.blockSize)){
			boolean inCache = false;
			Collection<Block> blocks = getBlocks(alignedAddr); 
			for(Block b : blocks){ // loop through the relevant blocks, ie get the set of blocks in which the address can possibly be found
				if(!b.isValid()) // no piont checking an invalid/empty block
					continue;

				else if(b.getAddress().equals(alignedAddr)){ // we have a block that contains the address we are looking for
					super.hits++;
					inCache = true;
					super.writeHit(writer, addr, size);
					if(!silent)
						super.printHit(addr, size);
					crm.refresh(blocks, b); // Some replacement strategies (eg LRU) are affected by cache hits, so we must handle that
					ca.hit();
					break;
				}
			}

			if(!inCache){ // not in cache? That means we have a miss
				super.misses++;
				super.writeMiss(writer, addr, size);
				if(!silent)
					super.printMiss(addr, size);

				// update cache and check for evicted block
				Block evicted = crm.updateCache(alignedAddr, getBlocks(addr));
				if(evicted != null) // did we evict a block from the cache?
					super.writeEvicted(writer, alignedAddr, evicted);

				// check if we have a hit on the next level cache
				if(nextLevel != null){
					ca.increaseLevel();
					nextLevel.read(alignedAddr, (short)1, writer, silent, ca); //can the block be found in the next cache level?
				}else{
					ca.miss();
				}
			}
			accesses++;
			reads++;
		}
		ca.decreaseLevel();
	}


	public void write(Address addr, short size, BufferedWriter writer, boolean silent){
		Address alignedAddr = addr.aligned(super.blockSize);
		Address topAddr = addr.advance(size);
		Collection<Block> blocks = getBlocks(alignedAddr);
		while(alignedAddr.lessThan(topAddr)){
			Block evicted = crm.updateCache(alignedAddr, blocks);
			if(evicted != null){ // did we evict a block from cache?
				super.writeEvicted(writer, alignedAddr, evicted);
				if(!silent)
					super.printEvicted(alignedAddr, evicted);
			}
			alignedAddr = alignedAddr.advance(super.blockSize);
			accesses++;
		}
	}

	private Collection<Block> getBlocks(Address alignedAddr){
		Integer set = (int) ((alignedAddr.getValue() / blockSize) % nSets);
		return cache.get(set);
	}

	/**
	* Returns the total number of hits that have been recorded by the cache
	*/ 
    public int getHits(){
    	return super.hits;
    }

	/**
	* Returns the total number of misses that have been recorded by the cache
	*/ 
    public int getMisses(){
    	return super.misses;
    }
}