package world.bentobox.aoneblock.commands.island;

import java.util.List;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

public class IslandActionBarCommand extends CompositeCommand {

    private AOneBlock addon;

    public IslandActionBarCommand(CompositeCommand islandCommand, String label, String[] aliases)
    {
        super(islandCommand, label, aliases);
    }

    @Override
    public void setup() {
        setDescription("aoneblock.commands.island.actionbar.description");
        setOnlyPlayer(true);
        // Permission
        setPermission("island.actionbar");
        addon = getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        addon.getBossBar().toggleUser(user);
        getIslands().getIslandAt(user.getLocation()).ifPresent(i -> {
            if (!i.isAllowed(addon.ONEBLOCK_ACTIONBAR)) {
                user.sendMessage("aoneblock.actionbar.not-active");
            }
        });
        return true;
    }
}
