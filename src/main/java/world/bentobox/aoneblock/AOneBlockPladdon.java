package world.bentobox.aoneblock;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class AOneBlockPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new AOneBlock();
    }
}
