package types;

public class CacheBlock {
    private long tag;
    private boolean valid;

    public CacheBlock(long tag, boolean valid) {
        this.tag = tag;
        this.valid = valid;
    }

    public long getTag() {
        return tag;
    }

    public boolean isValid() {
        return valid;
    }
}
