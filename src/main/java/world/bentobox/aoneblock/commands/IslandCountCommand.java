package world.bentobox.aoneblock.commands;

import java.util.List;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

public class IslandCountCommand extends CompositeCommand {

    private AOneBlock addon;

    public IslandCountCommand(CompositeCommand islandCommand) {
        super(islandCommand, "count");
    }

    @Override
    public void setup() {
        setDescription("oneblock.commands.count.description");
        setOnlyPlayer(true);
        // Permission
        setPermission("count");
        addon = (AOneBlock)getAddon();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!Util.getWorld(user.getWorld()).equals(getWorld())) {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }
        if (!getIslands().locationIsOnIsland(user.getPlayer(), user.getLocation())) {
            user.sendMessage("commands.island.sethome.must-be-on-your-island");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        getIslands().getProtectedIslandAt(user.getLocation()).ifPresent(island -> {
            OneBlockIslands i = addon.getOneBlocksIsland(island);
            user.sendMessage("oneblock.commands.count.info", TextVariables.NUMBER, String.valueOf(i.getBlockNumber()), TextVariables.NAME, i.getPhaseName());
        });
        return true;
    }
}
