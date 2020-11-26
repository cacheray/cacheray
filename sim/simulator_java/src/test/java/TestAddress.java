import org.junit.Test;

import static org.junit.Assert.*;

import types.Address;

public class TestAddress {

    @Test
    public void testNumerical() {
        Address addr = new Address(0);
        Address addr2 = new Address(1);
        Address addr3 = new Address("0xffffffff");

        assertEquals("Test equality", addr, new Address(0));
        assertNotEquals("Test negative equality",addr2,addr);
    }

    @Test
    public void testAlignment() {
        Address addr = new Address(0);
        assertEquals("Test 0 alignment", addr, addr.aligned(8));
        assertEquals("Test non-zero alignment", new Address(67).aligned(8), new Address(64));
    }

    @Test
    public void testToString() {
        assertEquals("Test low ToString", new Address(1).toString(),"0x1");
        assertEquals("Test high ToString", new Address(-1).toString(), "0xffffffffffffffff");
    }
}
