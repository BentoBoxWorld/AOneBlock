package world.bentobox.oneblock.listeners;

import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;


public class OneBlockPhase {

    /**
     * Tree map of all materials and their probabilities as a ratio to the sum of all probabilities
     */
    private final TreeMap<Integer, OneBlockObject> probMap = new TreeMap<>();
    /**
     * Sum of all probabilities
     */
    private int total = 0;
    private String phaseName;
    private Biome phaseBiome;
    private final TreeMap<Integer, OneBlockObject> probChest = new TreeMap<>();
    private int chestTotal = 0;
    private final Random random = new Random();

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

    /**
     * @return the phaseBiome
     */
    public Biome getPhaseBiome() {
        return phaseBiome;
    }

    /**
     * @param phaseBiome the phaseBiome to set
     */
    public void setPhaseBiome(Biome phaseBiome) {
        this.phaseBiome = phaseBiome;
    }

    /**
     * Adds a material and associated probability
     * @param material - Material
     * @param prob - probability
     */
    public void addBlock(Material material, int prob) {
        total += prob;
        probMap.put(total, new OneBlockObject(material));
    }

    /**
     * Adds an entity type and associated probability
     * @param entityType - entityType
     * @param prob - probability
     */
    public void addMob(EntityType entityType, int prob) {
        total += prob;
        probMap.put(total, new OneBlockObject(entityType));
    }

    public void addChest(List<ItemStack> chest, int prob) {
        chestTotal += prob;
        probChest.put(chestTotal, new OneBlockObject(chest));
    }

    /**
     * This picks a random object
     * @return OneBlockObject selected
     */

    public OneBlockObject getNextBlock() {
        OneBlockObject block = getRandomBlock(probMap, total);
        if (block.isEntity()) return block;
        return block.getMaterial().equals(Material.CHEST) ? getRandomBlock(probChest, chestTotal) : block;
    }

    private OneBlockObject getRandomBlock(TreeMap<Integer, OneBlockObject> probMap2, int total2) {
        OneBlockObject temp = probMap2.get(random.nextInt(total2));
        if (temp == null) {
            temp = probMap2.ceilingEntry(random.nextInt(total2)).getValue();
        }
        if (temp == null) {
            temp = probMap2.firstEntry().getValue();
        }
        return new OneBlockObject(temp);
    }
}