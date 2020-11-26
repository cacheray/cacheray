import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import types.TypeData;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore("TypeData tests are disable since the format changed. Maybe fix this?")
public class TestTypeData {
    private TypeData typeData;

    private static final URL TYPE_DATA_FILE = TestTypeData.class.getResource("layoutTestTypeData.json");//"layoutTestTypeData.json";


    @Before
    public void setUp() throws FileNotFoundException {
        typeData = new TypeData(TYPE_DATA_FILE.toString().split(":")[1], 2); //TODO: NOT SAFE!
    }

    @After
    public void tearDown() {
        typeData = null;
    }

    @Test
    public void testBasic() {
        assertTrue(true);
    }

    @Test
    public void testDataStructure() {
        assertTrue("test amount of structs",typeData.getInternal().getStructs().size() == 1);
      //  assertTrue("test amount of types", typeData.getInternal().getTypes().size() == 2);
    }
/*
    @Test
    public void testTypesContents() {
        Map<Long, TypeData.TypeDataInternal.TypesInternal> stuff = new HashMap<>();
        for (TypeData.TypeDataInternal.TypesInternal types : typeData.getInternal().getTypes() ) {
            stuff.put(types.getId(),types);
        }
        assertEquals("Test Types header ID", Long.valueOf(63), stuff.get((long)63).getId());
        assertEquals("Test Types header size", Integer.valueOf(4), stuff.get((long)63).getSize());
        assertEquals("Test Types header name", "int", stuff.get((long)63).getName());
    }
*/
    @Test
    public void testStructContents() {
        Map<String, TypeData.TypeDataInternal.StructInternal> info = new HashMap<>();
        for(TypeData.TypeDataInternal.StructInternal struct : typeData.getInternal().getStructs()) {
            info.put(struct.getName(), struct);
        }
        assertEquals("Test struct.types.Struct header member","q", info.get("b_struct").getMembers().get(0).getName());
        assertEquals("Test struct.types.Struct header member", Long.valueOf(63), info.get("b_struct").getMembers().get(0).getType());
        assertEquals("Test struct.types.Struct header member","p", info.get("b_struct").getMembers().get(1).getName());
        assertEquals("Test struct.types.Struct header member",Long.valueOf(124), info.get("b_struct").getMembers().get(1).getType());
    }

    @Test
    public void testStructTypeRetrieval() {
        // Test struct-name and size of member list
        assertEquals("b_struct", typeData.getStructByName("b_struct").getName());
        assertEquals(2,typeData.getStructByName("b_struct").getMemberList().size());

        // Test members
        assertEquals("q", typeData.getStructByName("b_struct").getMemberList().get(0).getMemberName());
        assertEquals("int", typeData.getStructByName("b_struct").getMemberList().get(0).getMemberName());
        assertEquals("p", typeData.getStructByName("b_struct").getMemberList().get(1).getMemberName());
        assertEquals("double", typeData.getStructByName("b_struct").getMemberList().get(1).getMemberName());
    }
}
