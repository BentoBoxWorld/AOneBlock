package world.bentobox.aoneblock.commands.island;

import java.util.List;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

public class IslandBossBarCommand extends CompositeCommand {

    private AOneBlock addon;

    public IslandBossBarCommand(CompositeCommand islandCommand, String label, String[] aliases)
    {
        super(islandCommand, label, aliases);
    }

    @Override
    public void setup() {
        setDescription("aoneblock.commands.island.bossbar.description");
        setOnlyPlayer(true);
        // Permission
        setPermission("island.bossbar");
        addon = getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        addon.getBossBar().toggleUser(user);
        getIslands().getIslandAt(user.getLocation()).ifPresent(i -> {
            if (!i.isAllowed(addon.ONEBLOCK_BOSSBAR)) {
                user.sendMessage("aoneblock.bossbar.not-active");
            }
        });
        return true;
    }
}
