package core;

import types.Address;
import types.TraceType;

public class MemEvent extends TraceEvent{
    private final Address addr;
    private final byte size;
    private final long threadId;

    public MemEvent(byte type, Address addr, byte size, long threadId) {
        super(type, threadId);
        this.addr = addr;
        this.size = size;
        this.threadId = threadId;
    }

    public Address getAddr() {
        return addr;
    }

    public long getThreadId() { return threadId; }

    public byte getSize() {
        return size;
    }

    public String getEventAsString() {
        return "CPU: " + threadId + "\tEVENT: " + TraceType.getName(type) + "\tAddress: " + addr.toString() + "\tSIZE: " + size;
    }
}
