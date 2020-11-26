package core;

import types.TraceType;

public abstract class TraceEvent {
    protected final byte type;
    protected final long threadId;

    public TraceEvent(byte type, long threadId) {
        this.type = type;
        this.threadId = threadId;
    }

    public byte getType(){
        return type;
    }

    public long getThreadId() { return threadId; }

    public abstract String getEventAsString();
}
