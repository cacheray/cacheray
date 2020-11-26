package core;

import types.Address;
import types.Struct;
import types.TraceType;
import types.TypeData;

import java.util.LinkedList;
import java.util.List;

public class MallocEvent extends TraceEvent{
    private final Address addr;
    private final int structSize;
    private final int elemCount;
    private final String structName;

    public MallocEvent(Address addr, int structSize, int elemCount, String structName, long threadId) {
        super(TraceType.RTTA_ADD, threadId);
        this.addr = addr;
        this.structSize = structSize;
        this.elemCount = elemCount;
        this.structName = structName;
        //System.out.println("Created MallocEvent: " + structName);
    }

    public Address getAddr() {
        return addr;
    }

    public int getStructSize() {
        return structSize;
    }

    public String getStructName() {
        return structName;
    }

    public int getElemCount() {
        return elemCount;
    }

    public List<Address> createStructs() {
        List<Address> addressList = new LinkedList<>();
        addressList.add(addr);
        for (int i = 1; i < elemCount; i++) {
            addressList.add(addr.advance(i*structSize));
        }
        return addressList;
    }

    @Override
    public String getEventAsString() {
        return "CPU: " + threadId + "\tEVENT: " + TraceType.getName(type) + "\tAddress: " + addr.toString() + "\tELEMSIZE: " + structSize
                + "\tELEMCOUNT: " + elemCount + "\tTYPENAME: " + structName;
    }
}
