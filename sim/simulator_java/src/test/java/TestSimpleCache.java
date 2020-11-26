import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;

import cache.DirectMappedCache;
import org.junit.*;
import types.Address;
import types.CacheAccess;

public class TestSimpleCache {
    private DirectMappedCache cache;
    private BufferedWriter writer;
    private String name = "testCache";
    private int cacheSize = 16;
    private int blockSize = 6;
    private short replacementPolicy = 0;
    private short writePolicy = 0;

    @Before
    public void setUp() {
        writer = new BufferedWriter(new OutputStreamWriter(OutputStream.nullOutputStream()));
    }

    @After
    public void tearDown() {
        cache = null;
        writer = null;
    }

    @Test
    public void testTest() {
        assertTrue(true);
    }

    @Test
    public void testTearDown() {
        assertTrue(cache == null);
    }

    @Test
    public void testInitialized(){
        cache = new DirectMappedCache(name, cacheSize, blockSize, writePolicy, replacementPolicy, null);
        assertTrue(cache.getHits() == 0);
        assertTrue(cache.getMisses() == 0);
    }

    @Test
    public void testRead(){
        cache = new DirectMappedCache(name, cacheSize, blockSize, writePolicy, replacementPolicy, null);
        CacheAccess ca = new CacheAccess(1);
        cache.read(new Address(1234), (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 1);
        ca = new CacheAccess(1);
        cache.read(new Address(1234), (short) 8, writer, true, ca);
        assertTrue(cache.getHits() == 1);
        ca = new CacheAccess(1);
        cache.read(new Address(1234), (short) 8, writer, true, ca);
        assertTrue(cache.getHits() == 2);
    }

    @Test
    public void testWrite(){
        cache = new DirectMappedCache(name, cacheSize, blockSize, writePolicy, replacementPolicy, null);
        CacheAccess ca = new CacheAccess(1);
        cache.read(new Address(1234), (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 1);
        ca = new CacheAccess(1);
        cache.write(new Address(1234), (short) 8, writer, true);
        assertTrue(cache.getHits() == 0);
        ca = new CacheAccess(1);
        cache.read(new Address(1234), (short) 8, writer, true, ca);
        assertTrue(cache.getHits() == 1);
    }

    @Test
    public void testReplacement(){
        cache = new DirectMappedCache(name, cacheSize, blockSize, writePolicy, replacementPolicy, null);
        CacheAccess ca = new CacheAccess(1);
        int bSize = 1<<blockSize;
        Address baseAddr = new Address((long) bSize*1337);
        Address loopAddr = new Address(1<<cacheSize);
        cache.read(baseAddr, (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 1);
        ca = new CacheAccess(1);
        cache.read(new Address(baseAddr.getValue()+loopAddr.getValue()), (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 2);
        ca = new CacheAccess(1);
        cache.read(baseAddr, (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 3);
    }

    @Test
    public void testStradleCacheLine(){
        cache = new DirectMappedCache(name, cacheSize, blockSize, writePolicy, replacementPolicy, null);
        CacheAccess ca = new CacheAccess(1);    
        int bSize = 1<<blockSize;
        long baseAddr = (long) bSize*1337;
        cache.read(new Address(baseAddr-4), (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 2);
    }

    @Test
    public void testStradleCacheLine2(){
        cache = new DirectMappedCache(name, cacheSize, blockSize, writePolicy, replacementPolicy, null);
        int bSize = 1<<blockSize;
        CacheAccess ca = new CacheAccess(1);
        long baseAddr = (long) bSize*1337;
        cache.read(new Address(baseAddr-4), (short) 8, writer, true, ca);
        ca = new CacheAccess(1);
        cache.read(new Address(baseAddr-4), (short) 8, writer, true, ca);
        assertTrue(cache.getMisses() == 2);
        assertTrue(cache.getHits() == 2);
    }
}
