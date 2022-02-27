package world.bentobox.aoneblock.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.panels.PhasesPanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;

public class IslandPhasesCommand extends CompositeCommand {

    private AOneBlock addon;

    public IslandPhasesCommand(CompositeCommand islandCommand) {
        super(islandCommand, "phases");
    }

    @Override
    public void setup() {
        setDescription("aoneblock.commands.phases.description");
        setOnlyPlayer(true);
        // Permission
        setPermission("phases");
        addon = getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        PhasesPanel.openPanel(this.addon, this.getWorld(), user);
        return true;
    }
}
