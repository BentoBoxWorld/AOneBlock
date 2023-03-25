package world.bentobox.aoneblock;

import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

@Plugin(name="AOneBlock", version="1.0")
@ApiVersion(ApiVersion.Target.v1_17)
public class AOneBlockPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new AOneBlock();
    }
}
