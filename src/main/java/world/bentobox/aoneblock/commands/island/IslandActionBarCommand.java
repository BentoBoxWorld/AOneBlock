package world.bentobox.aoneblock.commands.island;

import java.util.List;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.listeners.BossBarListener;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.metadata.MetaDataValue;
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
        getIslands().getIslandAt(user.getLocation()).ifPresent(i -> {
            if (!i.isAllowed(addon.ONEBLOCK_ACTIONBAR)) {
                user.sendMessage("aoneblock.actionbar.not-active");
            }
        });
        // Toggle state
        boolean newState = !user.getMetaData(BossBarListener.AONEBLOCK_ACTIONBAR).map(MetaDataValue::asBoolean).orElse(true);
        user.putMetaData(BossBarListener.AONEBLOCK_ACTIONBAR, new MetaDataValue(newState));
        if (newState) {
             user.sendMessage("aoneblock.commands.island.actionbar.status_on");
        } else {
            user.sendMessage("aoneblock.commands.island.actionbar.status_off");
        }
        return true;
    }
}
