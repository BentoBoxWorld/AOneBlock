package world.bentobox.aoneblock.dataobjects;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.Expose;

import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * @author tastybento
 */
@Table(name = "OneBlockIslands")
public class OneBlockIslands implements DataObject {

    @Expose
    private String uniqueId;
    /**
     * The number of blocks broken in the current loop
     */
    @Expose
    private int blockNumber;
    /**
     * The lifetime number of blocks broken not including the current blockNumber.
     * This is unfortunately for backwards compatibility reasons.
     */
    @Expose
    private long lifetime;
    /**
     * Current phase number
     */
    @Expose
    private String phaseName = "";
    /**
     * Hologram text to show
     */
    @Expose
    private String hologram = "";

    private Queue<OneBlockObject> queue = new LinkedList<>();

    /**
     * @return the phaseName
     */
    @NonNull
    public String getPhaseName() {
        return phaseName == null ? "" : phaseName;
    }

    /**
     * @param phaseName the phaseName to set
     */
    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public OneBlockIslands(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * @return the blockNumber
     */
    public int getBlockNumber() {
        return blockNumber;
    }

    /**
     * @param blockNumber the blockNumber to set
     */
    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Increments the block number
     */
    public void incrementBlockNumber() {
        // Ensure that lifetime is always at least blockNumber
        if (this.lifetime < this.blockNumber) {
            this.lifetime = this.blockNumber;
        }
        this.blockNumber++;
        this.lifetime++;
    }

    /**
     * @return the hologram Line
     */
    @NonNull
    public String getHologram() {
        return hologram == null ? "" : hologram;
    }

    /**
     * @param hologramLine Hologram line
     */
    public void setHologram(String hologramLine) {
        this.hologram = hologramLine;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.database.objects.DataObject#getUniqueId()
     */
    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.database.objects.DataObject#setUniqueId(java.lang.String)
     */
    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * @return the queue
     */
    public Queue<OneBlockObject> getQueue() {
        if (queue == null) queue = new LinkedList<>();
        return queue;
    }

    /**
     * Get a list of nearby upcoming mobs
     * @param i - look ahead value
     * @return list of upcoming mobs
     */
    public List<EntityType> getNearestMob(int i) {
        return getQueue().stream().limit(i).filter(OneBlockObject::isEntity).map(OneBlockObject::getEntityType).toList();
    }

    /**
     * Adds a OneBlockObject to the queue
     * @param nextBlock the OneBlockObject to be added
     */
    public void add(OneBlockObject nextBlock) {
        getQueue().add(nextBlock);
    }

    /**
     * Retrieves and removes the head of the queue, or returns null if this queue is empty.
     * Inserts the specified element into the queue if it is possible to do so immediately without
     * violating capacity restrictions, and throwing an 
     * {@code IllegalStateException} if no space is currently available.
     * @param toAdd OneBlockObject
     * @return OneBlockObject head of the queue, or returns null if this queue is empty.
     */
    public OneBlockObject pollAndAdd(OneBlockObject toAdd) {
        getQueue();
        OneBlockObject b = queue.poll();
        queue.add(toAdd);
        return b;
    }

    /**
     * Clear the look ahead queue
     */
    public void clearQueue() {
        getQueue().clear();
    }

    /**
     * @return the lifetime number of blocks broken not including the current block count
     */
    public long getLifetime() {
        // Ensure that lifetime is always at least blockNumber
        if (this.lifetime < this.blockNumber) {
            this.lifetime = this.blockNumber;
        }
        return lifetime;
    }

    /**
     * @param lifetime lifetime number of blocks broken to set
     */
    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }


}
