package world.bentobox.aoneblock.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.Chest;
import org.bukkit.block.SuspiciousSand;
import org.bukkit.block.data.Brushable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.bgsoftware.wildstacker.api.loot.LootTable;

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
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 */
public class BlockListener implements Listener {

    /**
     * Main addon class.
     */
    private final AOneBlock addon;

    /**
     * Oneblock manager
     */
    private final OneBlocksManager oneBlocksManager;

    /**
     * Oneblock data database
     */
    private final Database<OneBlockIslands> handler;

    /**
     * Oneblock cache.
     */
    private final Map<String, OneBlockIslands> cache;

    /**
     * Phase checker class
     */
    private final CheckPhase check;

    /**
     * Sound player
     */
    private final WarningSounder warningSounder;

    /**
     * How many blocks ahead it should look.
     */
    public static final int MAX_LOOK_AHEAD = 5;

    /**
     * How often data is saved.
     */
    public static final int SAVE_EVERY = 50;

    private final Random random = new Random();

    // Loot for suspicious blocks
    private static final Map<Material, Double> LOOT;
    static {
        Map<Material, Double> loot = new HashMap<>();
        loot.put(Material.BLUE_DYE, 0.044);
        loot.put(Material.BRICK, 0.044);
        loot.put( Material.BROWN_CANDLE, 0.044);
        loot.put(Material.EMERALD, 0.044);
        loot.put(Material.GREEN_CANDLE, 0.044);
        loot.put(Material.LIGHT_BLUE_DYE, 0.044);
        loot.put(Material.ORANGE_DYE, 0.044);
        loot.put(Material.PURPLE_CANDLE, 0.044);
        loot.put(Material.RED_CANDLE, 0.044);
        loot.put(Material.WHEAT, 0.044);
        loot.put(Material.WOODEN_HOE, 0.044);
        loot.put(Material.YELLOW_DYE, 0.044);
        loot.put(Material.BEETROOT_SEEDS, 0.022);
        loot.put(Material.BLUE_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.COAL, 0.022);
        loot.put(Material.DEAD_BUSH, 0.022);
        loot.put(Material.FLOWER_POT, 0.022);
        loot.put(Material.LEAD, 0.022);
        loot.put(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.MAGENTA_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.OAK_HANGING_SIGN, 0.022);
        loot.put(Material.PINK_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.PURPLE_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.RED_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.SPRUCE_HANGING_SIGN, 0.022);
        loot.put(Material.STRING, 0.022);
        loot.put(Material.WHEAT_SEEDS, 0.022);
        loot.put(Material.YELLOW_STAINED_GLASS_PANE, 0.022);
        loot.put(Material.GOLD_NUGGET, 0.022);
        LOOT = loot;
    }

    /**
     * @param addon - OneBlock
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
      * Save the island cache
      */
     public void saveCache() {
         cache.values().forEach(handler::saveObjectAsync);
     }

     // ---------------------------------------------------------------------
     // Section: Listeners
     // ---------------------------------------------------------------------

     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onNewIsland(IslandCreatedEvent e) {
         if (addon.inWorld(e.getIsland().getWorld())) {
             setUp(e.getIsland());
         }
     }

     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onNewIsland(IslandResettedEvent e) {
         if (addon.inWorld(e.getIsland().getWorld())) {
             setUp(e.getIsland());
         }
     }

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
      * Drop items at the top of the block.
      * 
      * @param event EntitySpawnEvent object.
      */
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onItemStackSpawn(EntitySpawnEvent event) {
         if (!this.addon.getSettings().isDropOnTop()) {
             // Do nothing as item spawning is not interested in this case.
             return;
         }

         if (!EntityType.ITEM.equals(event.getEntityType())) {
             // We are interested only in dropped item entities.
             return;
         }

         if (!this.addon.inWorld(event.getLocation().getWorld())) {
             // Not correct world
             return;
         }

         Entity entity = event.getEntity();
         Location location = event.getLocation();

         Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(location)
                 .filter(island -> location.getBlock().getLocation().equals(island.getCenter()));

         if (optionalIsland.isPresent()) {
             // Teleport entity to the top of magic block.
             entity.teleport(optionalIsland.get().getCenter().add(0.5, 1, 0.5));
             entity.setVelocity(new Vector(0, 0, 0));
         }
     }

     // ---------------------------------------------------------------------
     // Section: Processing methods
     // ---------------------------------------------------------------------

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
      * Main block processing method that handles the magic block mechanics.
      * This includes phase changes, block spawning, and event handling.
      *
      * @param e - event causing the processing
      * @param i - island where it's happening
      * @param player - player who broke the block or who is involved - may be null
      * @param world - world where the block is being broken
      */
     private void process(@NonNull Cancellable e, @NonNull Island i, @Nullable Player player, @NonNull World world) {
         Block block = Objects.requireNonNull(i.getCenter()).toVector().toLocation(world).getBlock();
         OneBlockIslands is = getIsland(i);

         // Process phase changes and requirements
         ProcessPhaseResult phaseResult = processPhase(e, i, is, player, world, block);
         if (e.isCancelled()) {
             return;
         }

         // Initialize queue if needed
         initializeQueue(is, phaseResult.phase, phaseResult.isCurrPhaseNew);

         // Process hologram and warning sounds
         processHologramAndWarnings(i, is, phaseResult.phase, block);

         // Process the next block
         processNextBlock(e, i, player, block, is, phaseResult);
     }

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
             if (check.phaseRequirementsFail(player, i, is, phase, world)) {
                 e.setCancelled(true);
                 return new ProcessPhaseResult(phase, true, 0);
             }
             handleNewPhase(player, i, is, phase, block, prevPhaseName);
         } else if (is.getBlockNumber() % SAVE_EVERY == 0) {
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
      * Handles different types of block breaking events.
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
      * Handles bucket fill events including block spawning and event firing.
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

     private void spawnBlock(@NonNull OneBlockObject nextBlock, @NonNull Block block) {
         if (nextBlock.isCustomBlock()) {
             nextBlock.getCustomBlock().execute(addon, block);
         } else {
             @NonNull
             Material type = nextBlock.getMaterial();
             // Place new block with no physics
             block.setType(type, false);
             // Fill the chest
             if (type.equals(Material.CHEST) && nextBlock.getChest() != null) {
                 fillChest(nextBlock, block);
                 return;
             } else if (Tag.LEAVES.isTagged(type)) {
                 Leaves leaves = (Leaves) block.getState().getBlockData();
                 leaves.setPersistent(true);
                 block.setBlockData(leaves);
             }
         }

     }
     
    @SuppressWarnings("deprecation")
    @EventHandler
     public void onPlayerInteract(PlayerInteractEvent e) {
       if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
       if (e.getHand() != EquipmentSlot.HAND) return;
       if (e.getClickedBlock() == null) return;
       if (e.getClickedBlock().getType() != Material.SUSPICIOUS_GRAVEL &&
           e.getClickedBlock().getType() != Material.SUSPICIOUS_SAND) return;
       if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.BRUSH) return;
       BentoBox.getInstance().logDebug("Brushing " + e.getClickedBlock());
       if (e.getClickedBlock() != null && e.getClickedBlock().getBlockData() instanceof Brushable bb) {      
               BentoBox.getInstance().logDebug("item is brushable "  + bb.getDusted());      
       }
     }
     
     private static Material getRandomLoot(Random random) {
         double roll = random.nextDouble();
         double cumulative = 0.0;

         for (Map.Entry<Material, Double> entry : LOOT.entrySet()) {
             cumulative += entry.getValue();
             if (roll <= cumulative) {
                 return entry.getKey();
             }
         }

         // Fallback in case of rounding errors
         List<Material> materials = new ArrayList<>(LOOT.keySet());
         return materials.get(random.nextInt(materials.size()));
     }

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
