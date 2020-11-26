package types;

import org.apache.commons.lang3.StringUtils;

public class StructMember {

    private final String memberName;
    private final int size;
    private long offset;
    private long accesses;
    private long[] hits;
    private long misses;
    private long stradledCachelines;
    private int nCaches;
    private boolean isRewritten;

    public StructMember(String memberName, int size, long offset, int nCaches) {
        this.memberName = memberName;
        this.size = size;
        this.offset = offset;
        this.nCaches = nCaches;
        hits = new long[nCaches];
        accesses = 0;
        isRewritten = false;
    }

    public String getMemberName() {
        return memberName;
    }

    public long getHits() {
        return hits[0];
    }

    public long getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public void printStats(String format, String indent){
        System.out.printf(format, /* Hacky way of indenting structmembers */"   "+StringUtils.abbreviate(memberName,16) /*+ ":" +  getOffset()*/, accesses, misses);
        for(long h : hits)
            System.out.printf(indent,h);
    }

    public void rewriteOffset(long offset) {
        this.offset = offset;
        isRewritten = true;
    }

    public void hit(CacheAccess ca) {
        if (!ca.isRead()) {
            // TODO: really shoddy
            return;
        }
        accesses++;
        //ca.print();
        //System.out.println(nCaches);
        for(int i = 0; i < nCaches; i++){
            //System.out.println("hits in CA[i] " + ca.getHits(i));
            hits[i] += ca.getHits(i);
        }
        misses += ca.getMisses();
        if(ca.getStradle())
            stradledCachelines++;
    }
}
