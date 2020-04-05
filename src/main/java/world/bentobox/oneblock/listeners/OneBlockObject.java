package world.bentobox.oneblock.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents something that can be generated when a block is broken
 * Can be a block or entity
 * @author tastybento
 *
 */
public class OneBlockObject {

    private EntityType entityType;
    private Material material;
    private Map<Integer,ItemStack> chest;
    private boolean rare;
    private final int prob;

    /**
     * An entity
     * @param entityType - type
     */
    public OneBlockObject(EntityType entityType, int prob) {
        this.entityType = entityType;
        this.prob = prob;
    }

    /**
     * A block
     * @param material - block type
     */
    public OneBlockObject(Material material, int prob) {
        this.material = material;
        this.prob = prob;

    }

    /**
     * A chest
     * @param chest - list of itemstacks in the chest
     */
    public OneBlockObject(Map<Integer,ItemStack> chest, int prob) {
        this.material = Material.CHEST;
        this.chest = chest;
        this.prob = prob;

    }

    /**
     * Copy constructor
     * @param ob - OneBlockObject
     */
    public OneBlockObject(OneBlockObject ob) {
        this.chest = ob.getChest();
        this.entityType = ob.getEntityType();
        this.material = ob.getMaterial();
        this.prob = ob.getProb();
        this.rare = ob.isRare();
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
     * @return the rare
     */
    public boolean isRare() {
        return rare;
    }

    /**
     * @param rare the rare to set
     */
    public void setRare(boolean rare) {
        this.rare = rare;
    }

    /**
     * @return the prob
     */
    public int getProb() {
        return prob;
    }

}
