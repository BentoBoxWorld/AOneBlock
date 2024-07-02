package world.bentobox.aoneblock;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class AOneBlockPladdon extends Pladdon {

    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new AOneBlock();
        }

        return addon;
    }
}
