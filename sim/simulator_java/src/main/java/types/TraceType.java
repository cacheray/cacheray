package types;

public class TraceType {
    public static final byte READ = 0;
    public static final byte WRITE = 1;
    public static final byte RTTA_ADD = 2;
    public static final byte RTTA_REMOVE = 3;
    public static final byte ATOMIC = (byte)(1 << 6);
    public static final byte UNALIGNED = (byte)(1 << 7);

    public static boolean isAtomic(byte b) {
        return (b & ATOMIC) == ATOMIC;
    }

    public static boolean isUnaligned(byte b) {
        return (b & UNALIGNED) == UNALIGNED;
    }

    public static byte strip(byte b) {
        return (byte)((~(ATOMIC | UNALIGNED)) & b);
    }

    public static String getName(byte type) {
        StringBuilder sb = new StringBuilder();
        if(isAtomic(type)) {
            sb.append("ATOMIC ");
        }
        if(isUnaligned(type)) {
            sb.append("UNALIGNED");
        }
        switch(strip(type)) {
            case READ:
                sb.append("READ");
                break;
            case WRITE:
                sb.append("WRITE");
                break;
            case RTTA_ADD:
                sb.append("RTTA_ADD");
                break;
            case RTTA_REMOVE:
                sb.append("RTTA_REMOVE");
                break;
            default:
                sb.append("UNKNOWN_TYPE");
                break;
        }

        return sb.toString();
    }
}
