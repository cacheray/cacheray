package core;

import types.Address;
import types.TraceType;

public class FreeEvent extends TraceEvent{
    private Address addr;
    public FreeEvent(Address addr, long threadId) {
        super(TraceType.RTTA_REMOVE, threadId);
        this.addr = addr;
    }

    @Override
    public String getEventAsString() {
        return "CPU: " + threadId + "\tEVENT: " + TraceType.getName(type) + "\tAddress: " + addr.toString();
    }
}
