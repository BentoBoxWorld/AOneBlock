package world.bentobox.oneblock.listeners;

import java.util.List;

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
    private List<ItemStack> chest;

    public OneBlockObject(EntityType entityType) {
        this.entityType = entityType;
    }

    public OneBlockObject(Material material) {
        this.material = material;
    }

    public OneBlockObject(List<ItemStack> chest) {
        this.material = Material.CHEST;
        this.chest = chest;
    }

    /**
     * Copy constructor
     * @param ob - OneBlockObject
     */
    public OneBlockObject(OneBlockObject ob) {
        this.chest = ob.getChest();
        this.entityType = ob.getEntityType();
        this.material = ob.getMaterial();
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
    public List<ItemStack> getChest() {
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

}
