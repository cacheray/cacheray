package types;

import java.util.*;

public class StructType {
    private final String name;
    private final long size;
    private final List<StructMember> memberList;
    private Map<Long, Long> reorderMap;
    private int hitCount;
    private boolean accessed;
    private boolean rewritten;

    public final static StructType PLACEHOLDER = new StructType("PlaceholderStruct");

    private StructType(String name) {
        this.name = name;
        memberList = new ArrayList<>();
        size = 0;
        hitCount = 0;
        reorderMap = new HashMap<>();
    }

    public StructType(String name, long size, List<StructMember> memberList) {
        this.name = name;
        this.memberList = memberList;
        this.size = size;
        reorderMap = new HashMap<>();
        this.rewritten = false;
    }

    public String getName() {
        return name;
    }

    public List<StructMember> getMemberList(){
        return memberList;
    }

    public long getSize() {
        return size;
    }

    public boolean isAccessed() {
        return accessed;
    }

    public void reorderMemberList(Map<String, Long> reMap) {
        for (StructMember structMember : memberList) {
            Long newOffset = reMap.get(structMember.getMemberName());
            if (newOffset != null) {
                // add to rewrite mapping
                reorderMap.put(structMember.getOffset(), newOffset);
                // rewrite
                structMember.rewriteOffset(newOffset);
                rewritten = true;
            } else {
                System.err.println("ERROR: Remap list contained bad member");
            }
        }


        // reorder the actual list
        memberList.sort((structMember, t1) -> (int)((int)structMember.getOffset() - t1.getOffset()));
    }

/*
    public long recordAccess(long offset, int size) {
        //TODO: fix this
        if (hasRewrite()) {
            // the struct has been rewritten.
            // use the rewritten table.

        } else {
            // figure out which members are affected
            for (var mem : memberList) {
                long memOffset = mem.getOffset();
                int memSize = mem.getSize();
                if (memOffset == offset) {
                    mem.hit(); // TODO: make this better
                }
            }
            return offset; // return the offset entered
        }
        return offset;
    }   */

    public void printStats(String format, String indent){
        System.out.println(name + "{");
        for(StructMember sm : memberList){
            sm.printStats(format, indent);
            System.out.println("");
            System.out.flush();

        }
        System.out.println("}");
        System.out.println("Total hits for " + name + ": " + hitCount);
    }

    public long recordAccess(long offset, int size, CacheAccess ca) {
        // change offset if there is a reorder
        long accessOffset = reorderMap.getOrDefault(offset,offset);
        //if (newOffset != offset) System.out.println("changed offset!");
        // figure out which members are affected
        long endOfAccess = accessOffset + size - 1;
        boolean isTouched = false;
        for (var structMember : memberList) {
            long memberOffset = structMember.getOffset();
            long endOfMember = memberOffset + structMember.getSize();
            // Following constitues an access:
            // 1. accessOffset >= memberOffset, accessOffset < endOfMember
            // 2. endOfAccess >= memberOffset, endOfAccess < endOfMember
            if ((accessOffset >= memberOffset && accessOffset < endOfMember) || (endOfAccess >= memberOffset && endOfAccess < endOfMember)) {
                //System.out.println("found the corresponding struct member");
                isTouched = true;
                structMember.hit(ca); // TODO: make this better
            }
        }
        if (isTouched) hitCount++;
        accessed = true;
        return accessOffset;
    }

    public long getNewOffset(long offset) {
        return reorderMap.getOrDefault(offset, offset);
    }

    private boolean hasRewrite() {
        return rewritten;
    }

    public int getHitCount() {
        return hitCount;
    }
}
