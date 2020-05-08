package world.bentobox.aoneblock.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;

import world.bentobox.aoneblock.AOneBlock;
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
        addon = (AOneBlock)getAddon();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        PanelBuilder pb = new PanelBuilder()
                .user(user)
                .name(user.getTranslation("aoneblock.commands.phases.title"));
        List<PanelItem> items = addon.getOneBlockManager().getBlockProbs().entrySet().stream()
                .filter(en -> !en.getValue().isGotoPhase())
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(en -> {
                    PanelItemBuilder item = new PanelItemBuilder();
                    item.name(user.getTranslation("aoneblock.commands.phases.name-syntax",
                            TextVariables.NAME, en.getValue().getPhaseName(),
                            TextVariables.NUMBER, String.valueOf(en.getKey())));
                    Material material = en.getValue().getFirstBlock() == null ? Material.STONE : en.getValue().getFirstBlock().getMaterial();
                    item.icon(material);
                    item.description(user.getTranslation("aoneblock.commands.phases.description-syntax",
                            TextVariables.NAME, en.getValue().getPhaseName(),
                            TextVariables.NUMBER, String.valueOf(en.getKey())));
                    return item.build();
                }).collect(Collectors.toList());
        items.forEach(pb::item);
        pb.build();
        return true;
    }
}
