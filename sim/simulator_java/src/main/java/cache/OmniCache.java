package cache;

import types.Address;
import types.CacheAccess;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class OmniCache extends SimpleCache {
    private final List<List<Address>> sets;

    public OmniCache(String name, int cacheSize, int blockSize, short writePolicy, short replacementPolicy, SimpleCache nextLevel) {
        super(name, cacheSize, blockSize, writePolicy, replacementPolicy, nextLevel);

        // Create sets
        int n_sets = this.cacheSize / (this.associativity * this.blockSize);
        sets = new ArrayList<>();
        for (int i = 0; i < n_sets; i++) {
            List<Address> temp = new ArrayList<>();
            for (int j = 0; j < this.associativity; j++) {
                temp.add(j, new Address());
            }
            sets.add(i, temp);
        }
    }

    @Override
    public void read(Address addr, short size, BufferedWriter writer, boolean silent, CacheAccess ca) {
        // 1. See which set it belongs too. Can be done by mod'ing the address
        long set = addr.mod(this.associativity);

        // 2. Is the address already in the set?
        //      If yes, register hit and return
        //boolean hit = false;
        List<Address> theSet = this.sets.get((int)set);
       // Address[] addressesAccessed = addr.getHitAddresses(size, blockSize);
        for (int index = 0; index < this.associativity; index++) {
            if (theSet.get(index).containsAddress(addr, this.blockSize)) {
                // Hit!
                ca.hit();
                this.hits += 1;
                return;
            }
        }

        // 3. Register miss and fetch from below
        ca.miss();
        this.misses += 1;


        // 4. Check with CacheRep which line should be replaced

        // 5. Return
    }

    @Override
    public void write(Address addr, short size, BufferedWriter writer, boolean silent) {

    }

    @Override
    public int getHits() {
        return 0;
    }

    @Override
    public int getMisses() {
        return 0;
    }
}
