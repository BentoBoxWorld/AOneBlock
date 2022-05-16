package world.bentobox.aoneblock.commands.island;

import java.util.List;
import java.util.Objects;

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
        setDescription("aoneblock.commands.count.description");
        setOnlyPlayer(true);
        // Permission
        setPermission("count");
        addon = getAddon();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!Util.sameWorld(getWorld(), user.getWorld())) {
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
        getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation())).ifPresent(island -> {
            OneBlockIslands i = addon.getOneBlocksIsland(island);
            user.sendMessage("aoneblock.commands.count.info", TextVariables.NUMBER, String.valueOf(i.getBlockNumber()), TextVariables.NAME, i.getPhaseName());
        });
        return true;
    }
}
