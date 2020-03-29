package world.bentobox.oneblock.listeners;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.events.island.IslandEvent.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandResettedEvent;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.oneblock.OneBlock;
import world.bentobox.oneblock.dataobjects.OneBlockIslands;

/**
 * @author tastybento
 *
 */
public class BlockListener implements Listener {

    private final OneBlock addon;
    private OneBlocks oneBlocks;
    private final Database<OneBlockIslands> handler;
    private final Map<String, OneBlockIslands> cache;

    /**
     * @param addon - OneBlock
     * @throws InvalidConfigurationException - exception
     * @throws IOException - exception
     * @throws FileNotFoundException - exception
     */
    public BlockListener(OneBlock addon) throws FileNotFoundException, IOException, InvalidConfigurationException {
        this.addon = addon;
        handler = new Database<>(addon, OneBlockIslands.class);
        cache = new HashMap<>();
        oneBlocks = new OneBlocks(addon);
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
    public void onBlockBreak(BlockBreakEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> process(e, i, e.getPlayer()));
    }

    private void process(BlockBreakEvent e, Island i, @NonNull Player player) {
        e.setCancelled(true);
        // Get island from cache
        OneBlockIslands is = cache.containsKey(i.getUniqueId()) ? cache.get(i.getUniqueId()) : cache.computeIfAbsent(i.getUniqueId(), OneBlockIslands::new);
        // Get the phase for this island
        OneBlockPhase phase = oneBlocks.getPhase(is.getBlockNumber());
        // Announce the phase
        if (!is.getPhaseName().equalsIgnoreCase(phase.getPhaseName())) {
            cache.get(i.getUniqueId()).setPhaseName(phase.getPhaseName());
            player.sendTitle(phase.getPhaseName(), null, -1, -1, -1);
        }
        // Get the next block
        OneBlockObject nextBlock = phase.getNextBlock();
        // Get the block that is being broken
        Block block = i.getCenter().toVector().toLocation(player.getWorld()).getBlock();
        // Set the biome
        if (block.getWorld().getEnvironment().equals(Environment.NORMAL)) {
            block.setBiome(phase.getPhaseBiome());
        }
        // Entity
        if (nextBlock.isEntity()) {
            Location spawnLoc = i.getCenter().toVector().add(new Vector(0.5D, 1D, 0.5D)).toLocation(player.getWorld());
            block.getWorld().spawnEntity(spawnLoc, nextBlock.getEntityType());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
            // Entity spawns do not increment the block number
            return;
        }
        // Break the block
        block.breakNaturally();
        player.giveExp(e.getExpToDrop());
        // TODO Damage tool
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand instanceof Damageable) {
            Damageable meta = (Damageable) inHand.getItemMeta();
            Integer damage = meta.getDamage();
            if (damage != null) {
                meta.setDamage(damage + 1);
            }
        }
        @NonNull
        Material type = nextBlock.getMaterial();
        // Place new block with no physics
        block.setType(type, false);
        // Fill the chest
        if (type.equals(Material.CHEST)) {
            Chest chest = (Chest)block.getState();
            nextBlock.getChest().forEach(chest.getBlockInventory()::addItem);
        }
        // Increment the block number
        is.incrementBlockNumber();
    }

    /*
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> {
            e.setCancelled(true);
            process(i, e.getPlayer());
        });
    }*/
}
