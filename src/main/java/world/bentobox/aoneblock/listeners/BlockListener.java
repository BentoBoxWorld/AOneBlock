package world.bentobox.aoneblock.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.events.BlockClearEvent;
import world.bentobox.aoneblock.events.MagicBlockEntityEvent;
import world.bentobox.aoneblock.events.MagicBlockEvent;
import world.bentobox.aoneblock.events.MagicBlockPhaseEvent;
import world.bentobox.aoneblock.oneblocks.MobAspects;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.Requirement;
import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * @author tastybento
 */
public class BlockListener implements Listener {

    private final AOneBlock addon;
    private final OneBlocksManager oneBlocksManager;
    private final Database<OneBlockIslands> handler;
    private final Map<String, OneBlockIslands> cache;
    private final Random random = new Random();

    /**
     * Tools that take damage. See https://minecraft.gamepedia.com/Item_durability#Tool_durability
     */
    private static final Map<Material, Integer> TOOLS;

    static {
        Map<Material, Integer> t = new EnumMap<>(Material.class);
        t.put(Material.DIAMOND_AXE, 1);
        t.put(Material.DIAMOND_SHOVEL, 1);
        t.put(Material.DIAMOND_PICKAXE, 1);
        t.put(Material.IRON_AXE, 1);
        t.put(Material.IRON_SHOVEL, 1);
        t.put(Material.IRON_PICKAXE, 1);
        t.put(Material.WOODEN_AXE, 1);
        t.put(Material.WOODEN_SHOVEL, 1);
        t.put(Material.WOODEN_PICKAXE, 1);
        t.put(Material.GOLDEN_AXE, 1);
        t.put(Material.GOLDEN_SHOVEL, 1);
        t.put(Material.GOLDEN_PICKAXE, 1);
        t.put(Material.STONE_AXE, 1);
        t.put(Material.STONE_SHOVEL, 1);
        t.put(Material.STONE_PICKAXE, 1);
        t.put(Material.SHEARS, 1);
        t.put(Material.DIAMOND_SWORD, 2);
        t.put(Material.GOLDEN_SWORD, 2);
        t.put(Material.STONE_SWORD, 2);
        t.put(Material.IRON_SWORD, 2);
        t.put(Material.WOODEN_SWORD, 2);
        t.put(Material.TRIDENT, 2);
        TOOLS = Collections.unmodifiableMap(t);
    }

    /**
     * Water entities
     */
    private static final List<EntityType> WATER_ENTITIES = Arrays.asList(
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            EntityType.COD,
            EntityType.SALMON,
            EntityType.PUFFERFISH,
            EntityType.TROPICAL_FISH,
            EntityType.DROWNED,
            EntityType.DOLPHIN,
            EntityType.AXOLOTL,
            EntityType.SQUID,
            EntityType.GLOW_SQUID);

    private static final Map<EntityType, MobAspects> MOB_ASPECTS;
    public static final int MAX_LOOK_AHEAD = 5;
    public static final int SAVE_EVERY = 50;

    static {
        Map<EntityType, MobAspects> m = new EnumMap<>(EntityType.class);
        m.put(EntityType.BLAZE, new MobAspects(Sound.ENTITY_BLAZE_AMBIENT, Color.fromRGB(238, 211, 91)));
        m.put(EntityType.CAVE_SPIDER, new MobAspects(Sound.ENTITY_SPIDER_AMBIENT, Color.fromRGB(63, 37, 31)));
        m.put(EntityType.CREEPER, new MobAspects(Sound.ENTITY_CREEPER_PRIMED, Color.fromRGB(125, 255, 106)));
        m.put(EntityType.DROWNED, new MobAspects(Sound.ENTITY_DROWNED_AMBIENT, Color.fromRGB(109, 152, 144)));
        m.put(EntityType.ELDER_GUARDIAN, new MobAspects(Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, Color.fromRGB(201, 143, 113)));
        m.put(EntityType.ENDERMAN, new MobAspects(Sound.ENTITY_ENDERMAN_AMBIENT, Color.fromRGB(0, 0, 0)));
        m.put(EntityType.ENDERMITE, new MobAspects(Sound.ENTITY_ENDERMITE_AMBIENT, Color.fromRGB(30, 30, 30)));
        m.put(EntityType.EVOKER, new MobAspects(Sound.ENTITY_EVOKER_AMBIENT, Color.fromRGB(144, 148, 148)));
        m.put(EntityType.GHAST, new MobAspects(Sound.ENTITY_GHAST_AMBIENT, Color.fromRGB(242, 242, 242)));
        m.put(EntityType.GUARDIAN, new MobAspects(Sound.ENTITY_GUARDIAN_AMBIENT, Color.fromRGB(201, 143, 113)));
        m.put(EntityType.HUSK, new MobAspects(Sound.ENTITY_HUSK_AMBIENT, Color.fromRGB(111, 104, 90)));
        m.put(EntityType.ILLUSIONER, new MobAspects(Sound.ENTITY_ILLUSIONER_AMBIENT, Color.fromRGB(144, 149, 149)));
        m.put(EntityType.PIGLIN, new MobAspects(Sound.ENTITY_PIGLIN_AMBIENT, Color.fromRGB(255, 215, 0)));
        m.put(EntityType.PIGLIN_BRUTE, new MobAspects(Sound.ENTITY_PIGLIN_BRUTE_AMBIENT, Color.fromRGB(255, 215, 0)));
        m.put(EntityType.ZOMBIFIED_PIGLIN, new MobAspects(Sound.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT, Color.fromRGB(125, 100, 0)));
        m.put(EntityType.PILLAGER, new MobAspects(Sound.ENTITY_PILLAGER_AMBIENT, Color.fromRGB(74, 74, 53)));
        m.put(EntityType.RAVAGER, new MobAspects(Sound.ENTITY_RAVAGER_AMBIENT, Color.fromRGB(85, 78, 73)));
        m.put(EntityType.SHULKER, new MobAspects(Sound.ENTITY_SHULKER_AMBIENT, Color.fromRGB(142, 106, 146)));
        m.put(EntityType.SILVERFISH, new MobAspects(Sound.ENTITY_SILVERFISH_AMBIENT, Color.fromRGB(211, 211, 211)));
        m.put(EntityType.SKELETON, new MobAspects(Sound.ENTITY_SKELETON_AMBIENT, Color.fromRGB(211, 211, 211)));
        m.put(EntityType.SPIDER, new MobAspects(Sound.ENTITY_SPIDER_AMBIENT, Color.fromRGB(94, 84, 73)));
        m.put(EntityType.STRAY, new MobAspects(Sound.ENTITY_STRAY_AMBIENT, Color.fromRGB(118, 132, 135)));
        m.put(EntityType.VEX, new MobAspects(Sound.ENTITY_VEX_AMBIENT, Color.fromRGB(137, 156, 176)));
        m.put(EntityType.VINDICATOR, new MobAspects(Sound.ENTITY_VINDICATOR_AMBIENT, Color.fromRGB(137, 156, 166)));
        m.put(EntityType.WITCH, new MobAspects(Sound.ENTITY_WITCH_AMBIENT, Color.fromRGB(56, 39, 67)));
        m.put(EntityType.WITHER, new MobAspects(Sound.ENTITY_WITHER_AMBIENT, Color.fromRGB(80, 80, 80)));
        m.put(EntityType.WITHER_SKELETON, new MobAspects(Sound.ENTITY_WITHER_SKELETON_AMBIENT, Color.fromRGB(100, 100, 100)));
        m.put(EntityType.ZOGLIN, new MobAspects(Sound.ENTITY_ZOGLIN_AMBIENT, Color.fromRGB(255, 192, 203)));
        m.put(EntityType.ZOMBIE, new MobAspects(Sound.ENTITY_ZOMBIE_AMBIENT, Color.fromRGB(74, 99, 53)));
        m.put(EntityType.ZOMBIE_VILLAGER, new MobAspects(Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT, Color.fromRGB(111, 104, 90)));

        MOB_ASPECTS = Collections.unmodifiableMap(m);
    }

    /**
     * @param addon - OneBlock
     */
    public BlockListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        handler = new Database<>(addon, OneBlockIslands.class);
        cache = new HashMap<>();
        oneBlocksManager = addon.getOneBlockManager();
    }

    /**
     * Save the island cache
     */
    public void saveCache() {
        cache.values().forEach(handler::saveObjectAsync);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandCreatedEvent e) {
        if (addon.inWorld(e.getIsland().getWorld())) {
            setUp(e.getIsland());
        }
    }

    private void setUp(@NonNull Island island) {
        // Set the bedrock to the initial block
        Util.getChunkAtAsync(Objects.requireNonNull(island.getCenter())).thenRun(() -> island.getCenter().getBlock().setType(Material.GRASS_BLOCK));
        // Create a database entry
        OneBlockIslands is = new OneBlockIslands(island.getUniqueId());
        cache.put(island.getUniqueId(), is);
        handler.saveObjectAsync(is);
        if (addon.useHolographicDisplays()) {
            addon.getHoloListener().setUp(island, is);
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
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> process(e, i, e.getPlayer(), e.getPlayer().getWorld()));
    }

    /**
     * Handles JetsMinions. These are special armor stands. Requires Minions 6.9.3 or later
     *
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreakByMinion(final EntityInteractEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld()) || !e.getEntityType().equals(EntityType.ARMOR_STAND)) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> process(e, i, null, e.getBlock().getWorld()));
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
            addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> process(e, i, e.getPlayer(), e.getPlayer().getWorld()));
        }
    }

    /**
     * Main block processing method
     *
     * @param e      - event causing the processing
     * @param i      - island where it's happening
     * @param player - player who broke the block or who is involved - may be null
     * @param world  - world where the block is being broken
     */
    private void process(@NonNull Cancellable e, @NonNull Island i, @Nullable Player player, @NonNull World world) {
        // Get island from cache or load it
        OneBlockIslands is = getIsland(i);
        // Get the phase for this island
        OneBlockPhase phase = oneBlocksManager.getPhase(is.getBlockNumber());
        // Store the original phase in case it changes.
        String originalPhase = is.getPhaseName();
        // Check for a goto
        if (Objects.requireNonNull(phase).getGotoBlock() != null) {
            int gotoBlock = phase.getGotoBlock();
            phase = oneBlocksManager.getPhase(gotoBlock);
            // Store lifetime
            is.setLifetime(is.getLifetime() + is.getBlockNumber());
            // Set current block
            is.setBlockNumber(gotoBlock);

        }
        // Check for new phase and run commands if required
        boolean newPhase = checkPhase(player, i, is, Objects.requireNonNull(phase));
        if (!newPhase && is.getBlockNumber() % SAVE_EVERY == 0) {
            // Save island data every MAX_LOOK_AHEAD blocks.
            saveIsland(i);
        }
        // Check if requirements met
        if (phaseRequirementsFail(player, i, phase, world)) {
            e.setCancelled(true);
            return;
        }
        if (newPhase) {
            is.clearQueue();
        }
        // Get the block number in this phase
        int blockNumber = is.getBlockNumber() - phase.getBlockNumberValue() + (int) is.getQueue().stream().filter(OneBlockObject::isMaterial).count();
        // Get the block that is being broken
        Block block = Objects.requireNonNull(i.getCenter()).toVector().toLocation(world).getBlock();
        // Fill a 5 block queue
        if (is.getQueue().isEmpty() || newPhase) {
            // Add initial 5 blocks
            for (int j = 0; j < MAX_LOOK_AHEAD; j++) {
                is.add(phase.getNextBlock(addon, blockNumber++));
            }
        }
        // Manage Holograms
        if (addon.useHolographicDisplays()) {
            addon.getHoloListener().process(i, is, phase);
        }
        // Play warning sound for upcoming mobs
        if (addon.getSettings().getMobWarning() > 0) {
            playWarning(is, block);
        }
        // Get the next block
        OneBlockObject nextBlock = (newPhase && phase.getFirstBlock() != null) ? phase.getFirstBlock() : is.pollAndAdd(phase.getNextBlock(addon, blockNumber++));
        // Set the biome for the block and one block above it
        if (newPhase) {
            setBiome(block, phase.getPhaseBiome());
            // Fire new phase event
            Bukkit.getPluginManager().callEvent(new MagicBlockPhaseEvent(i, player == null ? null : player.getUniqueId(), block, phase.getPhaseName(), originalPhase, is.getBlockNumber()));
        }
        // Entity
        if (nextBlock.isEntity()) {
            if (!(e instanceof EntitySpawnEvent)) e.setCancelled(true);
            // Entity spawns do not increment the block number or break the block
            spawnEntity(nextBlock, block);
            // Fire event
            Bukkit.getPluginManager().callEvent(new MagicBlockEntityEvent(i, player == null ? null : player.getUniqueId(), block, nextBlock.getEntityType()));
            return;
        }
        // Break the block
        if (e instanceof BlockBreakEvent) {
            breakBlock(e, player, block, world, nextBlock, i);
        } else if (e instanceof PlayerBucketFillEvent) {
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> spawnBlock(nextBlock, block));
            // Fire event
            ItemStack tool = Objects.requireNonNull(player).getInventory().getItemInMainHand();
            Bukkit.getPluginManager().callEvent(new MagicBlockEvent(i, player.getUniqueId(), tool, block, nextBlock.getMaterial()));
        } else if (e instanceof EntitySpawnEvent) {
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> spawnBlock(nextBlock, block));
        } else if (e instanceof EntityInteractEvent) {
            // Minion breaking block
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> spawnBlock(nextBlock, block));
            // Fire event
            Bukkit.getPluginManager().callEvent(new MagicBlockEvent(i, null, null, block, nextBlock.getMaterial()));
        }
        // Increment the block number
        is.incrementBlockNumber();
    }

    /**
     * Checks whether the player can proceed to the next phase
     *
     * @param player - player
     * @param i      - island
     * @param phase  - one block phase
     * @param world  - world
     * @return true if the player can proceed to the next phase, false if not or if there is no next phase.
     */
    private boolean phaseRequirementsFail(@Nullable Player player, @NonNull Island i, OneBlockPhase phase, @NonNull World world) {
        if (phase.getRequirements().isEmpty()) {
            return false;
        }
        // Check requirements
        for (Requirement r : phase.getRequirements()) {
            switch (r.getType()) {
            case LEVEL:
                return addon.getAddonByName("Level").map(l -> {
                    if (((Level) l).getIslandLevel(world, i.getOwner()) < r.getLevel()) {
                        User.getInstance(player).sendMessage("aoneblock.phase.insufficient-level", TextVariables.NUMBER, String.valueOf(r.getLevel()));
                        return true;
                    }
                    return false;
                }).orElse(false);
            case BANK:
                return addon.getAddonByName("Bank").map(l -> {
                    if (((Bank) l).getBankManager().getBalance(i).getValue() < r.getBank()) {
                        User.getInstance(player).sendMessage("aoneblock.phase.insufficient-bank-balance", TextVariables.NUMBER, String.valueOf(r.getBank()));
                        return true;
                    }
                    return false;
                }).orElse(false);
            case ECO:
                return addon.getPlugin().getVault().map(l -> {
                    if (l.getBalance(User.getInstance(player), world) < r.getEco()) {
                        User.getInstance(player).sendMessage("aoneblock.phase.insufficient-funds", TextVariables.NUMBER, String.valueOf(r.getEco()));
                        return true;
                    }
                    return false;
                }).orElse(false);
            case PERMISSION:
                if (player != null && !player.hasPermission(r.getPermission())) {
                    User.getInstance(player).sendMessage("aoneblock.phase.insufficient-permission", TextVariables.NAME, String.valueOf(r.getPermission()));
                    return true;
                }
                return false;
            default:
                break;

            }
        }
        return false;
    }

    private void playWarning(@NonNull OneBlockIslands is, @NonNull Block block) {
        List<EntityType> opMob = is.getNearestMob(addon.getSettings().getMobWarning());
        opMob.stream().filter(MOB_ASPECTS::containsKey).map(MOB_ASPECTS::get).forEach(s -> {
            block.getWorld().playSound(block.getLocation(), s.sound(), 1F, 1F);
            block.getWorld().spawnParticle(Particle.REDSTONE, block.getLocation().add(new Vector(0.5, 1.0, 0.5)), 10, 0.5, 0, 0.5, 1, new Particle.DustOptions(s.color(), 1));
        });

    }

    /**
     * Check whether this phase is done or not.
     *
     * @param player - player
     * @param i      - island
     * @param is     - OneBlockIslands object
     * @param phase  - current phase name
     * @return true if this is a new phase, false if not
     */
    private boolean checkPhase(@Nullable Player player, @NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        String phaseName = phase.getPhaseName() == null ? "" : phase.getPhaseName();
        if (!is.getPhaseName().equalsIgnoreCase(phaseName)) {
            // Run previous phase end commands
            oneBlocksManager.getPhase(is.getPhaseName()).ifPresent(oldPhase -> {
                String oldPhaseName = oldPhase.getPhaseName() == null ? "" : oldPhase.getPhaseName();
                Util.runCommands(User.getInstance(player),
                        replacePlaceholders(player, oldPhaseName, phase.getBlockNumber(), i, oldPhase.getEndCommands()),
                        "Commands run for end of " + oldPhaseName);
            });
            // Set the phase name
            is.setPhaseName(phaseName);
            if (player != null) {
                player.sendTitle(phaseName, null, -1, -1, -1);
            }
            // Run phase start commands
            Util.runCommands(User.getInstance(player),
                    replacePlaceholders(player, phaseName, phase.getBlockNumber(), i, phase.getStartCommands()),
                    "Commands run for start of " + phaseName);
            saveIsland(i);
            return true;
        }
        return false;
    }

    /**
     * Replaces placeholders in commands.
     * <pre>
     * [island] - Island name
     * [owner] - Island owner's name
     * [player] - The name of the player who broke the block triggering the commands
     * [phase] - the name of this phase
     * [blocks] - the number of blocks broken
     * [level] - island level (Requires Levels Addon)
     * [bank-balance] - island bank balance (Requires Bank Addon)
     * [eco-balance] - player's economy balance (Requires Vault and an economy plugin)
     * </pre>
     *
     * @param player      - player
     * @param phaseName   - phase name
     * @param phaseNumber - phase's block number
     * @param i           - island
     * @param commands    - list of commands
     * @return list of commands with placeholders replaced
     */
    @NonNull
    List<String> replacePlaceholders(@Nullable Player player, @NonNull String phaseName, @NonNull String phaseNumber, @NonNull Island i, List<String> commands) {
        return commands.stream()
                .map(c -> {
                    long level = addon.getAddonByName("Level").map(l -> ((Level) l).getIslandLevel(addon.getOverWorld(), i.getOwner())).orElse(0L);
                    double balance = addon.getAddonByName("Bank").map(b -> ((Bank) b).getBankManager().getBalance(i).getValue()).orElse(0D);
                    double ecoBalance = addon.getPlugin().getVault().map(v -> v.getBalance(User.getInstance(player), addon.getOverWorld())).orElse(0D);

                    return c.replace("[island]", i.getName() == null ? "" : i.getName())
                            .replace("[owner]", addon.getPlayers().getName(i.getOwner()))
                            .replace("[phase]", phaseName)
                            .replace("[blocks]", phaseNumber)
                            .replace("[level]", String.valueOf(level))
                            .replace("[bank-balance]", String.valueOf(balance))
                            .replace("[eco-balance]", String.valueOf(ecoBalance));

                })
                .map(c -> addon.getPlugin().getPlaceholdersManager().replacePlaceholders(player, c))
                .collect(Collectors.toList());
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

    private void breakBlock(@NonNull Cancellable e, @Nullable Player player, Block block, @NonNull World world, @NonNull OneBlockObject nextBlock, @NonNull Island i) {
        ItemStack tool = Objects.requireNonNull(player).getInventory().getItemInMainHand();
        if (addon.getSettings().isDropOnTop()) {
            breakBlockOnTop(e, player, block, world, nextBlock);
        } else {
            // Break normally and lift the player up so they don't fall
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> spawnBlock(nextBlock, block));
            if (player.getLocation().getBlock().equals(block)) {
                double delta = 1 - player.getLocation().getY() - block.getY();
                player.teleport(player.getLocation().add(new Vector(0, delta, 0)));
            } else if (player.getLocation().getBlock().equals(block.getRelative(BlockFace.UP))) {
                player.teleport(player.getLocation());
            }
        }
        // Fire event
        Bukkit.getPluginManager().callEvent(new MagicBlockEvent(i, player.getUniqueId(), tool, block, nextBlock.getMaterial()));
    }

    private void breakBlockOnTop(@NonNull Cancellable e, @Nullable Player player, @NonNull Block block, @NonNull World world, @NonNull OneBlockObject nextBlock) {
        e.setCancelled(true);
        ItemStack tool = Objects.requireNonNull(player).getInventory().getItemInMainHand();
        if (addon.getSettings().isDropOnTop()) {
            // Drop the drops
            dropItemStacks(block.getDrops(tool, player), block, world);
            // Drop the contents of inventory
            if (block.getState() instanceof InventoryHolder ih) {
                dropItemStacks(Arrays.asList(ih.getInventory().getContents()), block, world);
            }
            // Set the air
            block.setType(Material.AIR);
        } else {
            block.breakNaturally(tool);
        }
        // Give exp
        Objects.requireNonNull(player).giveExp(((BlockBreakEvent) e).getExpToDrop());
        // Damage tool
        damageTool(Objects.requireNonNull(player));
        spawnBlock(nextBlock, block);
    }

    private void dropItemStacks(Collection<ItemStack> drops, @NonNull Block block, @NonNull World world) {
        drops.stream()
        .filter(Objects::nonNull)
        .filter(item -> !item.getType().equals(Material.AIR))
        .forEach(item -> world.dropItem(block.getRelative(BlockFace.UP).getLocation()
                .add(new Vector(0.5, 0, 0.5)), item)
                .setVelocity(new Vector(0, 0, 0)));

    }

    private void spawnBlock(@NonNull OneBlockObject nextBlock, @NonNull Block block) {
        @NonNull
        Material type = nextBlock.getMaterial();
        // Place new block with no physics
        block.setType(type, false);
        // Fill the chest
        if (type.equals(Material.CHEST) && nextBlock.getChest() != null) {
            fillChest(nextBlock, block);
            return;
        }
        if (Tag.LEAVES.isTagged(type)) {
            Leaves leaves = (Leaves) block.getState().getBlockData();
            leaves.setPersistent(true);
            block.setBlockData(leaves);
        }
    }

    private void spawnEntity(@NonNull OneBlockObject nextBlock, @NonNull Block block) {
        if (block.isEmpty()) block.setType(Material.STONE);
        Location spawnLoc = block.getLocation().add(new Vector(0.5D, 1D, 0.5D));
        Entity entity = block.getWorld().spawnEntity(spawnLoc, nextBlock.getEntityType());
        // Make space for entity - this will blot out blocks
        makeSpace(entity);
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
    }

    private void makeSpace(@NonNull Entity e) {
        World world = e.getWorld();
        List<Block> airBlocks = new ArrayList<>();
        List<Block> waterBlocks = new ArrayList<>();
        // Make space for entity based on the entity's size
        BoundingBox bb = e.getBoundingBox();
        for (double x = bb.getMinX(); x <= bb.getMaxX() + 1; x++) {
            for (double z = bb.getMinZ(); z <= bb.getMaxZ() + 1; z++) {
                double y = bb.getMinY();
                Block b = world.getBlockAt(new Location(world, x, y, z));
                for (; y <= Math.min(bb.getMaxY() + 1, world.getMaxHeight()); y++) {
                    b = world.getBlockAt(new Location(world, x, y, z));
                    if (!b.getType().equals(Material.AIR) && !b.isLiquid()) {
                        airBlocks.add(b);
                    }
                    if (WATER_ENTITIES.contains(e.getType()) && addon.getSettings().isWaterMobProtection()) {
                        waterBlocks.add(b);
                    }
                }
                // Add air block on top for all water entities (required for dolphin, okay for others)
                if (WATER_ENTITIES.contains(e.getType())) {
                    airBlocks.add(b.getRelative(BlockFace.UP));
                }
            }
        }
        // Fire event
        BlockClearEvent event = new BlockClearEvent(e, airBlocks, waterBlocks);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        // Clear blocks
        airBlocks.forEach(Block::breakNaturally);
        airBlocks.forEach(b -> b.setType(Material.AIR));
        waterBlocks.forEach(b -> b.setType(Material.WATER));
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
        block.getWorld().spawnParticle(Particle.REDSTONE, block.getLocation().add(new Vector(0.5, 1.0, 0.5)), 50, 0.5, 0, 0.5, 1, new Particle.DustOptions(color, 1));
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

    private void damageTool(@NonNull Player player) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = inHand.getItemMeta();

        if (itemMeta instanceof Damageable meta && !itemMeta.isUnbreakable() && TOOLS.containsKey(inHand.getType())) {
            // Get the item's current durability
            Integer durability = meta.getDamage();
            // Get the damage this will do
            int damage = TOOLS.get(inHand.getType());
            if (durability != null) {
                // Check for DURABILITY
                if (itemMeta.hasEnchant(Enchantment.DURABILITY)) {
                    int level = itemMeta.getEnchantLevel(Enchantment.DURABILITY);
                    if (random.nextInt(level + 1) == 0) {
                        meta.setDamage(durability + damage);
                    }
                } else {
                    meta.setDamage(durability + damage);
                }
                if (meta.getDamage() > inHand.getType().getMaxDurability()) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                    player.getInventory().setItemInMainHand(null);
                } else {
                    inHand.setItemMeta(itemMeta);
                }
            }
        }

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
     * @return CompletableFuture - true if saved or not in cache, false if save failed
     */
    public CompletableFuture<Boolean> saveIsland(@NonNull Island island) {
        if (cache.containsKey(island.getUniqueId())) {
            return handler.saveObjectAsync(cache.get(island.getUniqueId()));
        }
        return CompletableFuture.completedFuture(true);
    }

}
