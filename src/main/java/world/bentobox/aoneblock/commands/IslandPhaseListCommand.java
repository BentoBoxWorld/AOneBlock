package world.bentobox.aoneblock.commands;

import java.util.List;
import java.util.Map.Entry;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

public class IslandPhaseListCommand extends CompositeCommand {

    private AOneBlock addon;

    public IslandPhaseListCommand(CompositeCommand islandCommand) {
        super(islandCommand, "phaselist");
    }

    @Override
    public void setup() {
        setDescription("aoneblock.commands.phaselist.description");
        setOnlyPlayer(true);
        // Permission
        setPermission("phaselist");
        addon = (AOneBlock)getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        boolean noPhases = true;
        for (Entry<String, Integer> e : addon.getOneBlockManager().getPhaseMap().entrySet()) {
            if (noPhases) {
                noPhases = false;
                user.sendMessage("aoneblock.commands.phaselist.title");
            }
            user.sendMessage("aoneblock.commands.phaselist.syntax", TextVariables.NAME, e.getKey(), TextVariables.NUMBER, String.valueOf(e.getValue()));
        }
        if (noPhases) {
            user.sendMessage("aoneblock.commands.phaselist.no-phases");
        }
        return true;
    }
}
