package world.bentobox.aoneblock.listeners;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.MobAspects;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandResettedEvent;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public class BlockListener implements Listener {

    private final AOneBlock addon;
    private OneBlocksManager oneBlocksManager;
    private final Database<OneBlockIslands> handler;
    private final Map<String, OneBlockIslands> cache;
    private final Random random = new Random();

    /**
     * Tools that take damage. See https://minecraft.gamepedia.com/Item_durability#Tool_durability
     */
    private final static Map<Material, Integer> TOOLS;
    static {
        Map<Material, Integer> t = new HashMap<>();
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
    private final static List<EntityType> WATER_ENTITIES = Arrays.asList(
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            EntityType.SQUID,
            EntityType.COD,
            EntityType.SALMON,
            EntityType.PUFFERFISH,
            EntityType.TROPICAL_FISH,
            EntityType.DROWNED,
            EntityType.DOLPHIN);

    private static final Map<EntityType, MobAspects> MOB_ASPECTS;
    public static final int MAX_LOOK_AHEAD = 5;
    static {
        Map<EntityType, MobAspects> m = new HashMap<>();
        m.put(EntityType.ZOMBIE, new MobAspects(Sound.ENTITY_ZOMBIE_AMBIENT, Color.fromRGB(74, 99, 53)));
        m.put(EntityType.CREEPER, new MobAspects(Sound.ENTITY_CREEPER_PRIMED, Color.fromRGB(125, 255, 106)));
        m.put(EntityType.SKELETON, new MobAspects(Sound.ENTITY_SKELETON_AMBIENT, Color.fromRGB(211, 211, 211)));
        m.put(EntityType.DROWNED, new MobAspects(Sound.ENTITY_DROWNED_AMBIENT, Color.fromRGB(109, 152, 144)));
        m.put(EntityType.BLAZE, new MobAspects(Sound.ENTITY_BLAZE_AMBIENT, Color.fromRGB(238, 211, 91)));
        m.put(EntityType.CAVE_SPIDER, new MobAspects(Sound.ENTITY_SPIDER_AMBIENT, Color.fromRGB(63, 37, 31)));
        m.put(EntityType.SPIDER, new MobAspects(Sound.ENTITY_SPIDER_AMBIENT, Color.fromRGB(94, 84, 73)));
        m.put(EntityType.EVOKER, new MobAspects(Sound.ENTITY_EVOKER_AMBIENT, Color.fromRGB(144, 148, 148)));
        m.put(EntityType.GHAST, new MobAspects(Sound.ENTITY_GHAST_AMBIENT, Color.fromRGB(242, 242, 242)));
        m.put(EntityType.HUSK, new MobAspects(Sound.ENTITY_HUSK_AMBIENT, Color.fromRGB(111, 104, 90)));
        m.put(EntityType.ILLUSIONER, new MobAspects(Sound.ENTITY_ILLUSIONER_AMBIENT, Color.fromRGB(144, 149, 149)));
        m.put(EntityType.RAVAGER, new MobAspects(Sound.ENTITY_RAVAGER_AMBIENT, Color.fromRGB(85, 78, 73)));
        m.put(EntityType.SHULKER, new MobAspects(Sound.ENTITY_SHULKER_AMBIENT, Color.fromRGB(142, 106, 146)));
        m.put(EntityType.VEX, new MobAspects(Sound.ENTITY_VEX_AMBIENT, Color.fromRGB(137, 156, 176)));
        m.put(EntityType.WITCH, new MobAspects(Sound.ENTITY_WITCH_AMBIENT, Color.fromRGB(56, 39, 67)));
        m.put(EntityType.STRAY, new MobAspects(Sound.ENTITY_STRAY_AMBIENT, Color.fromRGB(118, 132, 135)));
        m.put(EntityType.GUARDIAN, new MobAspects(Sound.ENTITY_GUARDIAN_AMBIENT, Color.fromRGB(201, 143, 113)));
        m.put(EntityType.ELDER_GUARDIAN, new MobAspects(Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, Color.fromRGB(201, 143, 113)));
        MOB_ASPECTS = Collections.unmodifiableMap(m);
    }

    /**
     * @param addon - OneBlock
     * @throws InvalidConfigurationException - exception
     * @throws IOException - exception
     * @throws FileNotFoundException - exception
     */
    public BlockListener(AOneBlock addon) throws FileNotFoundException, IOException, InvalidConfigurationException {
        this.addon = addon;
        handler = new Database<>(addon, OneBlockIslands.class);
        cache = new HashMap<>();
        oneBlocksManager = addon.getOneBlockManager();
    }

    /**
     * Save the island cache
     */
    public void saveCache() {
        cache.values().forEach(handler::saveObject);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandCreatedEvent e) {
        setUp(e.getIsland());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        cache.remove(e.getIsland().getUniqueId());
        handler.deleteID(e.getIsland().getUniqueId());
    }

    private void setUp(Island island) {
        // Set the bedrock to the initial block
        island.getCenter().getBlock().setType(Material.GRASS_BLOCK);
        // Create a database entry
        OneBlockIslands is = new OneBlockIslands(island.getUniqueId());
        cache.put(island.getUniqueId(), is);
        handler.saveObject(is);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandResettedEvent e) {
        setUp(e.getIsland());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> process(e, i, e.getPlayer()));
    }

    /**
     * Prevent entities other than players changing the magic block
     * @param e - EntityChangeBlockEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockChange(final EntityChangeBlockEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> e.setCancelled(true));
    }

    /**
     * Blocks oneblocks from being blown up
     * @param e - EntityExplodeEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(final EntityExplodeEvent e) {
        if (!addon.inWorld(e.getEntity().getWorld())) {
            return;
        }
        e.blockList().removeIf(b -> addon.getIslands().getIslandAt(b.getLocation()).filter(i -> b.getLocation().equals(i.getCenter())).isPresent());
    }

    /**
     * Check for water grabbing
     * @param e - event (note that you cannot register PlayerBucketEvent)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(final PlayerBucketFillEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> process(e, i, e.getPlayer()));
    }

    private void process(Cancellable e, Island i, @NonNull Player player) {
        // Get island from cache or load it
        OneBlockIslands is = getIsland(i);
        // Get the phase for this island
        OneBlockPhase phase = oneBlocksManager.getPhase(is.getBlockNumber());
        // Check for a goto
        if (phase.getGotoBlock() != null) {
            int gotoBlock = phase.getGotoBlock();
            phase = oneBlocksManager.getPhase(gotoBlock);
            // Store lifetime
            is.setLifetime(is.getLifetime() + is.getBlockNumber());
            // Set current block
            is.setBlockNumber(gotoBlock);

        }
        // Announce the phase
        boolean newPhase = false;
        if (!is.getPhaseName().equalsIgnoreCase(phase.getPhaseName())) {
            cache.get(i.getUniqueId()).setPhaseName(phase.getPhaseName());
            player.sendTitle(phase.getPhaseName(), null, -1, -1, -1);
            newPhase = true;
        }
        // Get the block that is being broken
        Block block = i.getCenter().toVector().toLocation(player.getWorld()).getBlock();
        // Fill a 5 block queue
        if (is.getQueue().isEmpty() || newPhase) {
            is.clearQueue();
            // Add initial 5 blocks
            for (int j = 0; j < MAX_LOOK_AHEAD; j++) {
                is.add(phase.getNextBlock());
            }
        }
        // Play warning sound for upcoming mobs
        if (addon.getSettings().getMobWarning() > 0) {
            List<EntityType> opMob = is.getNearestMob(addon.getSettings().getMobWarning());
            opMob.stream().filter(MOB_ASPECTS::containsKey).map(MOB_ASPECTS::get).forEach(s -> {
                block.getWorld().playSound(block.getLocation(), s.getSound(), 1F, 1F);
                block.getWorld().spawnParticle(Particle.REDSTONE, block.getLocation().add(new Vector(0.5, 1.0, 0.5)), 10, 0.5, 0, 0.5, 1, new Particle.DustOptions(s.getColor(), 1));
            });
        }
        // Get the next block
        OneBlockObject nextBlock = newPhase && phase.getFirstBlock() != null ? phase.getFirstBlock() : is.pollAndAdd(phase.getNextBlock());
        // Set the biome for the block and one block above it
        if (newPhase) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    for (int y = -4; y <= 4; y++) {
                        block.getWorld().setBiome(block.getX() + x, block.getY() + y, block.getZ() + z, phase.getPhaseBiome());
                    }
                }
            }
        }
        // Entity
        if (nextBlock.isEntity()) {
            e.setCancelled(true);
            // Entity spawns do not increment the block number or break the block
            spawnEntity(nextBlock, block);
            return;
        }
        // Break the block
        if (e instanceof BlockBreakEvent) {
            e.setCancelled(true);
            block.breakNaturally(player.getInventory().getItemInMainHand());
            // Give exp
            player.giveExp(((BlockBreakEvent)e).getExpToDrop());
            // Damage tool
            damageTool(player, block);
            spawnBlock(nextBlock, block);
        } else if (e instanceof PlayerBucketFillEvent) {
            Bukkit.getScheduler().runTask(addon.getPlugin(), ()-> spawnBlock(nextBlock, block));
        }
        // Increment the block number
        is.incrementBlockNumber();
    }

    private void spawnBlock(OneBlockObject nextBlock, Block block) {
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
            Leaves leaves = (Leaves)block.getState().getBlockData();
            leaves.setPersistent(true);
            block.setBlockData(leaves);
        }
    }

    private void spawnEntity(OneBlockObject nextBlock, Block block) {
        if (block.isEmpty()) block.setType(Material.STONE);
        Location spawnLoc = block.getLocation().add(new Vector(0.5D, 1D, 0.5D));
        Entity entity = block.getWorld().spawnEntity(spawnLoc, nextBlock.getEntityType());
        // Make space for entity - this will blot out blocks
        if (entity != null) {
            makeSpace(entity);
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
        } else {
            addon.logWarning("Could not spawn entity at " + spawnLoc);
        }
    }

    private void makeSpace(Entity e) {
        World world = e.getWorld();
        // Make space for entity based on the entity's size
        BoundingBox bb = e.getBoundingBox();
        for (double x = bb.getMinX(); x <= bb.getMaxX() + 1; x++) {
            for (double z = bb.getMinZ(); z <= bb.getMaxZ() + 1; z++) {
                double y = bb.getMinY();
                Block b = world.getBlockAt(new Location(world, x,y,z));
                for (; y <= Math.min(bb.getMaxY() + 1, world.getMaxHeight()); y++) {
                    b = world.getBlockAt(new Location(world, x,y,z));
                    if (!b.getType().equals(Material.AIR) && !b.isLiquid()) b.breakNaturally();
                    b.setType(WATER_ENTITIES.contains(e.getType()) ? Material.WATER : Material.AIR, false);
                }
                // Add air block on top for all water entities (required for dolphin, okay for others)
                if (WATER_ENTITIES.contains(e.getType())) {
                    b.getRelative(BlockFace.UP).setType(Material.AIR);
                }
            }
        }
    }

    private void fillChest(OneBlockObject nextBlock, Block block) {
        Chest chest = (Chest)block.getState();
        nextBlock.getChest().forEach(chest.getBlockInventory()::setItem);
        Color color = Color.fromBGR(0,255,255); // yellow
        switch (nextBlock.getRarity()) {
        case EPIC:
            color = Color.fromBGR(255,0,255); // magenta
            break;
        case RARE:
            color = Color.fromBGR(255,255,255); // cyan
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
     * @param i - island
     * @return one block island
     */
    @NonNull
    public OneBlockIslands getIsland(Island i) {
        return cache.containsKey(i.getUniqueId()) ? cache.get(i.getUniqueId()) : loadIsland(i.getUniqueId());
    }

    private void damageTool(@NonNull Player player, Block block) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = inHand.getItemMeta();

        if (itemMeta instanceof Damageable && !itemMeta.isUnbreakable() && TOOLS.containsKey(inHand.getType())) {
            Damageable meta = (Damageable) itemMeta;
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
    private OneBlockIslands loadIsland(String uniqueId) {
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Block block = e.getBlock();
        Location l = block.getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i ->
        block.getWorld().spawnParticle(Particle.REDSTONE, l.add(new Vector(0.5, 1.0, 0.5)), 5, 0.1, 0, 0.1, 1, new Particle.DustOptions(Color.fromBGR(0,100,0), 1)));
    }
    /*
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {

    } */
}
