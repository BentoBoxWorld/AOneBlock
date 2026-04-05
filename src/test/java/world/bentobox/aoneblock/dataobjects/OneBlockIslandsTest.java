package world.bentobox.aoneblock.dataobjects;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;

/**
 * @author tastybento
 *
 */
public class OneBlockIslandsTest extends CommonTestSetup {
    
    private OneBlockIslands obi;
    private String id;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        id = UUID.randomUUID().toString();
        obi = new OneBlockIslands(id);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getPhaseName()}.
     */
    @Test
    void testGetPhaseName() {
        assertEquals("", obi.getPhaseName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setPhaseName(java.lang.String)}.
     */
    @Test
    void testSetPhaseName() {
        obi.setPhaseName("test");
        assertEquals("test", obi.getPhaseName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#OneBlockIslands(java.lang.String)}.
     */
    @Test
    void testOneBlockIslands() {
        assertNotNull(obi);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getBlockNumber()}.
     */
    @Test
    void testGetBlockNumber() {
        assertEquals(0, obi.getBlockNumber());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setBlockNumber(int)}.
     */
    @Test
    void testSetBlockNumber() {
        obi.setBlockNumber(1234);
        assertEquals(1234, obi.getBlockNumber());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#incrementBlockNumber()}.
     */
    @Test
    void testIncrementBlockNumber() {
        obi.incrementBlockNumber();
        assertEquals(1, obi.getBlockNumber());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getHologram()}.
     */
    @Test
    void testGetHologram() {
        assertEquals("", obi.getHologram());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setHologram(java.lang.String)}.
     */
    @Test
    void testSetHologram() {
        obi.setHologram("test");
        assertEquals("test",obi.getHologram());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getUniqueId()}.
     */
    @Test
    void testGetUniqueId() {
        assertEquals(id, obi.getUniqueId());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setUniqueId(java.lang.String)}.
     */
    @Test
    void testSetUniqueId() {
        String t = UUID.randomUUID().toString();
        obi.setUniqueId(t);
        assertEquals(t, obi.getUniqueId());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getQueue()}.
     */
    @Test
    void testGetQueue() {
        Queue<OneBlockObject> q = obi.getQueue();
        assertTrue(q.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getNearestMob(int)}.
     */
    @Test
    void testGetNearestMob() {
       List<EntityType> l = obi.getNearestMob(10);
       assertTrue(l.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#add(world.bentobox.aoneblock.oneblocks.OneBlockObject)}.
     */
    @Test
    void testAdd() {
        OneBlockObject obo = new OneBlockObject(EntityType.ALLAY, 50);
        obi.add(obo);
        Queue<OneBlockObject> q = obi.getQueue();
        OneBlockObject ob = q.poll();
        assertEquals(obo, ob);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#pollAndAdd(world.bentobox.aoneblock.oneblocks.OneBlockObject)}.
     */
    @Test
    void testPollAndAdd() {
        OneBlockObject obo = new OneBlockObject(EntityType.ALLAY, 50);
        OneBlockObject ob = obi.pollAndAdd(obo);
        assertNull(ob);
        ob = obi.getQueue().poll();
        assertEquals(obo, ob);
  
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#clearQueue()}.
     */
    @Test
    void testClearQueue() {
        OneBlockObject obo = new OneBlockObject(EntityType.ALLAY, 50);
        obi.add(obo);
        assertEquals(1, obi.getQueue().size());
        obi.clearQueue();
        assertEquals(0, obi.getQueue().size());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getLifetime()}.
     */
    @Test
    void testGetLifetime() {
        assertEquals(0L, obi.getLifetime());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setLifetime(long)}.
     */
    @Test
    void testSetLifetime() {
        obi.setLifetime(123456789L);
        assertEquals(123456789L, obi.getLifetime());
    }

}
