package types;

/**
 * Data structure containing information about the types and structures of the
 * program-under-testing. Useful for knowing where structures are located, how large
 * they are and which members they are comprised of.
 *
 * @author Hannes Åström, Wilhelm "Will" Lundström
 * @version 1
 */

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class TypeData {

    // Change if format has been adjusted
    private final static Integer VERSION_NBR = 4;

    private final TypeDataInternal data;
    private final Map<String, StructType> structMap;
    private final Map<String, String> typedefMap;

    public TypeData(String fileName, int nCaches) throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        Gson gson = new Gson();
        data = gson.fromJson(bufferedReader,TypeDataInternal.class);

        // Check version number is existent and correct
        Integer vNum = data.getVersion();
        if (vNum == null) {
            System.err.println("TypeData: Version number could not be found. Exiting.");
            System.exit(-1);
        }
        if (!vNum.equals(VERSION_NBR)) {
            System.err.println("TypeData: Version number is wrong. Are you running an old file? Exiting.");
            System.exit(-1);
        }

        // Generate struct map
        structMap = new HashMap<>();
        for (TypeDataInternal.StructInternal structData : data.getStructs()) {
            String structName = structData.getName();
            List<StructMember> memberList = new ArrayList<>();
            for (TypeDataInternal.StructInternal.StructMemberInternal smi : structData.getMembers()){
                int size = smi.getSize();
                long offset = smi.getOffset();
                memberList.add(new StructMember(smi.getName(), size, offset, nCaches));
            }
            long size = -1;
            if (!memberList.isEmpty()) {
                StructMember lastMember = memberList.get(memberList.size() - 1);
                size = lastMember.getSize() == -1 ? -1 : lastMember.getSize() + lastMember.getOffset();
            }
            StructType s = new StructType(structName, size, memberList);
            structMap.put(structName, s);
        }

        typedefMap = new HashMap<>();
        for (TypeDataInternal.TypedefsInternal typedefsInternal : data.getTypedefs()) {
            typedefMap.put(typedefsInternal.getType(), typedefsInternal.getDef());
        }
    }

    private TypeData() {
        // All empty
        data = new TypeDataInternal();
        structMap = new HashMap<>();
        typedefMap = new HashMap<>();
    }

    public static TypeData makeEmptyTypeData() {
        return new TypeData();
    }

    public TypeDataInternal getInternal() {
        return data;
    }

    public Map<String, StructType> getStructMap() {
        return structMap;
    }

    public List<StructType> getStructList() {
        return new ArrayList<>(structMap.values());
    }

    public StructType getStructByName(String name) {
        // Check if prefixed by "struct."
        // This is needed since MallocTracker adds this at the start
        System.out.println("Found struct called " + name);
        if (name.startsWith("struct.")) {
            return getStructByName("struct " + name.substring("struct.".length()));
        }
        // Check typedefs
        String typedef = typedefMap.get(name);
        if (typedef != null) {
           // System.out.println("Got a typedef...");
            return structMap.getOrDefault(typedef, StructType.PLACEHOLDER);
        }
        return structMap.getOrDefault(name, StructType.PLACEHOLDER);
    }

    public static class TypeDataInternal {
        private Integer version;
        private List<StructInternal> structs;
        private List<UnionInternal> unions;
        private List<TypedefsInternal> typedefs;

        public Integer getVersion() {
            return version;
        }
        public List<StructInternal> getStructs() {
            return structs;
        }
        public List<UnionInternal> getUnions() {
            return unions;
        }
        public List<TypedefsInternal> getTypedefs() {
            return typedefs;
        }

        public static class StructInternal {
            private String name;
            private Long id;
            private List<StructMemberInternal> members;

            public String getName() {
                return name;
            }
            public List<StructMemberInternal> getMembers() {
                return members;
            }
            public static class StructMemberInternal {
                private Long id;
                private String name;
                private String memberName;
                private Long offset;
                private Integer size;

                public Long getType() {
                    return id;
                }
                public String getTypeName() {
                    return name;
                }
                public String getName() {
                    return memberName;
                }
                public Long getOffset() {
                    return offset;
                }
                public Integer getSize() {
                    return size;
                }
            }
        }

        public static class UnionInternal {
            private String name;
            private Long id;

            public String getName() {
                return name;
            }

            public Long getId() {
                return id;
            }

            public List<UnionMemberInternal> getMembers() {
                return members;
            }

            private List<UnionMemberInternal> members;

            public static class UnionMemberInternal {
                private Long id;
                private String name;
                private String memberName;
                private Long offset;

                public Long getId() {
                    return id;
                }

                public String getName() {
                    return name;
                }

                public String getMemberName() {
                    return memberName;
                }

                public Long getOffset() {
                    return offset;
                }

                public Long getSize() {
                    return size;
                }

                private Long size;
            }
        }

        public static class TypedefsInternal {
            private String type;
            private String def;

            public String getType() {
                return type;
            }

            public String getDef() {
                return def;
            }
        }
    }


}
