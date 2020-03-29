package world.bentobox.oneblock.dataobjects;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;

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

}
