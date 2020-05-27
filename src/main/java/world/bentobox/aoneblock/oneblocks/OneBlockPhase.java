package world.bentobox.aoneblock.oneblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockObject.Rarity;


public class OneBlockPhase {
    public static final TreeMap<Double, Rarity> CHEST_CHANCES = new TreeMap<>();
    static {
        CHEST_CHANCES.put(0.62D, Rarity.COMMON);
        CHEST_CHANCES.put(0.87D, Rarity.UNCOMMON);
        CHEST_CHANCES.put(0.96D, Rarity.RARE);
        CHEST_CHANCES.put(1D, Rarity.EPIC);
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
    private final Map<Rarity, List<OneBlockObject>> chests = new EnumMap<>(Rarity.class);
    private final Random random = new Random();
    private final String blockNumber;
    private Integer gotoBlock;
    private int blockTotal = 0;
    private int entityTotal = 0;


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
    @Nullable
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
    @Nullable
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
        blockTotal += prob;
        probMap.put(total, new OneBlockObject(material, prob));
    }

    /**
     * Adds an entity type and associated probability
     * @param entityType - entityType
     * @param prob - probability
     */
    public void addMob(EntityType entityType, int prob) {
        total += prob;
        entityTotal += prob;
        probMap.put(total, new OneBlockObject(entityType, prob));
    }

    public void addChest(Map<Integer, ItemStack> items, Rarity rarity) {
        chests.computeIfAbsent(rarity, k -> new ArrayList<>()).add(new OneBlockObject(items, rarity));
    }

    /**
     * This picks a random object
     * @return OneBlockObject selected
     */

    public OneBlockObject getNextBlock(AOneBlock addon) {
        if (total <1) {
            addon.logError("Phase " + this.getPhaseName() + " has zero probability of generating blocks. Check config file. Is the block section missing?");
            return this.getFirstBlock() != null ? getFirstBlock() : new OneBlockObject(Material.GRASS_BLOCK,1);
        }
        OneBlockObject block = getRandomBlock(probMap, total);
        if (block.isEntity()) return block;
        return block.getMaterial().equals(Material.CHEST) && !chests.isEmpty() ? getRandomChest() : block;
    }

    private OneBlockObject getRandomChest() {
        // Get the right type of chest
        Rarity r = CHEST_CHANCES.getOrDefault(CHEST_CHANCES.ceilingKey(random.nextDouble()), Rarity.COMMON);
        List<OneBlockObject> list = chests.getOrDefault(r, Collections.emptyList());
        // Pick one from the list or return an empty chest
        return list.isEmpty() ? new OneBlockObject(Material.CHEST, 0) : list.get(random.nextInt(list.size()));
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
    @Nullable
    public OneBlockObject getFirstBlock() {
        return firstBlock;
    }

    /**
     * @param firstBlock the firstBlock to set
     */
    public void setFirstBlock(OneBlockObject firstBlock) {
        this.firstBlock = firstBlock;
    }

    /**
     * Get all the chests in this phase
     * @return collection of all the chests
     */
    public Collection<OneBlockObject> getChests() {
        return chests.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * @return the chest map
     */
    public Map<Rarity, List<OneBlockObject>> getChestsMap() {
        return chests;
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

    /**
     * @return the gotoBlock
     */
    public Integer getGotoBlock() {
        return gotoBlock;
    }

    /**
     * @param gotoBlock the gotoBlock to set
     */
    public void setGotoBlock(Integer gotoBlock) {
        this.gotoBlock = gotoBlock;
    }

    /**
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * @return the blockTotal
     */
    public int getBlockTotal() {
        return blockTotal;
    }

    /**
     * @return the entityTotal
     */
    public int getEntityTotal() {
        return entityTotal;
    }

    /**
     * @return true if phase is a goto phase
     */
    public boolean isGotoPhase() {
        return gotoBlock != null;
    }

}