package world.bentobox.aoneblock.commands.admin;

import java.util.List;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.panels.AdminPhasesPanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Command to open the phase order editor
 *
 * @author tastybento
 */
public class AdminPhasesCommand extends CompositeCommand {

    private AOneBlock addon;

    public AdminPhasesCommand(CompositeCommand parent) {
        super(parent, "phases");
    }

    @Override
    public void setup() {
        setDescription("aoneblock.commands.admin.phases.description");
        // Permission
        setPermission("admin.phases");
        setOnlyPlayer(true);
        addon = getAddon();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!args.isEmpty()) {
            showHelp(this, user);
            return false;
        }
        if (addon.getOneBlockManager().getPhaseIndex().isEmpty()) {
            // Phases were loaded without an index, so there is nothing to reorder
            user.sendMessage("aoneblock.commands.admin.phases.no-index");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        AdminPhasesPanel.openPanel(addon, user);
        return true;
    }
}
