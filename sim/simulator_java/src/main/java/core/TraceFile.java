package core;

import types.Address;
import types.TraceType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The DataField class is a simple helper class that wraps byte dat to
 * make it easier to read the trace file
 */
public class TraceFile implements Iterable<TraceEvent>{
    private static final long S_EVENT = 24;
    private static final short CURRENT_VERSION = 1000;

    /*
    private final byte[] byteArray;
    private final int dataLength;
    private final List<TraceEvent> events;

     private int head;

    public int version;
    public short out_of_memory;
    public long n_events;
    public short type_len;
    public short addr_len;
    public short struct_pos_len;
    public short struct_size_len;
    public short struct_id_len;
    */

    // new stuff
    private final FileInputStream fileInputStream;

    /**
     * Constructor for the DataField class
     *
     * @param traceFileStream - contains the byte data that the DataField will wrap
     */

    public TraceFile(FileInputStream traceFileStream) {
        this.fileInputStream = traceFileStream;
    }

    @Override
    public Iterator<TraceEvent> iterator() {
        return new Iterator<TraceEvent>() {
            @Override
            public boolean hasNext() {
                return hasEvent();
            }

            @Override
            public TraceEvent next() {
                return createTraceEvent();
            }
        };
    }

    public boolean hasEvent() {
        try {
            return !(fileInputStream.available() == 0);
        } catch (IOException e) {
            return false;
        }
    }

    private TraceEvent createTraceEvent() {
        try {
            byte type = (byte)fileInputStream.read();
            //System.out.println("Type was " + type);
            byte strippedType = TraceType.strip(type);
            boolean isAtomic = TraceType.isAtomic(type);
            boolean isUnaligned = TraceType.isUnaligned(type);
            if (strippedType == TraceType.RTTA_ADD) {
                //System.out.println("Saw an rtta_add in decode...");
                //System.out.println("Created struct event");
                /*typedef struct __attribute__ ((__packed__)) cacheray_rtta_add {
                    cacheray_event_t type;
                    void *addr;
                    unsigned long threadid;
                    unsigned elem_size;
                    unsigned elem_count;
                    unsigned typename_len;
                    char typename[256];
                } cacheray_rtta_add_t;*/
                byte[] rawAddress = new byte[8];
                if (fileInputStream.read(rawAddress) != rawAddress.length) throw new IOException();
                byte[] rawThreadId = new byte[8];
                if (fileInputStream.read(rawThreadId) != rawThreadId.length) throw new IOException();
                byte[] rawElemSize = new byte[4];
                if (fileInputStream.read(rawElemSize) != rawElemSize.length) throw new IOException();
                byte[] rawElemCount = new byte[4];
                if (fileInputStream.read(rawElemCount) != rawElemCount.length) throw new IOException();
                byte[] rawTypenameLen = new byte[4];
                if (fileInputStream.read(rawTypenameLen) != rawTypenameLen.length) throw new IOException();
                int typenameLen = ByteBuffer.wrap(rawTypenameLen).order(ByteOrder.LITTLE_ENDIAN).getInt();
                byte[] rawStructName = new byte[typenameLen];
                if (fileInputStream.read(rawStructName) != rawStructName.length)throw new IOException();

                Address address = new Address(ByteBuffer.wrap(rawAddress).order(ByteOrder.LITTLE_ENDIAN).getLong());
                int elemSize = ByteBuffer.wrap(rawElemSize).order(ByteOrder.LITTLE_ENDIAN).getInt();
                int elemCount = ByteBuffer.wrap(rawElemCount).order(ByteOrder.LITTLE_ENDIAN).getInt();
                long threadId = ByteBuffer.wrap(rawThreadId).order(ByteOrder.LITTLE_ENDIAN).getLong();
                String structName = new String(rawStructName, StandardCharsets.UTF_8).strip(); // KILL IT WITH FIRE: new String(rawStructName, StandardCharsets.UTF_8).strip().substring("struct.".length());
                return new MallocEvent(address,elemSize,elemCount,structName, threadId);
            } else if(strippedType == TraceType.RTTA_REMOVE){
            /*
            typedef struct __attribute__ ((__packed__)) cacheray_rtta_remove {
                cacheray_event_t type;
                void *addr;
                unsigned long threadid;
            } cacheray_rtta_remove_t;
             */

                byte[] rawAddress = new byte[8];
                if (fileInputStream.read(rawAddress) != rawAddress.length) throw new IOException();
                byte[] rawThreadId = new byte[8];
                if (fileInputStream.read(rawThreadId) != rawThreadId.length) throw new IOException();

                Address address = new Address(ByteBuffer.wrap(rawAddress).order(ByteOrder.LITTLE_ENDIAN).getLong());
                long threadId = ByteBuffer.wrap(rawThreadId).order(ByteOrder.LITTLE_ENDIAN).getLong();
                return new FreeEvent(address, threadId);
            } else {
                // Init som stuff
                byte[] rawAddress = new byte[8];
                if (fileInputStream.read(rawAddress) != rawAddress.length)throw new IOException();
                byte[] rawVarSize = new byte[1];
                if (fileInputStream.read(rawVarSize) != rawVarSize.length)throw new IOException();
                byte[] rawThreadId = new byte[8];
                if (fileInputStream.read(rawThreadId) != rawThreadId.length)throw new IOException();

                // Get info
                Address address = new Address(ByteBuffer.wrap(rawAddress).order(ByteOrder.LITTLE_ENDIAN).getLong());
                byte varSize = rawVarSize[0];
                long threadId = ByteBuffer.wrap(rawThreadId).order(ByteOrder.LITTLE_ENDIAN).getLong();
                return new MemEvent(strippedType, address, rawVarSize[0], threadId);
            }
        } catch (IOException e) {
            System.err.println("ERROR: trace file parse error");
            System.exit(-1);
        }
        return null;
    }
}
