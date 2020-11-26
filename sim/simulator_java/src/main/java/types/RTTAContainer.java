package types;

import java.util.*;

public class RTTAContainer {
    private final List<RTTA> rtta_list;
    private final Set<StructType> structTypeSet;
    //private final List<StructAccess> structAccessList;
    //private final Map<StructType, List<StructAccess>> structAccessMap;

    // TODO: might be built into SimpleSim instead
    public RTTAContainer() {
        rtta_list = new ArrayList<>();
        //structAccessList = new ArrayList<>();
        //structAccessMap = new HashMap<>();
        structTypeSet = new HashSet<>();
    }

    public Address access(Address address, int size, CacheAccess ca) {
        Address changedAddress = address;
        for (RTTA s : rtta_list) {
            if (s.contains(address)) {
                changedAddress = s.access(address, size, ca);
            }
        }
        return changedAddress;
    }

    public Address getChangedAddress(Address address) {
        for (RTTA s : rtta_list) {
            if (s.contains(address)) {
                return s.getNewAddress(address);
            }
        }
        return address;
    }

    public void addRTTA(RTTA rtta) {
        rtta_list.add(rtta);
        structTypeSet.add(rtta.getStructType());
    }

    public String getStatsAsString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("BEGIN STRUCT STATS\n");

        structTypeSet.forEach((StructType type) -> {
            stringBuilder.append(type.getName()).append(" {\n");
            for (var mem : type.getMemberList()) {
                stringBuilder.append("\t").append(mem.getMemberName()).append(": ").append(mem.getHits()).append("\n");
            }
            stringBuilder.append("}\n\n");
        });

        return stringBuilder.toString();
    }

    // A somewhat cryptic print function that prints statistics over struct accesses
    public void printStats(int nCaches){
        System.out.println("STRUCT ACCESS STATISTICS FOR " + nCaches + " CACHES");
        System.out.println("==============================================");
        System.out.println("RTTAs present: " + rtta_list.size());
        System.out.println("Structs present: " + rtta_list.stream().mapToLong(RTTA::getNumberOfUnits).sum());
        System.out.println("Struct types present: " + structTypeSet.size());
        System.out.println("Struct accesses: " + structTypeSet.stream().mapToInt(StructType::getHitCount).sum());
        System.out.println("");
        String format = "%-20s %-16s %-16s";
        String indent = "%-10s";
        System.out.printf(format, "Struct Name","Accesses","Full Misses");
        for(int i = 0; i < nCaches; i++)
            System.out.printf(indent,"L"+(i+1));

        System.out.println();
        for(StructType st : structTypeSet){
            /*if (!st.isAccessed()){
                continue;
            }*/
            st.printStats(format, indent);
            System.out.println("");
        }
        System.out.println("==============================================\n");
    }

/*
    public String getFormattedStats() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("BEGIN STRUCT STATS\n");
        for (Map.Entry<StructType, List<StructAccess>> entry : structAccessMap.entrySet()){
            int accesses = 0;
            stringBuilder.append("StructType: ").append(entry.getKey().getName()).append("\n");
            for (StructAccess structAccess : entry.getValue()) {
                // Obviously, more should be done here.
                // TODO: do more collecting of stats. member accesses?
                accesses++;
            }
            stringBuilder.append("\tAccesses: ").append(accesses).append("\n");
        }
        stringBuilder.append("END STRUCT STATS\n");
        return stringBuilder.toString();
    }
*/
    /**
     * Dont use! Bad form. Marking it deprecated so I remember to change it.
     * @return structs
     * @deprecated
     */
    public List<RTTA> getRTTAList() {
        return rtta_list;
    }

    public int size() {
        return rtta_list.size();
    }
}
