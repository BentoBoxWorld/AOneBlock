package world.bentobox.oneblock.commands;

import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.oneblock.OneBlock;
import world.bentobox.oneblock.oneblocks.OneBlockPhase;

public class AdminSanityCheck extends CompositeCommand {

    private OneBlock addon;
    private OneBlockPhase phase;

    public AdminSanityCheck(CompositeCommand islandCommand) {
        super(islandCommand, "sanity");
    }

    @Override
    public void setup() {
        setParametersHelp("oneblock.commands.admin.sanity.parameters");
        setDescription("oneblock.commands.admin.sanity.description");
        // Permission
        setPermission("admin.sanity");
        addon = (OneBlock)getAddon();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // No args
        if (args.size() > 1) {
            showHelp(this, user);
            return false;
        }
        if (args.isEmpty()) return true;
        // Check phase
        Optional<OneBlockPhase> opPhase = addon.getOneBlockManager().getPhase(args.get(0).toUpperCase());
        if (!opPhase.isPresent()) {
            user.sendMessage("oneblock.commands.admin.setchest.unknown-phase");
            return false;
        } else {
            phase = opPhase.get();
        }

        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.isEmpty()) {
            addon.getOneBlockManager().getAllProbs();
        } else {
            addon.getOneBlockManager().getProbs(phase);
        }
        if (user.isPlayer()) {
            user.sendMessage("oneblock.commands.admin.setchest.see-console");
        }
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        if (args.size() == 2) {
            // Get a list of phases
            return Optional.of(addon.getOneBlockManager().getPhaseList());
        }
        // Return nothing
        return Optional.empty();
    }
}
