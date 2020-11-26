package cache;

import types.Address;
import types.Block;
import types.CacheAccess;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;

public class FullyAssociativeCache extends SimpleCache{
	private int nCacheBlocks;
	private Collection<Block> cache;
	private CacheReplacementManager crm;

	public FullyAssociativeCache(String name, int cacheSize, int blockSize, short writePolicy, short replacementPolicy, SimpleCache nextLevel){
		super(name, cacheSize, blockSize, writePolicy, replacementPolicy, nextLevel);
		nCacheBlocks = super.cacheSize / super.blockSize;
		crm = new CacheReplacementManager(replacementPolicy);
		cache = crm.initBlocks(nCacheBlocks, blockSize);
	}

	/**
	* This method simulates a read, if the address is in cache memory it returns true, otherwise it updates the cache and returns false
	*
	* @param addr - the addr that the read starts at
	* @param size - the size (in bytes) of the read
	*/
	public void read(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca){
		Address alignedAddr = addr.aligned(super.blockSize);
		Address topAddr = addr.advance(size);

		if(alignedAddr.advance(blockSize).lessThan(topAddr))
			ca.setStradle(true);

		// For loop, in case we strasdle a cache line on the read
		for(; alignedAddr.lessThan(topAddr); alignedAddr = alignedAddr.advance(super.blockSize)){

			// loop through all blocks and check if any block holds the address we are looking for
			boolean inCache = false; // if the address isn't in cache we have a cache miss. TODO: find a better way of handling this
			for(Block b : cache){
				if(!b.isValid()) // no point checking invalid/empty blocks TODO: implements invalidation of blocks
					continue;

				else if(b.getAddress().equals(alignedAddr)){
					super.hits++;
					inCache = true; 
					super.writeHit(writer, addr, size);				
					if(!silent)
						super.printHit(addr, size);
					crm.refresh(cache, b); // Some replacement strategies (eg LRU) are affected by cache hits, so we must handle that
					ca.hit();
					break; // break loop, we already found the block we're looking for
				}
			}

			if(!inCache){ // did we find the block containing the address we are looking for in cache?
				super.misses++;
				super.writeMiss(writer, addr, size);
				if(!silent)
					super.printMiss(addr, size);

				// update the cache and check if any block was evicted
				Block evicted = crm.updateCache(alignedAddr, cache);
				if(evicted != null){
					super.writeEvicted(writer, alignedAddr, evicted);
					if(!silent)
						super.printEvicted(alignedAddr, evicted);
				}	

				// check next level cache
				if(nextLevel != null){
					ca.increaseLevel();
					nextLevel.read(alignedAddr, (short)1, writer, silent, ca); //can the block be found in the next cache level?
				}else{
					ca.miss();
				}
			}
		}
		ca.decreaseLevel();
	}

	/**
	* This method simulates a read, the cache is updated to contain what was just weitten
	*
	* @param addr - the addr that the write starts at
	* @param size - the size (in bytes) of the write
	*/
	public void write(Address addr, short size, BufferedWriter writer, boolean silent){
		Address alignedAddr = addr.aligned(super.blockSize);
		Address topAddr = addr.advance(size);
		while(alignedAddr.lessThan(topAddr)){
			Block evicted = crm.updateCache(alignedAddr, cache);
			if(evicted != null){ // did we evict a block from cache?
				super.writeEvicted(writer, alignedAddr, evicted);
				if(!silent)
					super.printEvicted(alignedAddr, evicted);
			}
			alignedAddr = alignedAddr.advance(super.blockSize);
		}
	}

	/**
	* Returns the total number of hits that have been recorded by the cache
	*/ 
	public int getHits() {
        return super.hits;
    }

	/**
	* Returns the total number of misses that have been recorded by the cache
	*/ 
    public int getMisses() {
        return super.misses;
    }
}