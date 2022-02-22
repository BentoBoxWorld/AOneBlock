package world.bentobox.aoneblock.commands;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.commands.island.DefaultPlayerCommand;

public class PlayerCommand extends DefaultPlayerCommand {

    public PlayerCommand(AOneBlock addon) {
        super(addon);
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.island.DefaultPlayerCommand#setup()
     */
    @Override
    public void setup() {
        super.setup();
        // Count
        new IslandCountCommand(this);
        // Phase list
        new IslandPhasesCommand(this);
        // Set Count
        new IslandSetCountCommand(this);
    }

}
