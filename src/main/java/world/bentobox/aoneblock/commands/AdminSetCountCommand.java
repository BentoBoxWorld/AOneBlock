package world.bentobox.aoneblock.commands;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

public class AdminSetCountCommand extends CompositeCommand {

    private AOneBlock addon;

    public AdminSetCountCommand(CompositeCommand islandCommand) {
        super(islandCommand, "setcount");
    }

    @Override
    public void setup() {
        setParametersHelp("aoneblock.commands.admin.setcount.parameters");
        setDescription("aoneblock.commands.admin.setcount.description");
        // Permission
        setPermission("admin.setcount");
        addon = getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.size() < 2 || args.size() > 3 || (args.size() == 3 && !args.get(2).equalsIgnoreCase("lifetime"))) {
            showHelp(this, user);
            return false;
        }
        // Get value
        // Get new range
        if (!Util.isInteger(args.get(1), true) || Integer.parseInt(args.get(1)) < 0) {
            user.sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, args.get(1));
            return false;
        }
        int count = Integer.parseInt(args.get(1));
        // Get target player
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }
        // Get their island
        Island island = getIslands().getIsland(getWorld(), targetUUID);
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        OneBlockIslands i = addon.getOneBlocksIsland(island);
        if (args.size() == 3 && args.get(2).equalsIgnoreCase("lifetime")) {
            i.setLifetime(count);
            user.sendMessage("aoneblock.commands.admin.setcount.set-lifetime", TextVariables.NUMBER, String.valueOf(count), TextVariables.NAME, getPlayers().getName(targetUUID));
        } else {
            i.setBlockNumber(count);
            i.clearQueue();
            user.sendMessage("aoneblock.commands.admin.setcount.set", TextVariables.NUMBER, String.valueOf(count), TextVariables.NAME, getPlayers().getName(targetUUID));
        }
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        // Return the player names
        return args.size() == 2 ? Optional.of(Util.getOnlinePlayerList(user)) : Optional.empty();
    }
}
