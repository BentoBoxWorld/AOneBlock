package world.bentobox.aoneblock.oneblocks;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents something that can be generated when a block is broken
 * Can be a block or entity
 *
 * @author tastybento
 */
public class OneBlockObject {

    public enum Rarity {
        /**
         * Applies to most items.
         */
        COMMON,
        /**
         * Mostly common treasure items, as well as drops from minor bosses.
         * e.g. banner patterns, Exp bottle, potions, elytra, enchanted books, heads
         * heart of the sea, nether star, totem of undying
         */
        UNCOMMON,
        /**
         * Items crafted from boss drops, as well as trickier to obtain treasures.
         * Beacon, conduit, enchanted armor, golden apple, music discs
         */
        RARE,
        /**
         * Mostly reserved for extremely difficult-to-obtain treasures.
         * Enchanted golden apple, creative-exclusive items
         */
        EPIC
    }

    private EntityType entityType;
    private Material material;
    private Map<Integer, ItemStack> chest;
    private Rarity rarity;
    private OneBlockCustomBlock customBlock;
    private int prob;

    /**
     * An entity
     *
     * @param entityType - type
     * @param prob       - relative probability
     */
    public OneBlockObject(EntityType entityType, int prob) {
        this.entityType = entityType;
        this.prob = prob;
    }

    /**
     * A block
     *
     * @param material - block type
     * @param prob     - relative probability
     */
    public OneBlockObject(Material material, int prob) {
        this.material = material;
        this.prob = prob;
    }

    /**
     * A custom block
     *
     * @param customBlock - custom block
     * @param prob        - relative probability
     */
    public OneBlockObject(OneBlockCustomBlock customBlock, int prob) {
        this.customBlock = customBlock;
        this.prob = prob;
    }


    /**
     * A chest
     *
     * @param chest - list of itemstacks in the chest
     */
    public OneBlockObject(Map<Integer, ItemStack> chest, Rarity rarity) {
        this.material = Material.CHEST;
        this.chest = chest;
        this.setRarity(rarity);
    }

    /**
     * Copy constructor
     *
     * @param ob - OneBlockObject
     */
    public OneBlockObject(OneBlockObject ob) {
        this.chest = ob.getChest();
        this.entityType = ob.getEntityType();
        this.material = ob.getMaterial();
        this.rarity = ob.getRarity();
        this.prob = ob.getProb();
        this.customBlock = ob.getCustomBlock();
    }

    /**
     * @return the entityType
     */
    public EntityType getEntityType() {
        return entityType;
    }


    /**
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }


    /**
     * @return the inventory
     */
    public Map<Integer, ItemStack> getChest() {
        return chest;
    }


    /**
     * @return the customBlock
     */
    public OneBlockCustomBlock getCustomBlock() {
        return customBlock;
    }


    /**
     * @return the isMaterial
     */
    public boolean isMaterial() {
        return material != null;
    }


    /**
     * @return the isEntity
     */
    public boolean isEntity() {
        return entityType != null;
    }


    /**
     * @return the isCustomBlock
     */
    public boolean isCustomBlock() {
        return customBlock != null;
    }

    /**
     * @return the rarity
     */
    public Rarity getRarity() {
        return rarity == null ? Rarity.COMMON : rarity;
    }

    /**
     * @param rarity the rarity to set
     */
    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    /**
     * @return the prob
     */
    public int getProb() {
        return prob;
    }

    /**
     * @param prob the prob to set
     */
    public void setProb(int prob) {
        this.prob = prob;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OneBlockObject [" + (entityType != null ? "entityType=" + entityType + ", " : "")
                + (material != null ? "material=" + material + ", " : "")
                + (chest != null ? "chest=" + chest + ", " : "") + (rarity != null ? "rarity=" + rarity + ", " : "")
                + (customBlock != null ? "customBlock=" + customBlock + ", " : "")
                + "prob=" + prob + "]";
    }

}
