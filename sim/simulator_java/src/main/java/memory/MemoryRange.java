package memory;

import types.Address;

public abstract class MemoryRange {
    protected Address startAddress;
    protected Address endAddress;
    protected String name;

    public MemoryRange(Address startAddress, Address endAddress, String name) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.name = name;
    }

    public abstract boolean contains(Address address);
}
