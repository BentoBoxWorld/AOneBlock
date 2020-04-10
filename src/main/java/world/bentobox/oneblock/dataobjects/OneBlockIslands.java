package world.bentobox.oneblock.dataobjects;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.bukkit.entity.EntityType;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.oneblock.listeners.BlockListener;
import world.bentobox.oneblock.listeners.OneBlockObject;

/**
 * @author tastybento
 *
 */
public class OneBlockIslands implements DataObject {

    @Expose
    private String uniqueId;
    @Expose
    private int blockNumber;
    @Expose
    private String phaseName = "";

    private Queue<OneBlockObject> queue;

    /**
     * @return the phaseName
     */
    public String getPhaseName() {
        return phaseName;
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
        this.blockNumber++;
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
        if (queue == null) queue = new ArrayBlockingQueue<>(BlockListener.MAX_LOOK_AHEAD);
        return queue;
    }

    /**
     * Get the nearest upcoming mob, if it exists
     * @param i - look ahead value
     * @return nearest upcoming mob
     */
    public Optional<EntityType> getNearestMob(int i) {
        if (queue == null) queue = new ArrayBlockingQueue<>(BlockListener.MAX_LOOK_AHEAD);
        return queue.stream().limit(i).filter(OneBlockObject::isEntity).findFirst().map(OneBlockObject::getEntityType);
    }

    public void add(OneBlockObject nextBlock) {
        if (queue == null) queue = new ArrayBlockingQueue<>(BlockListener.MAX_LOOK_AHEAD);
        queue.add(nextBlock);
    }

    public OneBlockObject pop(OneBlockObject toAdd) {
        if (queue == null) queue = new ArrayBlockingQueue<>(BlockListener.MAX_LOOK_AHEAD);
        OneBlockObject b = queue.remove();
        queue.add(toAdd);
        return b;
    }

    /**
     * Clear the look ahead queue
     */
    public void clearQueue() {
        queue.clear();
    }


}
