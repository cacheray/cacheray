package cache;

import types.Address;
import types.Block;
import types.CacheAccess;
import types.CacheBlock;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class OmniCache extends SimpleCache {
    private final List<List<CacheBlock>> sets;
    private final Random random;

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
        sets = new ArrayList<>();
        for (int i = 0; i < nSets; i++) {
            LinkedList<CacheBlock> set = new LinkedList<>();
            for (int j = 0; j < associativity; j++) {
                set.add(new CacheBlock(-1, false));
            }
            sets.add(set);
        }
        random = new Random();

        assert(this.blockSize % 8 == 0);
        System.out.println("Created cache " + name +" with block size " + this.blockSize + " sets " + nSets + " n_blocks " + n_blocks + " assoc " + this.associativity);
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
        System.out.println("Index " + index + ", tag " + tag +" read");

        List<CacheBlock> set = sets.get((int)index / associativity);
        for (CacheBlock b : set) {
            if (b == null) {
                // nothin here
                continue;
            }

            if (b.getTag() == (int)tag) {
                // correct! we have a hit!
                super.hits++;
                super.writeHit(writer, addr, size);
                ca.hit();
                return;
            }
        }
        // Didn't find it. Lets fetch from lower level
        ca.increaseLevel();

        readNextLevel(addr,size,writer,silent,ca);
        misses++;

        // Now we have ned block, lets place it
        // yeet random block lol
        int blocksLength = set.size();
        sets.get((int)index / associativity).set(random.nextInt(blocksLength), new CacheBlock(tag,true));
        ca.decreaseLevel();
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
        List<CacheBlock> set = sets.get((int)index / associativity);
        int blocksLength = set.size();
        sets.get((int)index / associativity).set(random.nextInt(blocksLength), new CacheBlock(tag,true));
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
