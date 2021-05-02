package world.bentobox.aoneblock.oneblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
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
    protected static final SortedMap<Double, Rarity> CHEST_CHANCES = new TreeMap<>();
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
    private List<String> startCommands;
    private List<String> endCommands;
    private List<Requirement> requirements;
    private List<OneBlockObject> fixedBlocks;


    /**
     * Construct a phase that starts at blockNumber. Phase continues forever until
     * another phase starts.
     * @param blockNumber - starting block number
     */
    public OneBlockPhase(String blockNumber) {
        this.blockNumber = blockNumber;
        startCommands = new ArrayList<>();
        endCommands = new ArrayList<>();
        requirements = new ArrayList<>();
        fixedBlocks = new ArrayList<>();
    }

    /**
     * @return the blockNumber
     */
    public String getBlockNumber() {
        return blockNumber;
    }

    /**
     * Get the block number as an integer
     * @return the integer value of the blockNumber
     */
    public int getBlockNumberValue() {
        return Integer.parseInt(blockNumber);
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
     * @param addon AOneBlock
     * @param blockNumber the block number in the phase requested
     * @return OneBlockObject selected
     */

    public OneBlockObject getNextBlock(AOneBlock addon, int blockNumber) {
        if (total <1) {
            addon.logError("Phase " + this.getPhaseName() + " has zero probability of generating blocks. Check config file. Is the block section missing?");
            return this.getFirstBlock() != null ? getFirstBlock() : new OneBlockObject(Material.GRASS_BLOCK,1);
        }
        if (blockNumber == 0 && this.getFirstBlock() != null) {
            return getResult(this.getFirstBlock());
        }
        if (blockNumber < this.getFixedBlocks().size()) {
            return getResult(this.getFixedBlocks().get(blockNumber));
        }
        OneBlockObject block = getRandomBlock(probMap, total);
        if (block.isEntity()) return block;
        return getResult(block);
    }

    private OneBlockObject getResult(OneBlockObject block) {
        return block.getMaterial().equals(Material.CHEST) && !chests.isEmpty() ? getRandomChest() : block;
    }

    private OneBlockObject getRandomChest() {
        // Get the right type of chest
        Rarity r = CHEST_CHANCES.getOrDefault(((TreeMap<Double, Rarity>) CHEST_CHANCES).ceilingKey(random.nextDouble()), Rarity.COMMON);
        // If the chest lists have no common fallback, then return empty chest
        if (!chests.containsKey(r) && !chests.containsKey(Rarity.COMMON)) return new OneBlockObject(Material.CHEST, 0);
        // Get the rare chest or worse case the common one
        List<OneBlockObject> list = chests.containsKey(r) ? chests.get(r) : chests.get(Rarity.COMMON);
        // Pick one from the list or return an empty chest. Note list.get() can return nothing
        return list.isEmpty() ? new OneBlockObject(Material.CHEST, 0) : list.get(random.nextInt(list.size()));
    }

    private OneBlockObject getRandomBlock(TreeMap<Integer, OneBlockObject> probMap2, int total2) {
        // Use +1 on the bound because the random choice is exclusive
        OneBlockObject temp = probMap2.get(random.nextInt(total2+1));
        if (temp == null) {
            temp = probMap2.ceilingEntry(random.nextInt(total2+1)).getValue();
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
        return probMap.values().stream().filter(OneBlockObject::isEntity).collect(Collectors.toMap(OneBlockObject::getEntityType, OneBlockObject::getProb));
    }

    /**
     * Get the block materials in this phase
     * @return map of materials and relative probabilities
     */
    public Map<Material, Integer> getBlocks() {
        return probMap.values().stream().filter(OneBlockObject::isMaterial).collect(Collectors.toMap(OneBlockObject::getMaterial, OneBlockObject::getProb));
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

    /**
     * @return the commands
     */
    public List<String> getStartCommands() {
        return startCommands;
    }

    /**
     * @param commands the commands to set
     */
    public void setStartCommands(List<String> commands) {
        this.startCommands = commands;
    }

    /**
     * @return the requirements
     */
    public List<Requirement> getRequirements() {
        return requirements;
    }

    /**
     * @param requirements the requirements to set
     */
    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    /**
     * @return the endCommands
     */
    public List<String> getEndCommands() {
        return endCommands;
    }

    /**
     * @param endCommands the endCommands to set
     */
    public void setEndCommands(List<String> endCommands) {
        this.endCommands = endCommands;
    }

    /**
     * @return the fixedBlocks
     */
    public List<OneBlockObject> getFixedBlocks() {
        return fixedBlocks;
    }

    /**
     * @param fixedBlocks the fixedBlocks to set
     */
    public void setFixedBlocks(List<OneBlockObject> fixedBlocks) {
        this.fixedBlocks = fixedBlocks;
    }



}