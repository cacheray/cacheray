package cache;

import types.Address;
import types.Block;
import types.CacheAccess;
import types.CacheBlock;

import java.io.BufferedWriter;
import java.util.Random;

public class OmniCache extends SimpleCache {
    private final CacheBlock[][] sets;
    private Random random;

    public OmniCache(String name, int cacheSize, int blockSize, int associativity, short writePolicy, short replacementPolicy, SimpleCache nextLevel) {
        super(name, cacheSize, blockSize, writePolicy, replacementPolicy, nextLevel);
        int n_blocks = this.cacheSize / this.blockSize;
        assert(this.cacheSize % this.blockSize == 0);
        int nSets;
        if (associativity != 0) {
            nSets = n_blocks / associativity;
        } else {
            nSets = 1;
            associativity = n_blocks;
        }
        this.associativity = (short)associativity;
        sets = new CacheBlock[associativity][nSets];
        random = new Random();
    }

    @Override
    public void read(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca) {
        // 1. See which set it belongs too. Can be done by mod'ing the address
        long[] addressable = addr.getTIO(cacheSize / blockSize, blockSize);
        long offset = addressable[2];
        long index = addressable[1];
        long tag = addressable[0];
        accesses++;
        reads++;

        CacheBlock[] set = sets[(int)index % associativity];
        for (CacheBlock b : set) {
            if (b == null) {
                // nothin here
                continue;
            }

            if (b.getTag() == (int)tag) {
                // correct! we have a hit!
                hits++;
                return;
            }
        }
        // Didn't find it. Lets fetch from lower level
        ca.decreaseLevel();
        misses++;
        readNextLevel(addr,size,writer,silent,ca);
        ca.increaseLevel();

        // Now we have ned block, lets place it
        // yeet random block lol
        int blocksLength = set.length;
        sets[(int)index % associativity][random.nextInt(blocksLength)] = new CacheBlock(tag,true);
    }

    private void readNextLevel(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca){
        if (nextLevel == null) {
            ca.miss();
        } else {
            nextLevel.read(addr,size,writer,silent,ca);
        }
    }

    @Override
    public void write(Address addr, short size, BufferedWriter writer, boolean silent) {
        long[] addressable = addr.getTIO(cacheSize / blockSize, blockSize);
        long offset = addressable[2];
        long index = addressable[1];
        long tag = addressable[0];
        accesses++;
        reads++;
        CacheBlock[] set = sets[(int)index % associativity];
        int blocksLength = set.length;
        sets[(int)index % associativity][random.nextInt(blocksLength)] = new CacheBlock(tag,true);
    }

    @Override
    public int getHits() {
        return hits;
    }

    @Override
    public int getMisses() {
        return misses;
    }
}
