package world.bentobox.oneblock.commands;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.oneblock.OneBlock;
import world.bentobox.oneblock.dataobjects.OneBlockIslands;

public class AdminSetCountCommand extends CompositeCommand {

    private OneBlock addon;
    private @Nullable UUID targetUUID;
    private @Nullable Island island;
    private Integer count;

    public AdminSetCountCommand(CompositeCommand islandCommand) {
        super(islandCommand, "setcount");
    }

    @Override
    public void setup() {
        setParametersHelp("oneblock.commands.admin.setcount.parameters");
        setDescription("oneblock.commands.admin.setcount.description");
        // Permission
        setPermission("admin.setcount");
        addon = (OneBlock)getAddon();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.size() != 2) {
            showHelp(this, user);
            return false;
        }
        // Get value
        // Get new range
        if (!Util.isInteger(args.get(1), true) || Integer.parseInt(args.get(1)) < 0) {
            user.sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, args.get(1));
            return false;
        }
        count = Integer.parseInt(args.get(1));
        // Get target player
        targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }
        // Get their island
        island = getIslands().getIsland(getWorld(), targetUUID);
        return island != null;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        OneBlockIslands i = addon.getOneBlocksIsland(island);
        i.setBlockNumber(count);
        user.sendMessage("oneblock.commands.admin.setcount.set", TextVariables.NUMBER, String.valueOf(i.getBlockNumber()), TextVariables.NAME, getPlayers().getName(targetUUID));
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        // Return the player names
        return args.size() == 2 ? Optional.of(Util.getOnlinePlayerList(user)) : Optional.empty();
    }
}
