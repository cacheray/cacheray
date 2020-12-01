package cache;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import types.Address;
import types.Block;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;
import java.util.HashSet;

import java.io.BufferedWriter;
import java.io.IOException;

public class CacheReplacementManager{

	public static final short LRU = 0;
	public static final short FIFO = 1; 
	public static final short LIFO = 2;
	public static final short FILO = 3;
	public static final short RANDOM = 4;

	private short rp;
	private int nCacheBlocks;
	private int blockSize;

	//////////////////////////////////////////////////////////
	//														//
	//	Depending on what kind of replacement strategy we 	// 
	//	we are using for our cache, different Collections 	//
	//	will basically do the work for us. Otherwise we 	//
	//	to manually track which block is next in line for 	//
	//	replacement. 										//
	//														//
	//////////////////////////////////////////////////////////

	public CacheReplacementManager(short replacementPolicy){
		rp = replacementPolicy;
	}

	/**
	* This method is called by the SimpleCache implementations to get the appropriate collection to represent the cache memory.
	* This method does NOT initiate the blocks themselves, it merely returns the list type. It is up to the SimpleCache to initiate the blocks. 
	* The parameters are passed for later use, and aren't used for this method call itself.
	*
	* @param nCacheBlocks - the number of blocks that should fit into memory
	* @param blockSize - the size (in bytes) of each block
	*
	* @return - returns a collection that is to represent the cache
	*/
	public Collection<Block> initBlocks(int nCacheBlocks, int blockSize, int assoc){
		this.nCacheBlocks = nCacheBlocks;
		this.blockSize = blockSize;
		switch(rp){
			case FIFO:
			case LRU:
				var list = new LinkedList<Block>();
				for (int i = 0; i < assoc; i++) list.add(new Block(-1, Address.NULL_ADDRESS, false));
				return list;
			case LIFO:
				return new Stack<Block>();
			case FILO:
				break;
			case RANDOM: //lol
				return new HashSet<Block>();
		}
		return null;
	}

	/**
	* This method is called by the SimpleCache whenever there is an operation that updates the cache, such as a read operation that causes a miss.
	*
	* @param alignedAddr - the first address in the block that is to be updated. Since the entire block will be updated, the offset does not matter
	* @param cache - the collection of blocks that represent the cache
	*/
	public Block updateCache(Address alignedAddr, Collection<Block> cache){
		// the way the update is done depends on what collection is used, which in turn depends on the replacement policy.
		switch(rp){
			case LRU:
				if(cache.size() < nCacheBlocks){ // easy, we just add the block first in the list
					((LinkedList<Block>)cache).addFirst(new Block(blockSize, alignedAddr, true));
					return null;
				}
				else{ // the least recently used block will be at the end of the list
					Block b = ((LinkedList<Block>)cache).removeLast();
					Block evicted = new Block(blockSize, b.getAddress(), false); // return this block so we can write out what was evicted from the cache
					b.update(alignedAddr, true); 
					((LinkedList<Block>)cache).addFirst(b);
					return evicted;
				}
			case LIFO:
				if(cache.size() < nCacheBlocks){
					((Stack<Block>)cache).push(new Block(blockSize, alignedAddr, true));
					return null;
				}
				else{
					Block b = ((Stack<Block>)cache).pop();
					Block evicted = new Block(blockSize, b.getAddress(), false);
					b.update(alignedAddr, true);
					((LinkedList<Block>)cache).push(b);
					return evicted;
				}
			case FILO:
				return null;
			case FIFO:
				if(cache.size() < nCacheBlocks){
					((LinkedList<Block>)cache).addFirst(new Block(blockSize, alignedAddr, true));
					return null;					
				}else{
					Block b = ((LinkedList<Block>)cache).removeLast();
					Block evicted = new Block(blockSize, b.getAddress(), false);
					b.update(alignedAddr, true); 
					((LinkedList<Block>)cache).addFirst(b);
					return evicted;
				}
				//return null;
			case RANDOM: //lol
				return null;
			default:
				return null;

		}
	}

	public void refresh(Collection<Block> cache, Block b){
		switch(rp){
			case LRU:
				((LinkedList<Block>)cache).remove(b);
				((LinkedList<Block>)cache).addFirst(b);
			case FILO: // With this replacement strategy we only care about the order in which blocks were added, bothing to refresh
				break;
			case FIFO: // With this replacement strategy we only care about the order in which blocks were added, bothing to refresh
				break;
			case RANDOM: 
				break;
			default:
				break;
		}
	}
}