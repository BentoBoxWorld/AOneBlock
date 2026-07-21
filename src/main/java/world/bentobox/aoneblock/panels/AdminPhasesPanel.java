package world.bentobox.aoneblock.panels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.PhaseIndexEntry;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;

/**
 * Admin panel for editing the phase order. Clicking a phase picks it up - the
 * remaining phases shrink left to fill the gap. Clicking a phase in the row
 * shoves it and everything after it right and drops the held phase there;
 * clicking the slot after the last phase drops it at the end. Clicking
 * anywhere else, or closing the panel, puts the held phase back. Right-click
 * toggles a phase on or off. Shift-left-click asks for a new phase length in
 * chat. Changes are saved to the phase index and applied immediately.
 *
 * @author tastybento
 */
public class AdminPhasesPanel {

    private static final String REF = "aoneblock.commands.admin.phases.gui.";
    private static final String NAME_PLACEHOLDER = "[name]";
    private static final String NUMBER_PLACEHOLDER = "[number]";
    private static final int HAND_SLOT = 4;
    private static final int ROW_START = 9;
    private static final int PANEL_SIZE = 54;

    private final AOneBlock addon;
    private final User user;
    /**
     * Position in the phase index of the phase currently picked up, or -1.
     */
    private int heldIndex = -1;

    AdminPhasesPanel(AOneBlock addon, User user) {
        this.addon = addon;
        this.user = user;
    }

    /**
     * Opens the phase order panel for an admin.
     *
     * @param addon addon
     * @param user  admin viewing the panel
     */
    public static void openPanel(AOneBlock addon, User user) {
        new AdminPhasesPanel(addon, user).build();
    }

    private OneBlocksManager manager() {
        return addon.getOneBlockManager();
    }

    void build() {
        PanelBuilder pb = new PanelBuilder().user(user).size(PANEL_SIZE)
                .name(user.getTranslation(REF + "title"));
        List<PhaseIndexEntry> index = manager().getPhaseIndex();
        // Hand slot - the held phase, or how-to-use info
        pb.item(HAND_SLOT, heldIndex >= 0 ? handItem(index.get(heldIndex)) : infoItem());
        // Phase row - without the held phase, so the rest shrink to fill the gap
        List<PhaseIndexEntry> displayed = new ArrayList<>(index);
        PhaseIndexEntry held = heldIndex >= 0 ? displayed.remove(heldIndex) : null;
        int slot = ROW_START;
        int startBlock = 0;
        for (int i = 0; i < displayed.size() && slot < PANEL_SIZE; i++, slot++) {
            PhaseIndexEntry entry = displayed.get(i);
            pb.item(slot, phaseItem(entry, i, startBlock, held != null));
            if (manager().isPhaseAvailable(entry)) {
                startBlock += entry.getLength() > 0 ? entry.getLength() : OneBlocksManager.DEFAULT_PHASE_LENGTH;
            }
        }
        if (held != null && slot < PANEL_SIZE) {
            pb.item(slot++, dropAtEndItem(displayed.size()));
        }
        // Fillers catch clicks off the row and put a held phase back
        for (int i = 0; i < ROW_START; i++) {
            if (i != HAND_SLOT) {
                pb.item(i, fillerItem());
            }
        }
        while (slot < PANEL_SIZE) {
            pb.item(slot++, fillerItem());
        }
        pb.build();
    }

    private PanelItem phaseItem(PhaseIndexEntry entry, int displayIndex, int startBlock, boolean holding) {
        List<String> description = new ArrayList<>();
        boolean available = manager().isPhaseAvailable(entry);
        if (available) {
            description.add(user.getTranslation(REF + "start", NUMBER_PLACEHOLDER, String.valueOf(startBlock)));
        }
        description.add(user.getTranslation(REF + "length", NUMBER_PLACEHOLDER, String.valueOf(entry.getLength())));
        if (!entry.isEnabled()) {
            description.add(user.getTranslation(REF + "disabled"));
        } else if (!available) {
            description.add(user.getTranslation(REF + "version-locked", "[version]",
                    Objects.toString(entry.getRequiredMinecraftVersion(), "?")));
        }
        if (holding) {
            description.add(user.getTranslation(REF + "drop-here"));
        } else {
            description.add(user.getTranslation(REF + "pick-up"));
            description.add(user.getTranslation(REF + "toggle"));
            description.add(user.getTranslation(REF + "set-length"));
        }
        return new PanelItemBuilder().icon(phaseIcon(entry))
                .name(user.getTranslation(REF + "phase-name", NAME_PLACEHOLDER, entry.getName()))
                .description(description)
                .clickHandler((panel, u, clickType, slot) -> {
                    if (heldIndex >= 0) {
                        dropAt(displayIndex);
                    } else if (clickType == ClickType.SHIFT_LEFT) {
                        promptForLength(entry);
                    } else if (clickType.isRightClick()) {
                        toggle(entry);
                    } else {
                        pickUp(displayIndex);
                    }
                    return true;
                }).build();
    }

    private PanelItem handItem(PhaseIndexEntry entry) {
        return new PanelItemBuilder().icon(phaseIcon(entry)).glow(true)
                .name(user.getTranslation(REF + "held", NAME_PLACEHOLDER, entry.getName()))
                .description(List.of(user.getTranslation(REF + "put-back")))
                .clickHandler((panel, u, clickType, slot) -> {
                    putBack();
                    return true;
                }).build();
    }

    private PanelItem infoItem() {
        List<String> description = new ArrayList<>();
        description.add(user.getTranslation(REF + "instructions"));
        Integer gotoAtEnd = manager().getGotoAtEnd();
        if (gotoAtEnd != null) {
            description.add(user.getTranslation(REF + "repeat", NUMBER_PLACEHOLDER, String.valueOf(gotoAtEnd)));
        }
        return new PanelItemBuilder().icon(Material.BOOK).name(user.getTranslation(REF + "info-title"))
                .description(description).build();
    }

    private PanelItem dropAtEndItem(int endPosition) {
        return new PanelItemBuilder().icon(Material.LIME_STAINED_GLASS_PANE)
                .name(user.getTranslation(REF + "drop-at-end"))
                .clickHandler((panel, u, clickType, slot) -> {
                    dropAt(endPosition);
                    return true;
                }).build();
    }

    private PanelItem fillerItem() {
        return new PanelItemBuilder().icon(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name(" ")
                .clickHandler((panel, u, clickType, slot) -> {
                    putBack();
                    return true;
                }).build();
    }

    private ItemStack phaseIcon(PhaseIndexEntry entry) {
        if (!entry.isEnabled()) {
            return new ItemStack(Material.GRAY_STAINED_GLASS);
        }
        if (!manager().isPhaseAvailable(entry)) {
            return new ItemStack(Material.BARRIER);
        }
        return manager().getBlockProbs().values().stream().filter(p -> entry.equals(p.getIndexEntry()))
                .map(this::loadedPhaseIcon).filter(Objects::nonNull).findFirst()
                .orElseGet(() -> new ItemStack(Material.STONE));
    }

    /**
     * @return the phase's configured icon, falling back to its first block, or
     *         null if it has neither
     */
    private ItemStack loadedPhaseIcon(OneBlockPhase phase) {
        if (phase.getIconBlock() != null) {
            return phase.getIconBlock().clone();
        }
        if (phase.getFirstBlock() != null && phase.getFirstBlock().getMaterial() != null
                && phase.getFirstBlock().getMaterial().isItem()) {
            return new ItemStack(phase.getFirstBlock().getMaterial());
        }
        return null;
    }

    /**
     * Picks up the phase at this position in the index.
     */
    void pickUp(int indexPosition) {
        heldIndex = indexPosition;
        build();
    }

    /**
     * Puts a held phase back where it was without saving anything.
     */
    void putBack() {
        if (heldIndex >= 0) {
            heldIndex = -1;
            build();
        }
    }

    /**
     * Drops the held phase at this position among the other phases, shoving the
     * rest right, then saves and reloads.
     *
     * @param displayPosition position in the row of phases shown without the held
     *                        one
     */
    void dropAt(int displayPosition) {
        List<PhaseIndexEntry> index = manager().getPhaseIndex();
        if (heldIndex < 0 || heldIndex >= index.size()) {
            heldIndex = -1;
            build();
            return;
        }
        PhaseIndexEntry held = index.remove(heldIndex);
        index.add(Math.clamp(displayPosition, 0, index.size()), held);
        heldIndex = -1;
        persist();
    }

    /**
     * Enables or disables a phase, then saves and reloads.
     */
    void toggle(PhaseIndexEntry entry) {
        entry.setEnabled(!entry.isEnabled());
        persist();
    }

    /**
     * Closes the panel and asks the admin for a new length for this phase in
     * chat. The prompt shows the current length. A valid number is applied and
     * the panel reopens; typing the cancel word or timing out leaves the length
     * unchanged.
     */
    void promptForLength(PhaseIndexEntry entry) {
        user.closeInventory();
        new ConversationFactory(addon.getPlugin())
                .withLocalEcho(false)
                .withTimeout(60)
                .withEscapeSequence(user.getTranslation(REF + "cancel-word"))
                .withFirstPrompt(new LengthPrompt(entry))
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        user.sendMessage(REF + "length-cancelled");
                    }
                })
                .buildConversation(user.getPlayer()).begin();
    }

    /**
     * Asks for and applies a new phase length. Input handling runs on the chat
     * thread, so applying the value is pushed back onto the server thread.
     */
    class LengthPrompt extends NumericPrompt {

        private final PhaseIndexEntry entry;

        LengthPrompt(PhaseIndexEntry entry) {
            this.entry = entry;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return user.getTranslation(REF + "enter-length", NAME_PLACEHOLDER, entry.getName(),
                    NUMBER_PLACEHOLDER, String.valueOf(entry.getLength()));
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.intValue() > 0 && input.doubleValue() == input.intValue();
        }

        @Override
        protected String getInputNotNumericText(ConversationContext context, String invalidInput) {
            return user.getTranslation(REF + "invalid-length");
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return user.getTranslation(REF + "invalid-length");
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> setLength(entry, input.intValue()));
            return Prompt.END_OF_CONVERSATION;
        }
    }

    /**
     * Applies a new length to a phase, marks the index as holding admin-set
     * lengths so reconciliation never overwrites them, then saves, reloads, and
     * reopens the panel.
     */
    void setLength(PhaseIndexEntry entry, int length) {
        entry.setLength(length);
        manager().setAdminLengths();
        persist();
    }

    /**
     * Saves the index, reloads the phases so the new order takes effect, and
     * re-renders the panel from the fresh state.
     */
    private void persist() {
        boolean saved = manager().saveIndex();
        try {
            manager().loadPhases();
        } catch (IOException e) {
            addon.logError("Could not reload phases: " + e.getMessage());
            saved = false;
        }
        if (saved) {
            user.sendMessage("aoneblock.commands.admin.phases.saved");
        } else {
            user.sendMessage("aoneblock.commands.admin.phases.save-failed");
        }
        build();
    }
}
