import org.junit.Test;
import types.StructType;
import types.TypeData;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestStructTypeRewrite {
    private static final String DWARF_DATA_FILE_NAME = TestSimpleSim.class.getResource("struct_test/structs.dwarf.json").getFile();

    @Test
    public void testBasicRewrite() throws FileNotFoundException {
        TypeData types = new TypeData(DWARF_DATA_FILE_NAME, 1);
        StructType basicStruct = types.getStructByName("struct Basic");
        assertEquals("not placeholder", "struct Basic", basicStruct.getName());
        long offsetA = basicStruct.getMemberList().get(0).getOffset();
        long offsetB = basicStruct.getMemberList().get(1).getOffset();
        Map<String, Long> reMap = new HashMap<>();
        reMap.put("a", offsetB);
        reMap.put("b", offsetA);
        assertEquals("a, b, c", basicStruct.getMemberList().get(0).getMemberName(), "a");
        basicStruct.reorderMemberList(reMap);
        assertEquals("b, a, c", basicStruct.getMemberList().get(0).getMemberName(), "b");
        assertEquals("b, a, c", basicStruct.getMemberList().get(1).getMemberName(), "a");
        assertEquals("offset of a (should be b's)", basicStruct.getMemberList().get(1).getOffset(), offsetB);
    }
}
