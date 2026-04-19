package world.bentobox.aoneblock.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Brushable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.events.MagicBlockEntityEvent;
import world.bentobox.aoneblock.events.MagicBlockEvent;
import world.bentobox.aoneblock.events.MagicBlockPhaseEvent;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.flags.FlagListener;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * This listener handles all the core logic for the OneBlock block, including breaking, phase changes, and entity spawning.
 * @author tastybento
 */
public class BlockListener extends FlagListener implements Listener {

    /**
     * Main addon class.
     */
    private final AOneBlock addon;

    /**
     * Oneblock manager
     */
    private final OneBlocksManager oneBlocksManager;

    /**
     * Oneblock data database handler.
     */
    private final Database<OneBlockIslands> handler;

    /**
     * In-memory cache for OneBlock island data to reduce database lookups.
     */
    private final Map<String, OneBlockIslands> cache;

    /**
     * Active continuous-brushing sessions, keyed by player UUID. Each session
     * holds the repeating task driving dust progression and the block being brushed.
     */
    private final Map<UUID, BrushSession> brushSessions = new HashMap<>();

    /**
     * Per-player brushing session state.
     */
    private record BrushSession(BukkitTask task, Block block) {}

    /**
     * Helper class to check phase requirements.
     */
    private final CheckPhase check;

    /**
     * Helper class to play warning sounds for upcoming mobs.
     */
    private final WarningSounder warningSounder;

    /**
     * How many blocks ahead the queue should look when populating.
     */
    public static final int MAX_LOOK_AHEAD = 5;

    /**
     * How often island data is saved to the database (in blocks broken).
     */
    public static final int SAVE_EVERY = 50;

    /*
     * Loot tables for suspicious blocks
     */
    private static final NavigableMap<Integer, String> SUSPICIOUS_LOOT;
    static {
        TreeMap<Integer, String> sl = new TreeMap<>();
        sl.put(5,"archaeology/desert_pyramid");
        sl.put(10, "archaeology/desert_well");
        sl.put(15, "archaeology/ocean_ruin_cold");
        sl.put(20, "archaeology/ocean_ruin_warm");
        sl.put(25,  "archaeology/trail_ruins_common");
        sl.put(26,  "archaeology/trail_ruins_rare");
        SUSPICIOUS_LOOT = Collections.unmodifiableNavigableMap(sl);
    }  

    private static final Random RAND = new Random();
    
    /**
     * Constructs the BlockListener.
     * @param addon - The AOneBlock addon instance.
     */
    public BlockListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        handler = new Database<>(addon, OneBlockIslands.class);
        cache = new HashMap<>();
        oneBlocksManager = addon.getOneBlockManager();
        check = new CheckPhase(addon, this);
        warningSounder = new WarningSounder(addon);
    }

    /**
     * Saves all island data from the cache to the database asynchronously.
     */
    public void saveCache() {
        cache.values().forEach(handler::saveObjectAsync);
    }

    // ---------------------------------------------------------------------
    // Section: Listeners
    // ---------------------------------------------------------------------

    /**
     * Sets up a new OneBlock island when a BentoBox island is created.
     * @param e The IslandCreatedEvent.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandCreatedEvent e) {
        if (addon.inWorld(e.getIsland().getWorld())) {
            setUp(e.getIsland());
        }
    }

    /**
     * Resets a OneBlock island when a BentoBox island is reset.
     * @param e The IslandResettedEvent.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandResettedEvent e) {
        if (addon.inWorld(e.getIsland().getWorld())) {
            setUp(e.getIsland());
        }
    }

    /**
     * Removes OneBlock data when a BentoBox island is deleted.
     * @param e The IslandDeleteEvent.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        if (addon.inWorld(e.getIsland().getWorld())) {
            cache.remove(e.getIsland().getUniqueId());
            handler.deleteID(e.getIsland().getUniqueId());
        }
    }

    /**
     * Prevents liquids flowing into magic block
     * 
     * @param e BlockFromToEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getToBlock().getLocation();
        // Cannot flow to center block
        e.setCancelled(addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).isPresent());
    }

    /**
     * Handles the breaking of the magic block by a player.
     * @param e The BlockBreakEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter()))
        .ifPresent(i -> process(e, i, e.getPlayer(), e.getPlayer().getWorld()));
    }

    /**
     * Handles JetsMinions. These are special armor stands. Requires Minions 6.9.3
     * or later
     *
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreakByMinion(final EntityInteractEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld()) || !e.getEntityType().equals(EntityType.ARMOR_STAND)) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter()))
        .ifPresent(i -> process(e, i, null, e.getBlock().getWorld()));
    }

    /**
     * Check for water grabbing
     *
     * @param e - event (note that you cannot register PlayerBucketEvent)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(final PlayerBucketFillEvent e) {
        if (addon.inWorld(e.getBlock().getWorld())) {
            Location l = e.getBlock().getLocation();
            addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter()))
            .ifPresent(i -> process(e, i, e.getPlayer(), e.getPlayer().getWorld()));
        }
    }


    /**
     * This handler listens for items spawning.
     * If an item spawns exactly at an island's center block,
     * it cancels the spawn and re-drops the item 1 block higher
     * (at the center of that block) to stack it neatly.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        // --- Guard Clauses: Exit early if conditions aren't met ---

        // 1. Check if the "drop on top" feature is enabled.
        if (!this.addon.getSettings().isDropOnTop()) {
            // Feature is disabled, so we don't need to do anything.
            return;
        }

        // Get the spawn location once.
        Location spawnLocation = event.getLocation();

        // 2. Check if the spawn is happening in a world managed by the addon.
        if (!this.addon.inWorld(spawnLocation.getWorld())) {
            // Not a relevant world, ignore this event.
            return;
        }

        // Find an island at the spawn location.
        Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(spawnLocation)
                // Chained to the Optional: Filter the island.
                // Only keep it if the block the item spawned in
                // is *exactly* the island's center.
                .filter(island -> {
                    // .getBlock().getLocation() converts a precise location 
                    // (e.g., 10.2, 64.5, 12.8) to its block's location (10.0, 64.0, 12.0).
                    Location blockLocation = spawnLocation.getBlock().getLocation();
                    return blockLocation.equals(island.getCenter());
                });

        // If we found an island AND it passed the filter (spawned at center)...
        if (optionalIsland.isPresent()) {
            // 1. Cancel the original item spawn.
            event.setCancelled(true);

            // 2. Get the island and the item stack that was supposed to spawn.
            Island island = optionalIsland.get();
            // We use event.getEntity() which is guaranteed to be an Item.
            ItemStack itemStack = event.getEntity().getItemStack();

            // 3. Calculate the new, clean drop location.
            // .add(0.5, 1, 0.5) moves it to the center of the block (0.5)
            // and one block up (1.0) so it sits on top.
            Location newDropLocation = island.getCenter().add(0.5, 1, 0.5);

            // 4. Drop the item stack at the new location.
            spawnLocation.getWorld().dropItem(newDropLocation, itemStack);
        }
    }

    // ---------------------------------------------------------------------
    // Section: Processing methods
    // ---------------------------------------------------------------------

    /**
     * Sets up the initial state for a new OneBlock island.
     * @param island The island to set up.
     */
    private void setUp(@NonNull Island island) {
        // Set the bedrock to the initial block
        Util.getChunkAtAsync(Objects.requireNonNull(island.getCenter()))
        .thenRun(() -> island.getCenter().getBlock().setType(Material.GRASS_BLOCK));
        // Create a database entry
        OneBlockIslands is = new OneBlockIslands(island.getUniqueId());
        cache.put(island.getUniqueId(), is);
        handler.saveObjectAsync(is);
        addon.getHoloListener().setUp(island, is, true);
    }

    /**
     * Main magic block processing method that handles the magic block mechanics.
     * This includes phase changes, block spawning, and event handling.
     *
     * @param e - event causing the processing
     * @param island - island where it's happening
     * @param player - player who broke the block or who is involved - may be null
     * @param world - world where the block is being broken
     */
    private void process(@NonNull Cancellable e, @NonNull Island island, @Nullable Player player, @NonNull World world) {
        // Check if the player has authority to break the magic block
        if (!checkIsland((@NonNull Event) e, player, island.getCenter(), addon.MAGIC_BLOCK)) {
            // Not allowed
            return;
        }

        Block block = Objects.requireNonNull(island.getCenter()).toVector().toLocation(world).getBlock();
        OneBlockIslands is = getIsland(island);

        // Process phase changes and requirements
        ProcessPhaseResult phaseResult = processPhase(e, island, is, player, world, block);
        if (e.isCancelled()) {
            return;
        }

        // Initialize queue if needed
        initializeQueue(is, phaseResult.phase, phaseResult.isCurrPhaseNew);

        // Process hologram and warning sounds
        processHologramAndWarnings(island, is, phaseResult.phase, block);

        // Process the next block
        processNextBlock(e, island, player, block, is, phaseResult);
    }

    /**
     * A record to hold the result of phase processing.
     * @param phase The current phase.
     * @param isCurrPhaseNew Whether the current phase is new.
     * @param blockNumber The block number within the current phase.
     */
    private record ProcessPhaseResult(OneBlockPhase phase, boolean isCurrPhaseNew, int blockNumber) {}

    /**
     * Processes phase changes and requirements for the magic block.
     * Returns a record containing phase info, whether it's a new phase, and block number.
     *
     * @param e - event being processed
     * @param i - island instance
     * @param is - oneblock island data
     * @param player - player involved
     * @param world - world where processing occurs
     * @param block - block being processed
     * @return ProcessPhaseResult containing phase details
     */
    private ProcessPhaseResult processPhase(Cancellable e, Island i, OneBlockIslands is, Player player, World world, Block block) {
        OneBlockPhase phase = oneBlocksManager.getPhase(is.getBlockNumber());
        String prevPhaseName = is.getPhaseName();

        if (Objects.requireNonNull(phase).getGotoBlock() != null) {
            phase = handleGoto(is, phase.getGotoBlock());
        }

        String currPhaseName = phase.getPhaseName() == null ? "" : phase.getPhaseName();
        handlePhaseChange(is, phase, currPhaseName);

        boolean isCurrPhaseNew = !is.getPhaseName().equalsIgnoreCase(currPhaseName);
        if (isCurrPhaseNew) {
            // Check if the player meets the requirements for the new phase.
            if (check.phaseRequirementsFail(player, i, is, phase, world)) {
                e.setCancelled(true);
                return new ProcessPhaseResult(phase, true, 0);
            }
            handleNewPhase(player, i, is, phase, block, prevPhaseName);
        } else if (is.getBlockNumber() % SAVE_EVERY == 0) {
            // Periodically save the island's progress.
            saveIsland(i);
        }

        int materialBlocksInQueue = (int) is.getQueue().stream()
                .filter(obo -> obo.isMaterial() || obo.isCustomBlock())
                .count();
        int blockNumber = is.getBlockNumber() - (phase.getBlockNumberValue() - 1) + materialBlocksInQueue;

        return new ProcessPhaseResult(phase, isCurrPhaseNew, blockNumber);
    }

    /**
     * Handles the initialization of a new phase including biome setting and event firing.
     *
     * @param player - player triggering the phase change
     * @param i - island instance
     * @param is - oneblock island data
     * @param phase - new phase being entered
     * @param block - block being processed
     * @param prevPhaseName - name of the previous phase
     */
    private void handleNewPhase(Player player, Island i, OneBlockIslands is, OneBlockPhase phase, Block block, String prevPhaseName) {
        check.setNewPhase(player, i, is, phase);
        is.clearQueue();
        setBiome(block, phase.getPhaseBiome());
        Bukkit.getPluginManager().callEvent(new MagicBlockPhaseEvent(i, 
                player == null ? null : player.getUniqueId(),
                        block, phase.getPhaseName(), prevPhaseName, is.getBlockNumber()));
    }

    /**
     * Handles phase transition mechanics including setting timestamps for phase changes.
     *
     * @param is - oneblock island data
     * @param phase - current phase
     * @param currPhaseName - name of current phase
     */
    private void handlePhaseChange(OneBlockIslands is, OneBlockPhase phase, String currPhaseName) {
        OneBlockPhase nextPhase = oneBlocksManager.getPhase(is.getBlockNumber() + 1);
        if (Objects.requireNonNull(nextPhase).getGotoBlock() != null) {
            nextPhase = oneBlocksManager.getPhase(nextPhase.getGotoBlock());
        }
        String nextPhaseName = nextPhase == null || nextPhase.getPhaseName() == null ? "" : nextPhase.getPhaseName();
        if (!currPhaseName.equalsIgnoreCase(nextPhaseName)) {
            is.setLastPhaseChangeTime(System.currentTimeMillis());
        }
    }

    /**
     * Initializes the block queue for a phase with upcoming blocks.
     *
     * @param is - oneblock island data
     * @param phase - current phase
     * @param isCurrPhaseNew - whether this is a new phase
     */
    private void initializeQueue(OneBlockIslands is, OneBlockPhase phase, boolean isCurrPhaseNew) {
        if (is.getQueue().isEmpty() || isCurrPhaseNew) {
            for (int j = 0; j < MAX_LOOK_AHEAD; j++) {
                is.add(phase.getNextBlock(addon, j));
            }
        }
    }

    /**
     * Updates holograms and plays warning sounds if configured.
     *
     * @param i - island instance
     * @param is - oneblock island data
     * @param phase - current phase
     * @param block - block being processed
     */
    private void processHologramAndWarnings(Island i, OneBlockIslands is, OneBlockPhase phase, Block block) {
        addon.getHoloListener().process(i, is, phase);
        if (addon.getSettings().getMobWarning() > 0) {
            warningSounder.play(is, block);
        }
    }

    /**
     * Processes the next block in the sequence, handling entities and block changes.
     *
     * @param e - event being processed
     * @param i - island instance
     * @param player - player involved
     * @param block - block being processed
     * @param is - oneblock island data
     * @param phaseResult - result from phase processing
     */
    private void processNextBlock(Cancellable e, Island i, Player player, Block block, OneBlockIslands is, ProcessPhaseResult phaseResult) {
        OneBlockObject nextBlock = (phaseResult.isCurrPhaseNew && phaseResult.phase.getFirstBlock() != null) 
                ? phaseResult.phase.getFirstBlock()
                        : is.pollAndAdd(phaseResult.phase.getNextBlock(addon, phaseResult.blockNumber));

        if (nextBlock.isEntity()) {
            handleEntitySpawn(e, i, player, block, nextBlock);
            return;
        }

        is.incrementBlockNumber();
        handleBlockBreak(e, i, player, block, nextBlock);
    }

    /**
     * Handles entity spawning for entity-type blocks.
     *
     * @param e - event being processed
     * @param i - island instance
     * @param player - player involved
     * @param block - block where entity will spawn
     * @param nextBlock - next block object containing entity info
     */
    private void handleEntitySpawn(Cancellable e, Island i, Player player, Block block, OneBlockObject nextBlock) {
        if (!(e instanceof EntitySpawnEvent)) {
            e.setCancelled(true);
        }
        spawnEntity(nextBlock, block);
        Bukkit.getPluginManager().callEvent(new MagicBlockEntityEvent(i,
                player == null ? null : player.getUniqueId(), 
                        block, nextBlock.getEntityType()));
    }

    /**
     * Handles different types of block breaking events (player, bucket, entity).
     *
     * @param e - event being processed
     * @param i - island instance
     * @param player - player involved
     * @param block - block being broken
     * @param nextBlock - next block to spawn
     */
    private void handleBlockBreak(Cancellable e, Island i, Player player, Block block, OneBlockObject nextBlock) {
        if (e instanceof BlockBreakEvent) {
            breakBlock(player, block, nextBlock, i);
        } else if (e instanceof PlayerBucketFillEvent) {
            handleBucketFill(player, i, block, nextBlock);
        } else if (e instanceof EntitySpawnEvent || e instanceof EntityInteractEvent) {
            handleEntityBreak(i, block, nextBlock, e instanceof EntityInteractEvent);
        }
    }

    /**
     * Handles bucket fill events on the magic block.
     *
     * @param player - player filling bucket
     * @param i - island instance
     * @param block - block being processed
     * @param nextBlock - next block to spawn
     */
    private void handleBucketFill(Player player, Island i, Block block, OneBlockObject nextBlock) {
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> spawnBlock(nextBlock, block));
        ItemStack tool = Objects.requireNonNull(player).getInventory().getItemInMainHand();
        Bukkit.getPluginManager().callEvent(new MagicBlockEvent(i, player.getUniqueId(), tool, block, nextBlock.getMaterial()));
    }

    /**
     * Handles entity-related block breaking including minion interactions.
     *
     * @param i - island instance
     * @param block - block being broken
     * @param nextBlock - next block to spawn
     * @param isMinion - whether the breaker is a minion
     */
    private void handleEntityBreak(Island i, Block block, OneBlockObject nextBlock, boolean isMinion) {
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> spawnBlock(nextBlock, block));
        if (isMinion) {
            Bukkit.getPluginManager().callEvent(new MagicBlockEvent(i, null, null, block, nextBlock.getMaterial()));
        }
    }

    /**
     * Handles goto block mechanics, updating block numbers and lifetime.
     *
     * @param is - oneblock island data
     * @param gotoBlock - target block number
     * @return OneBlockPhase for the target block
     */
    private OneBlockPhase handleGoto(OneBlockIslands is, int gotoBlock) {
        // Store lifetime
        is.setLifetime(is.getLifetime() + gotoBlock);
        // Set current block
        is.setBlockNumber(gotoBlock);
        return oneBlocksManager.getPhase(gotoBlock);
    }

    /**
     * Sets the biome in a small radius around the given block.
     * @param block The center block.
     * @param biome The biome to set.
     */
    private void setBiome(@NonNull Block block, @Nullable Biome biome) {
        if (biome == null) {
            return;
        }
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -4; y <= 4; y++) {
                    block.getWorld().setBiome(block.getX() + x, block.getY() + y, block.getZ() + z, biome);
                }
            }
        }
    }

    /**
     * This method is called when block is removed, and next must be spawned. It
     * also teleports player above the magic block, to avoid falling in void.
     * 
     * @param player    Player who breaks the block.
     * @param block     Block that was broken.
     * @param nextBlock Next Block that will be summoned.
     * @param island    Island where player is located.
     */
    private void breakBlock(@Nullable Player player, Block block, @NonNull OneBlockObject nextBlock,
            @NonNull Island island) {
        ItemStack tool = Objects.requireNonNull(player).getInventory().getItemInMainHand();

        // Break normally and lift the player up so they don't fall
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> this.spawnBlock(nextBlock, block));

        if (player.getLocation().getBlock().equals(block)) {
            double delta = 1 - (player.getLocation().getY() - block.getY());
            player.teleport(player.getLocation().add(new Vector(0, delta, 0)));
            player.setVelocity(new Vector(0, 0, 0));
        } else if (player.getLocation().getBlock().equals(block.getRelative(BlockFace.UP))) {
            player.teleport(player.getLocation());
            player.setVelocity(new Vector(0, 0, 0));
        }

        // Fire event
        Bukkit.getPluginManager()
        .callEvent(new MagicBlockEvent(island, player.getUniqueId(), tool, block, nextBlock.getMaterial()));
    }

    /**
     * Spawns the next block in the sequence, handling custom blocks and block data.
     * @param nextBlock The object representing the block to spawn.
     * @param block The block in the world to be replaced.
     */
    private void spawnBlock(@NonNull OneBlockObject nextBlock, @NonNull Block block) {
        if (nextBlock.isCustomBlock()) {
            nextBlock.getCustomBlock().execute(addon, block);
            return;
        }
        Material type = nextBlock.getMaterial();
        block.setType(type, false);
        if (type.equals(Material.CHEST) && nextBlock.getChest() != null) {
            fillChest(nextBlock, block);
        } else if (Tag.LEAVES.isTagged(type)) {
            setLeavesPersistent(block);
        } else if (type == Material.SUSPICIOUS_GRAVEL || type == Material.SUSPICIOUS_SAND) {
            spawnSuspiciousBlock(block, type);
        }
    }

    /**
     * Sets a leaves block to persistent so it does not decay.
     * @param block The leaves block.
     */
    private void setLeavesPersistent(Block block) {
        Leaves leaves = (Leaves) block.getState().getBlockData();
        leaves.setPersistent(true);
        block.setBlockData(leaves);
    }

    /**
     * Sets up a suspicious gravel or sand block with a randomly chosen archaeology loot table.
     * @param block The block to configure.
     * @param type  SUSPICIOUS_GRAVEL or SUSPICIOUS_SAND.
     */
    private void spawnSuspiciousBlock(Block block, Material type) {
        // Use setBlockData (safer than setType for block entities) — no physics
        block.setBlockData(type.createBlockData(), false);

        if (!(block.getState() instanceof BrushableBlock suspiciousBlock)) {
            return;
        }
        int randomValue = RAND.nextInt(SUSPICIOUS_LOOT.lastKey());
        String loot = SUSPICIOUS_LOOT.higherEntry(randomValue).getValue();
        NamespacedKey lootTableKey = new NamespacedKey("minecraft", loot);
        LootTable lootTable = Bukkit.getLootTable(lootTableKey);
        if (lootTable != null) {
            suspiciousBlock.setLootTable(lootTable);
            suspiciousBlock.update(true, false);
        } else {
            BentoBox.getInstance().logWarning("Could not find loot table: " + lootTableKey);
        }
    }

    /**
     * Handles player interaction with suspicious blocks (sand/gravel) using a brush.
     * This is currently for debugging purposes.
     * @param e The PlayerInteractEvent.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!addon.inWorld(e.getPlayer().getWorld())) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.SUSPICIOUS_GRAVEL && block.getType() != Material.SUSPICIOUS_SAND) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.BRUSH) return;
        if (addon.getIslands().getIslandAt(block.getLocation())
                .filter(i -> i.getCenter().equals(block.getLocation())).isEmpty()) return;

        if (block.getBlockData() instanceof Brushable bb) {
            int dusted = bb.getDusted() + 1;
            if (dusted > bb.getMaximumDusted()) {
                completeBrush(e.getPlayer(), block);
                return;
            }
            bb.setDusted(dusted);
            block.setBlockData(bb);
            playBrushFeedback(block);
            updateBrushSession(e.getPlayer(), block);
        }
    }

    private void updateBrushSession(Player player, Block block) {
        // Kick off a continuous-brush session so the player can hold right-click
        // and have dusting advance automatically (vanilla feel). The kickoff click
        // above already advances one stage; the timer picks up from there.
        UUID uuid = player.getUniqueId();
        BrushSession existing = brushSessions.get(uuid);
        if (existing != null && !existing.block().equals(block)) {
            cancelBrushSession(uuid);
            existing = null;
        }
        if (existing == null) {
            brushSessions.put(uuid, startContinuousBrush(player, block));
        }
    }

    /**
     * Schedules a repeating task that advances brushing on the given block while the
     * player keeps holding right-click with the brush. Period of 10 ticks per dust stage
     * matches the vanilla brush cadence.
     * @param player The brushing player.
     * @param block  The suspicious block being brushed.
     * @return A new BrushSession holding the scheduled task.
     */
    private BrushSession startContinuousBrush(Player player, Block block) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(addon.getPlugin(), new Runnable() {
            @Override
            public void run() {
                // Validate that the player is still actively brushing this block.
                if (!player.isOnline()
                        || player.getInventory().getItemInMainHand().getType() != Material.BRUSH
                        || !player.isHandRaised()
                        || !block.equals(player.getTargetBlockExact(5))
                        || (block.getType() != Material.SUSPICIOUS_GRAVEL
                                && block.getType() != Material.SUSPICIOUS_SAND)
                        || !(block.getBlockData() instanceof Brushable bb)) {
                    cancelBrushSession(uuid);
                    return;
                }
                int dusted = bb.getDusted() + 1;
                if (dusted > bb.getMaximumDusted()) {
                    completeBrush(player, block);
                    cancelBrushSession(uuid);
                    return;
                }
                bb.setDusted(dusted);
                block.setBlockData(bb);
                playBrushFeedback(block);
            }
        }, 10L, 10L);
        return new BrushSession(task, block);
    }

    /**
     * Cancels any active brushing session for the given player UUID.
     * @param uuid The player's UUID.
     */
    private void cancelBrushSession(UUID uuid) {
        BrushSession session = brushSessions.remove(uuid);
        if (session != null) {
            session.task().cancel();
        }
    }

    /**
     * Clean up any brushing session when a player disconnects.
     * @param e The PlayerQuitEvent.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        cancelBrushSession(e.getPlayer().getUniqueId());
    }

    /**
     * Plays brushing particles and sound at a suspicious block to give visible/audible
     * progress feedback. Needed because the block is placed programmatically rather than
     * spawning naturally, so the vanilla brush animation is not triggered on clients.
     * @param block The suspicious block being brushed.
     */
    private void playBrushFeedback(Block block) {
        World world = block.getWorld();
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        // Dust particles using the block's own data so they match sand/gravel colour.
        world.spawnParticle(Particle.BLOCK, center, 10, 0.25, 0.25, 0.25, 0.0, block.getBlockData());
        Sound brushSound = (block.getType() == Material.SUSPICIOUS_GRAVEL)
                ? Sound.ITEM_BRUSH_BRUSHING_GRAVEL
                : Sound.ITEM_BRUSH_BRUSHING_SAND;
        world.playSound(center, brushSound, 0.8f, 1.0f);
    }

    /**
     * Completes the brushing of a suspicious block: drops loot (if available), plays the
     * break sound, removes the block, fires a BlockBreakEvent, and damages the brush.
     * @param player The brushing player.
     * @param block  The suspicious block being brushed.
     */
    private void completeBrush(Player player, Block block) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        World world = block.getWorld();

        if (block.getState() instanceof BrushableBlock suspiciousBlock) {
            LootTable lootTable = suspiciousBlock.getLootTable();
            if (lootTable != null) {
                LootContext context = new LootContext.Builder(loc)
                        .lootedEntity(player).killer(player).build();
                Collection<ItemStack> items = lootTable.populateLoot(new Random(), context);
                for (ItemStack item : items) {
                    world.dropItemNaturally(loc, item);
                }
            }
        }

        Sound breakSound = (block.getType() == Material.SUSPICIOUS_GRAVEL)
                ? Sound.BLOCK_SUSPICIOUS_GRAVEL_BREAK
                : Sound.BLOCK_SUSPICIOUS_SAND_BREAK;
        world.playSound(loc, breakSound, 1.0f, 1.0f);
        block.setType(Material.AIR);
        Bukkit.getPluginManager().callEvent(new BlockBreakEvent(block, player));
        player.getInventory().getItemInMainHand().damage(1, player);
    }

    /**
     * Spawns an entity at the magic block location.
     * @param nextBlock The object containing entity information.
     * @param block The magic block.
     */
    private void spawnEntity(@NonNull OneBlockObject nextBlock, @NonNull Block block) {
        if (block.isEmpty())
            block.setType(Material.STONE);
        Location spawnLoc = block.getLocation().add(new Vector(0.5D, 1D, 0.5D));
        Entity entity = block.getWorld().spawnEntity(spawnLoc, nextBlock.getEntityType());
        // Make space for entity - this will blot out blocks
        if (addon.getSettings().isClearBlocks()) {
            new MakeSpace(addon).makeSpace(entity, spawnLoc);
        }
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
    }

    /**
     * Fills a chest with items and adds particle effects based on rarity.
     * @param nextBlock The object containing chest information.
     * @param block The chest block.
     */
    private void fillChest(@NonNull OneBlockObject nextBlock, @NonNull Block block) {
        Chest chest = (Chest) block.getState();
        nextBlock.getChest().forEach(chest.getBlockInventory()::setItem);
        Color color = Color.fromBGR(0, 255, 255); // yellow
        switch (nextBlock.getRarity()) {
        case EPIC:
            color = Color.fromBGR(255, 0, 255); // magenta
            break;
        case RARE:
            color = Color.fromBGR(255, 255, 255); // cyan
            break;
        case UNCOMMON:
            // Yellow
            break;
        default:
            // No sparkles for regular chests
            return;
        }
        block.getWorld().spawnParticle(Particle.DUST, block.getLocation().add(new Vector(0.5, 1.0, 0.5)), 50, 0.5,
                0, 0.5, 1, new Particle.DustOptions(color, 1));
    }

    /**
     * Get the one block island data
     *
     * @param i - island
     * @return one block island
     */
    @NonNull
    public OneBlockIslands getIsland(@NonNull Island i) {
        return cache.containsKey(i.getUniqueId()) ? cache.get(i.getUniqueId()) : loadIsland(i.getUniqueId());
    }

    /**
     * Get all the OneBlockIslands from the Database
     * 
     * @return list of oneblock islands
     */
    public List<OneBlockIslands> getAllIslands() {
        return handler.loadObjects();
    }

    /**
     * Loads an island's OneBlock data from the database into the cache.
     * If it doesn't exist, a new object is created.
     * @param uniqueId The unique ID of the island.
     * @return The OneBlockIslands data object.
     */
    @NonNull
    private OneBlockIslands loadIsland(@NonNull String uniqueId) {
        if (handler.objectExists(uniqueId)) {
            OneBlockIslands island = handler.loadObject(uniqueId);
            if (island != null) {
                // Add to cache
                cache.put(island.getUniqueId(), island);
                return island;
            }
        }
        return cache.computeIfAbsent(uniqueId, OneBlockIslands::new);
    }

    /**
     * @return the oneBlocksManager
     */
    public OneBlocksManager getOneBlocksManager() {
        return oneBlocksManager;
    }

    /**
     * Saves the island progress to the database async
     *
     * @param island - island
     * @return CompletableFuture - true if saved or not in cache, false if save
     *         failed
     */
    public CompletableFuture<Boolean> saveIsland(@NonNull Island island) {
        if (cache.containsKey(island.getUniqueId())) {
            return handler.saveObjectAsync(cache.get(island.getUniqueId()));
        }
        return CompletableFuture.completedFuture(true);
    }
}
