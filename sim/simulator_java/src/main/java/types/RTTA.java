package types;

import java.math.BigInteger;

public class RTTA {
    private final Address start;
    private final long length;
    private final long unit_size;
    private final long nmemb;
    private final StructType type;

    public RTTA(Address start, StructType type, long nmemb) {
        this.start = start;
        this.nmemb = nmemb;
        this.type = type;
        this.unit_size = this.type.getSize();
        this.length = this.unit_size * nmemb;
    }

    public boolean contains(Address address) {
        return this.start.containsAddress(address, length);
    }

    public Address access(Address address, int length, CacheAccess ca) {

        long offset =  address.mod(unit_size) - start.mod(unit_size);
        long newOffset = type.recordAccess(offset, length, ca);
        if (offset != newOffset) {
            // Reordered!
            //System.out.println("Reorder from offset " + offset + " to offset " + newOffset);
            return address.advance(newOffset - offset);
        }
        return address;
    }

    public Address getNewAddress(Address address) {
        long offset =  address.mod(unit_size) - start.mod(unit_size);
        long newOffset = type.getNewOffset(offset);
        if (offset != newOffset) {
            // Reordered!
            //System.out.println("Reorder from offset " + offset + " to offset " + newOffset);
            return address.advance(newOffset - offset);
        }
        return address;
    }

    public StructType getStructType() {
        return type;
    }

    public long getNumberOfUnits() {
        return nmemb;
    }
}
