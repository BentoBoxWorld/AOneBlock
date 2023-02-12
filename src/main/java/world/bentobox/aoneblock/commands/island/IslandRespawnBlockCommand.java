package world.bentobox.aoneblock.commands.island;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;


/**
 * This command checks if center block is AIR or BEDROCK and in such situation it triggers BlockBreakEvent.
 */
public class IslandRespawnBlockCommand extends CompositeCommand
{
    /**
     * Instantiates a new Island respawn block command.
     *
     * @param islandCommand the island command
     */
    public IslandRespawnBlockCommand(CompositeCommand islandCommand, String label, String[] aliases)
    {
        super(islandCommand, label, aliases);
    }


    @Override
    public void setup()
    {
        this.setDescription("aoneblock.commands.respawn-block.description");
        this.setOnlyPlayer(true);
        // Permission
        this.setPermission("respawn-block");
    }


    @Override
    public boolean canExecute(User user, String label, List<String> args)
    {
        if (!Util.sameWorld(getWorld(), user.getWorld()))
        {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }

        Island island = this.getIslands().getIsland(this.getWorld(), user);

        if (island == null)
        {
            user.sendMessage("general.errors.no-island");
            return false;
        }

        return true;
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        Island island = this.getIslands().getIsland(this.getWorld(), user);

        if (island == null)
        {
            // Hmm, lost island so fast. Well, no, just idea null-pointer check bypass.
            user.sendMessage("general.errors.no-island");
        }
        else if (Material.BEDROCK.equals(island.getCenter().getBlock().getType()) ||
            Material.AIR.equals(island.getCenter().getBlock().getType()))
        {
            // Trigger manual block break event.
            Bukkit.getServer().getPluginManager().callEvent(
                new BlockBreakEvent(island.getCenter().getBlock(), user.getPlayer()));

            user.sendMessage("aoneblock.commands.respawn-block.block-respawned");
        }
        else
        {
            // Spawn 6 particles where block is located.
            island.getWorld().spawnParticle(Particle.REDSTONE,
                island.getCenter().add(new Vector(0.5, 1.0, 0.5)),
                5, 0.1, 0, 0.1, 1,
                new Particle.DustOptions(Color.fromBGR(0, 100, 0), 1));
            island.getWorld().spawnParticle(Particle.REDSTONE,
                island.getCenter().add(new Vector(1.0, 0.5, 0.5)),
                5, 0.1, 0, 0.1, 1,
                new Particle.DustOptions(Color.fromBGR(0, 100, 0), 1));
            island.getWorld().spawnParticle(Particle.REDSTONE,
                island.getCenter().add(new Vector(0.5, 0.5, 1.0)),
                5, 0.1, 0, 0.1, 1,
                new Particle.DustOptions(Color.fromBGR(0, 100, 0), 1));
            island.getWorld().spawnParticle(Particle.REDSTONE,
                island.getCenter().add(new Vector(0.5, 0.0, 0.5)),
                5, 0.1, 0, 0.1, 1,
                new Particle.DustOptions(Color.fromBGR(0, 100, 0), 1));
            island.getWorld().spawnParticle(Particle.REDSTONE,
                island.getCenter().add(new Vector(0.0, 0.5, 0.5)),
                5, 0.1, 0, 0.1, 1,
                new Particle.DustOptions(Color.fromBGR(0, 100, 0), 1));
            island.getWorld().spawnParticle(Particle.REDSTONE,
                island.getCenter().add(new Vector(0.5, 0.5, 0.0)),
                5, 0.1, 0, 0.1, 1,
                new Particle.DustOptions(Color.fromBGR(0, 100, 0), 1));

            user.sendMessage("aoneblock.commands.respawn-block.block-exist");
        }

        return true;
    }
}
