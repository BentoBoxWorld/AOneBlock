package world.bentobox.aoneblock.dataobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.aoneblock.oneblocks.OneBlockObject;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class OneBlockIslandsTest {
    
    private OneBlockIslands obi;
    private String id;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        id = UUID.randomUUID().toString();
        obi = new OneBlockIslands(id);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getPhaseName()}.
     */
    @Test
    public void testGetPhaseName() {
        assertEquals("", obi.getPhaseName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setPhaseName(java.lang.String)}.
     */
    @Test
    public void testSetPhaseName() {
        obi.setPhaseName("test");
        assertEquals("test", obi.getPhaseName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#OneBlockIslands(java.lang.String)}.
     */
    @Test
    public void testOneBlockIslands() {
        assertNotNull(obi);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getBlockNumber()}.
     */
    @Test
    public void testGetBlockNumber() {
        assertEquals(0, obi.getBlockNumber());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setBlockNumber(int)}.
     */
    @Test
    public void testSetBlockNumber() {
        obi.setBlockNumber(1234);
        assertEquals(1234, obi.getBlockNumber());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#incrementBlockNumber()}.
     */
    @Test
    public void testIncrementBlockNumber() {
        obi.incrementBlockNumber();
        assertEquals(1, obi.getBlockNumber());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getHologram()}.
     */
    @Test
    public void testGetHologram() {
        assertEquals("", obi.getHologram());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setHologram(java.lang.String)}.
     */
    @Test
    public void testSetHologram() {
        obi.setHologram("test");
        assertEquals("test",obi.getHologram());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getUniqueId()}.
     */
    @Test
    public void testGetUniqueId() {
        assertEquals(id, obi.getUniqueId());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setUniqueId(java.lang.String)}.
     */
    @Test
    public void testSetUniqueId() {
        String t = UUID.randomUUID().toString();
        obi.setUniqueId(t);
        assertEquals(t, obi.getUniqueId());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getQueue()}.
     */
    @Test
    public void testGetQueue() {
        Queue<OneBlockObject> q = obi.getQueue();
        assertTrue(q.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#getNearestMob(int)}.
     */
    @Test
    public void testGetNearestMob() {
       List<EntityType> l = obi.getNearestMob(10);
       assertTrue(l.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#add(world.bentobox.aoneblock.oneblocks.OneBlockObject)}.
     */
    @Test
    public void testAdd() {
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
    public void testPollAndAdd() {
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
    public void testClearQueue() {
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
    public void testGetLifetime() {
        assertEquals(0L, obi.getLifetime());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.dataobjects.OneBlockIslands#setLifetime(long)}.
     */
    @Test
    public void testSetLifetime() {
        obi.setLifetime(123456789L);
        assertEquals(123456789L, obi.getLifetime());
    }

}
