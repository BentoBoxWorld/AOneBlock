package world.bentobox.aoneblock.commands;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * Enables a player to set their count to anything less than they
 * have done so far.
 * @author tastybento
 *
 */
public class IslandSetCountCommand extends CompositeCommand {

    private AOneBlock addon;

    public IslandSetCountCommand(CompositeCommand islandCommand) {
        super(islandCommand, "setcount");
    }

    @Override
    public void setup() {
        setParametersHelp("aoneblock.commands.island.setcount.parameters");
        setDescription("aoneblock.commands.island.setcount.description");
        // Permission
        setPermission("island.setcount");
        addon = getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.size() != 1) {
            showHelp(this, user);
            return false;
        }
        // Get their island
        Island island = getIslands().getIsland(getWorld(), user);
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        // Check ownership
        if (!getIslands().hasIsland(getWorld(), user)) {
            user.sendMessage("general.errors.not-owner");
            return false;
        }
        // Get value
        // Get new range
        if (!Util.isInteger(args.get(0), true) || Integer.parseInt(args.get(0)) < 0) {
            user.sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, args.get(0));
            return false;
        }
        int count = Integer.parseInt(args.get(0));
        // Check the value is lower than played so far
        @NonNull
        OneBlockIslands i = addon.getBlockListener().getIsland(island);
        long maxCount = i.getLifetime();
        if (count > maxCount) {
            user.sendMessage("aoneblock.commands.island.setcount.too-high", TextVariables.NUMBER, String.valueOf(maxCount));
            return false;
        }
        i.setBlockNumber(count);
        i.clearQueue();
        user.sendMessage("aoneblock.commands.island.setcount.set", TextVariables.NUMBER, String.valueOf(i.getBlockNumber()));
        return true;
    }

}
