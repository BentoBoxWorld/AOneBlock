package world.bentobox.aoneblock.commands.island;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.Settings;
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

        Settings settings = this.<AOneBlock>getAddon().getSettings();

        // Count
        new IslandCountCommand(this,
            settings.getCountCommand().split(" ")[0],
            settings.getCountCommand().split(" "));
        // Phase list
        new IslandPhasesCommand(this,
            settings.getPhasesCommand().split(" ")[0],
            settings.getPhasesCommand().split(" "));
        // Set Count
        new IslandSetCountCommand(this,
            settings.getSetCountCommand().split(" ")[0],
            settings.getSetCountCommand().split(" "));
        // Force block respawn
        new IslandRespawnBlockCommand(this,
            settings.getRespawnBlockCommand().split(" ")[0],
            settings.getRespawnBlockCommand().split(" "));
        // Boss bar
        new IslandBossBarCommand(this, settings.getBossBarCommand().split(" ")[0],
                settings.getBossBarCommand().split(" "));
    }
}
