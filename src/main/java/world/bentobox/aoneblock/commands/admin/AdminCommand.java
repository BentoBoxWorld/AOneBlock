package world.bentobox.aoneblock.commands.admin;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.admin.DefaultAdminCommand;

public class AdminCommand extends DefaultAdminCommand {

    public AdminCommand(GameModeAddon addon) {
        super(addon);
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.admin.DefaultAdminCommand#setup()
     */
    @Override
    public void setup() {
        super.setup();
        // Set count
        new AdminSetCountCommand(this);
        // Set chest
        new AdminSetChestCommand(this);
        // Sanity
        new AdminSanityCheck(this);
    }

}
