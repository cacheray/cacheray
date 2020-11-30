package cache;

import types.Address;
import types.Block;
import types.CacheAccess;

import java.io.BufferedWriter;
import java.io.IOException;

import java.math.BigInteger;

import java.util.Collection;
import java.util.ArrayList;

public class DirectMappedCache extends SimpleCache{
	private int nCacheBlocks;
	private Collection<Block> cache;

	// NOTE direct mapped cache doesn't really have a replacement strategy, hence no CacheReplacementManager
	public DirectMappedCache(String name, int cacheSize, int blockSize, short writePolicy, short replacementPolicy, SimpleCache nextLevel){
		super(name, cacheSize, blockSize, writePolicy, replacementPolicy, nextLevel);
		nCacheBlocks = super.cacheSize / super.blockSize;

		cache = new ArrayList<Block>();
		for(int i = 0; i < nCacheBlocks; i++)
			cache.add(new Block(super.blockSize, Address.NULL_ADDRESS, true));
	}

	/**
	* This method simulates a read, if the address is in cache memory it returns true, otherwise it updates the cache and returns false
	*
	* @param addr - the addr that the read starts at
	* @param size - the size (in bytes) of the read
	*
	* @return - returns an int that represents a hit or a miss. 
	* 			If there is a hit the number corresponds to the cache level in which the cache occured (ie a hit in L1 cahce returns 1, in L3 cahce returns 3, etc)
	*			A miss results in a (large) negative number being returned. 
	*			Technically this will give false positives on misses if a sufficiently large number of caches are used, however this is unlikely to become an issue.
	*/
	public void read(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca){
		Address alignedAddr = addr.aligned(super.blockSize); // This address represents the start of the block we're reading from, which is what we are really interested in
		Address topAddr = addr.advance(size); // keep track of the top address, so we dont read to far
		ArrayList<Block> blocks = getBlocks(addr, size);  // get the blocks currently in cache, which could be the blocks we're looking for

		if(alignedAddr.advance(blockSize).lessThan(topAddr))
			ca.setStradle(true); // TODO what if we stradle multpiple cachelines!?

		// loop over the relevant blocks currently in the cache and check if they hold the addresses we want to read from
		for(int i = 0; i < blocks.size() && alignedAddr.lessThan(topAddr); i++){

			// hit
			if(blocks.get(i).getAddress().equals(alignedAddr)){ 
				super.writeHit(writer, addr, size);// write hit to output file
				if(!silent) // should we print to stdout?
					super.printHit(addr, size);				
				super.hits++;
				ca.hit();
			}

			// miss
			else{ 
				super.misses++;
				super.writeMiss(writer, addr, size); // write hit to output file
				if(!silent) // should we print to stdout?
					super.printMiss(addr, size);

				if(!blocks.get(i).getAddress().equals(Address.NULL_ADDRESS)){ // check if the miss was due to empty block or wrong contents in block
					// miss was due to wrong contents in block, so evict and replace
					super.writeEvicted(writer, alignedAddr, blocks.get(i));
					if(!silent)
						super.printEvicted(addr, blocks.get(i));
				}
				blocks.get(i).update(alignedAddr, true); // update the block to contain the data just read. This looks the same regardless of if we evicted or not.
				if(nextLevel != null) {
					ca.increaseLevel();
					nextLevel.read(alignedAddr, (short)1, writer, silent, ca); //can the block be found in the next cache level?
				}else{
					ca.miss();
				}
			}

			// Check the next block in "blocks", which will be blockSize addresses after the current block
			alignedAddr = alignedAddr.advance(super.blockSize);
			accesses++;
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
		ArrayList<Block> blocks = getBlocks(addr, size);
		Address alignedAddr = addr.aligned(blockSize);
		Address topAddr = addr.aligned(size);
		for(int i = 0; i < blocks.size() && alignedAddr.lessThan(topAddr); i++){
			blocks.get(i).update(alignedAddr, true);
			accesses++;
		}
	}

	/**
	* A method used to get the relevant blocks to see wheter or not there was a cache hit. Allows us to avoid looking through the entire cache to check for a hit
	*
	* @param addr - the addr that the read starts at
	* @param size - the size (in bytes) of the read
	*
	* @return - returns an ArrayList of blocks that are relevant given the address and size
	*/
	private ArrayList<Block> getBlocks(Address addr, short size){
		ArrayList<Block> blocks = new ArrayList<Block>();
		Address alignedAddr = addr.aligned(super.blockSize);
		Address topAddr = addr.advance(size);
                // System.out.println("nCacheBlocks=" + nCacheBlocks);
                // System.out.println("blockSize=" + blockSize);
                // System.out.println("alignedAddr=" + alignedAddr.getValue().toString());
                // System.out.println("topAddr=" + topAddr.getValue().toString());
		do{
                    BigInteger bigAddr = alignedAddr.getBigIntValue();
                    BigInteger bigBlockSize = BigInteger.valueOf(super.blockSize);
                    BigInteger bigCacheBlocks = BigInteger.valueOf(nCacheBlocks);

                    int cacheSlot = bigAddr.divide(bigBlockSize).mod(bigCacheBlocks).intValue();
                    blocks.add(((ArrayList<Block>)cache).get(cacheSlot));
                    alignedAddr = alignedAddr.advance(blockSize);
		}while(alignedAddr.lessThan(topAddr));

		return blocks;
	}

	/**
	* @return - Returns the total number of hits that have been recorded by the cache
	*/ 
    public int getHits() {
        return super.hits;
    }

	/**
	* @return - Returns the total number of misses that have been recorded by the cache
	*/ 
    public int getMisses() {
        return super.misses;
    }
}
