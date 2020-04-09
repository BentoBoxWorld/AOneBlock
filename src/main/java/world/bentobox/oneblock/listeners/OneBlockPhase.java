package world.bentobox.oneblock.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.oneblock.listeners.OneBlockObject.Rarity;


public class OneBlockPhase {

    private final static Map<Rarity, Integer> RARITY_TO_PROB;
    static {
        Map<Rarity, Integer> m = new HashMap<>();
        m.put(Rarity.COMMON, 62);
        m.put(Rarity.UNCOMMON, 25);
        m.put(Rarity.RARE, 10);
        m.put(Rarity.EPIC, 4);
        RARITY_TO_PROB = Collections.unmodifiableMap(m);
    }

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
    private Environment environment;
    private OneBlockObject firstBlock;
    private final TreeMap<Integer, OneBlockObject> probChest = new TreeMap<>();
    private int chestTotal = 0;
    private final Random random = new Random();
    private final String blockNumber;


    public OneBlockPhase(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * @return the blockNumber
     */
    public String getBlockNumber() {
        return blockNumber;
    }

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
        probMap.put(total, new OneBlockObject(material, prob));
    }

    /**
     * Adds an entity type and associated probability
     * @param entityType - entityType
     * @param prob - probability
     */
    public void addMob(EntityType entityType, int prob) {
        total += prob;
        probMap.put(total, new OneBlockObject(entityType, prob));
    }

    public void addChest(Map<Integer, ItemStack> items, Rarity rarity) {
        chestTotal += RARITY_TO_PROB.get(rarity);
        probChest.put(chestTotal, new OneBlockObject(items, rarity));
    }

    /**
     * This picks a random object
     * @return OneBlockObject selected
     */

    public OneBlockObject getNextBlock() {
        OneBlockObject block = getRandomBlock(probMap, total);
        if (block.isEntity()) return block;
        return block.getMaterial().equals(Material.CHEST) && chestTotal > 0 ? getRandomBlock(probChest, chestTotal) : block;
    }

    OneBlockObject getRandomBlock(TreeMap<Integer, OneBlockObject> probMap2, int total2) {
        OneBlockObject temp = probMap2.get(random.nextInt(total2));
        if (temp == null) {
            temp = probMap2.ceilingEntry(random.nextInt(total2)).getValue();
        }
        if (temp == null) {
            temp = probMap2.firstEntry().getValue();
        }
        return new OneBlockObject(temp);
    }

    /**
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @return the firstBlock
     */
    public OneBlockObject getFirstBlock() {
        return firstBlock;
    }

    /**
     * @param firstBlock the firstBlock to set
     */
    public void setFirstBlock(OneBlockObject firstBlock) {
        this.firstBlock = firstBlock;
    }

    public Collection<OneBlockObject> getChests() {
        return probChest.values();
    }

    /**
     * Get the mobs that are in this phase
     * @return map of mob type and its relative probability
     */
    public Map<EntityType, Integer> getMobs() {
        return probMap.values().stream().filter(o -> o.isEntity()).collect(Collectors.toMap(OneBlockObject::getEntityType, OneBlockObject::getProb));
    }

    /**
     * Get the block materials in this phase
     * @return map of materials and relative probabilities
     */
    public Map<Material, Integer> getBlocks() {
        return probMap.values().stream().filter(o -> o.isMaterial()).collect(Collectors.toMap(OneBlockObject::getMaterial, OneBlockObject::getProb));
    }

}