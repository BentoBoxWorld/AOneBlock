//
// Created by BONNe
// Copyright - 2021
//

package world.bentobox.aoneblock.panels;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.Requirement;
import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;


/**
 * This class shows how to set up easy panel by using BentoBox PanelBuilder API
 */
public class PhasesPanel
{
    // Text Constants
    private static final String PHASE = "PHASE";
    private static final String PREVIOUS = "PREVIOUS";
    private static final String NEXT = "NEXT";
    private static final String BIOME = "[biome]";
    private static final String BANK = "[bank]";
    private static final String ECONOMY = "[economy]";
    private static final String PERMISSION = "[permission]";
    private static final String LEVEL = "[level]";
    private static final String PHASE2 = "[phase]";
    private static final String INDEXING = "indexing";
    private static final String BLOCKS = "[blocks]";
    public static final String REFERENCE = "aoneblock.gui.buttons.phase.";

    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon VisitAddon object
     * @param world World where user will be teleported
     * @param user User who opens panel
     */
    private PhasesPanel(AOneBlock addon,
            World world,
            User user)
    {
        this.addon = addon;
        this.user = user;
        this.world = world;

        this.elementList = addon.getOneBlockManager().getBlockProbs().entrySet().stream().
                filter(en -> !en.getValue().isGotoPhase()).
                sorted(Comparator.comparingInt(Map.Entry::getKey)).
                toList();

        this.island = this.addon.getIslandsManager().getIsland(world, user);
        this.oneBlockIsland = this.island == null ? null : this.addon.getBlockListener().getIsland(this.island);
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * Build method manages current panel opening. It uses BentoBox PanelAPI that is easy to use and users can get nice
     * panels.
     */
    private void build()
    {
        // Do not open gui if there is no magic sticks.
        if (this.elementList.isEmpty())
        {
            this.addon.logError("There are no available phases for selection!");
            this.user.sendMessage("no-phases",
                    TextVariables.GAMEMODE, this.addon.getDescription().getName());
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("phases_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder(PHASE, this::createPhaseButton);

        // Register next and previous builders
        panelBuilder.registerTypeBuilder(NEXT, this::createNextButton);
        panelBuilder.registerTypeBuilder(PREVIOUS, this::createPreviousButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    // ---------------------------------------------------------------------
    // Section: Buttons
    // ---------------------------------------------------------------------


    /**
     * Create next button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        int size = this.elementList.size();

        if (size <= slot.amountMap().getOrDefault(PHASE, 1) ||
                1.0 * size / slot.amountMap().getOrDefault(PHASE, 1) <= this.pageIndex + 1)
        {
            // There are no next elements
            return null;
        }

        int nextPageIndex = this.pageIndex + 2;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((boolean) template.dataMap().getOrDefault(INDEXING, false))
            {
                clone.setAmount(nextPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                    TextVariables.NUMBER, String.valueOf(nextPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            template.actions().forEach(action -> {
                if ((clickType == action.clickType()  || action.clickType() == ClickType.UNKNOWN) && NEXT.equalsIgnoreCase(action.actionType()))
                {
                    // Next button ignores click type currently.
                    this.pageIndex++;
                    this.build();
                }

            });

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
                filter(action -> action.tooltip() != null).
                map(action -> this.user.getTranslation(this.world, action.tooltip())).
                filter(text -> !text.isBlank()).
                collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Create previous button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createPreviousButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.pageIndex == 0)
        {
            // There are no next elements
            return null;
        }

        int previousPageIndex = this.pageIndex;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((boolean) template.dataMap().getOrDefault(INDEXING, false))
            {
                clone.setAmount(previousPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                    TextVariables.NUMBER, String.valueOf(previousPageIndex)));
        }

        // Add ClickHandler
        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            template.actions().forEach(action -> {
                if ((clickType == action.clickType()  || action.clickType() == ClickType.UNKNOWN) && PREVIOUS.equalsIgnoreCase(action.actionType()))
                {
                    // Next button ignores click type currently.
                    this.pageIndex--;
                    this.build();
                }

            });

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
                filter(action -> action.tooltip() != null).
                map(action -> this.user.getTranslation(this.world, action.tooltip())).
                filter(text -> !text.isBlank()).
                collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * This method creates and returns island button.
     *
     * @return PanelItem that represents island button.
     */
    @Nullable
    private PanelItem createPhaseButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.elementList.isEmpty())
        {
            // Does not contain any sticks.
            return null;
        }

        int index = this.pageIndex * slot.amountMap().getOrDefault(PHASE, 1) + slot.slot();

        if (index >= this.elementList.size())
        {
            // Out of index.
            return null;
        }

        return this.createPhaseButton(template, this.elementList.get(index));
    }


    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------


    /**
     * This method creates phases button.
     *
     * @return PanelItem that allows to select phases button
     */
    private PanelItem createPhaseButton(ItemTemplateRecord template, Map.Entry<Integer, OneBlockPhase> phaseEntry)
    {
        if (phaseEntry == null || phaseEntry.getValue() == null)
        {
            return null;
        }

        OneBlockPhase phase = phaseEntry.getValue();
        PanelItemBuilder builder = new PanelItemBuilder();

        applyIcon(builder, template, phase);
        applyTitle(builder, template, phase);

        RequirementTexts reqs = buildRequirementsText(phase);
        String blocksText = buildBlocksText(phase);
        String descriptionText = buildDescriptionText(template, phase, reqs, blocksText);

        // Strip out or replace formatting
        descriptionText = descriptionText.replaceAll("(?m)^[ \\t]*\\r?\\n", "")
                .replaceAll("(?<!\\\\)\\|", "\n")
                .replaceAll("\\\\\\|", "|");

        builder.description(descriptionText);
        builder.glow(this.oneBlockIsland != null && this.oneBlockIsland.getPhaseName().equals(phase.getPhaseName()));

        boolean canApply = canApplyPhase(phase);
        List<ItemTemplateRecord.ActionRecords> actions = template.actions().stream()
                .filter(action -> switch (action.actionType().toUpperCase()) {
                    case "SELECT" -> canApply;
                    case "VIEW" -> true;
                    default -> false;
                })
                .toList();

        builder.clickHandler(buildClickHandler(actions, phase));

        List<String> tooltips = collectTooltips(actions);
        if (!tooltips.isEmpty())
        {
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }

    /**
     * Applies the icon to the panel item builder from the template or phase data.
     */
    private void applyIcon(PanelItemBuilder builder, ItemTemplateRecord template, OneBlockPhase phase)
    {
        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            ItemStack firstBlock = phase.getFirstBlock() == null ?
                    new ItemStack(Material.STONE) :
                    new ItemStack(phase.getFirstBlock().getMaterial());
            builder.icon(phase.getIconBlock() == null ? firstBlock : phase.getIconBlock());
        }
    }

    /**
     * Applies the title to the panel item builder from the template or phase data.
     */
    private void applyTitle(PanelItemBuilder builder, ItemTemplateRecord template, OneBlockPhase phase)
    {
        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title(), PHASE2, phase.getPhaseName()));
        }
        else
        {
            builder.name(this.user.getTranslation(REFERENCE + "name", PHASE2, phase.getPhaseName()));
        }
    }

    /**
     * Holds the pre-built requirement description strings for a phase.
     */
    private record RequirementTexts(String bank, String economy, String level, String permission) {}

    /**
     * Builds the requirement text strings for bank, economy, level, and permission requirements.
     */
    private RequirementTexts buildRequirementsText(OneBlockPhase phase)
    {
        StringBuilder bankText = new StringBuilder();
        StringBuilder economyText = new StringBuilder();
        StringBuilder permissionText = new StringBuilder();
        StringBuilder levelText = new StringBuilder();

        phase.getRequirements().forEach(requirement -> {
            switch (requirement.getType())
            {
                case ECO -> economyText.append(this.user.getTranslationOrNothing(REFERENCE + "economy",
                        TextVariables.NUMBER, String.valueOf(requirement.getEco())));
                case BANK -> bankText.append(this.user.getTranslationOrNothing(REFERENCE + "bank",
                        TextVariables.NUMBER, String.valueOf(requirement.getBank())));
                case LEVEL -> levelText.append(this.user.getTranslationOrNothing(REFERENCE + "level",
                        TextVariables.NUMBER, String.valueOf(requirement.getLevel())));
                case PERMISSION -> permissionText.append(this.user.getTranslationOrNothing(REFERENCE + "permission",
                        PERMISSION, requirement.getPermission()));
                case COOLDOWN -> { /* do nothing */ }
                default -> throw new IllegalArgumentException("Unexpected value: " + requirement.getType());
            }
        });

        return new RequirementTexts(bankText.toString(), economyText.toString(),
                levelText.toString(), permissionText.toString());
    }

    /**
     * Builds the blocks text with word-wrapped newlines inserted.
     */
    private String buildBlocksText(OneBlockPhase phase)
    {
        String blocksText = user.getTranslation(REFERENCE + "blocks-prefix") + phase.getBlocks().keySet().stream()
                .map(m -> getMaterialName(user, m))
                .map(string -> user.getTranslation(REFERENCE + "blocks", TextVariables.NAME, string))
                .collect(Collectors.joining());
        blocksText = blocksText.trim();
        if (blocksText.endsWith("\n") || blocksText.endsWith(","))
        {
            blocksText = blocksText.substring(0, blocksText.length() - 1);
        }
        return insertNewlines(blocksText, parseWrapAt());
    }

    /**
     * Parses the wrap-at translation value, defaulting to 50 on parse failure.
     */
    private int parseWrapAt()
    {
        try
        {
            return Integer.parseInt(user.getTranslation(REFERENCE + "wrap-at"));
        }
        catch (NumberFormatException e)
        {
            addon.logError("Warning: Unable to parse 'wrap-at' value, using default of 50.");
            return 50;
        }
    }

    /**
     * Builds the full description text, delegating to the templated or default branch.
     */
    private String buildDescriptionText(ItemTemplateRecord template, OneBlockPhase phase,
            RequirementTexts reqs, String blocksText)
    {
        if (template.description() != null)
        {
            return buildTemplatedDescription(template, phase, reqs, blocksText);
        }
        return buildDefaultDescription(phase, reqs, blocksText);
    }

    /**
     * Builds the description text when a template description key is present.
     */
    private String buildTemplatedDescription(ItemTemplateRecord template, OneBlockPhase phase,
            RequirementTexts reqs, String blocksText)
    {
        String biomeText = phase.getPhaseBiome() == null ? ""
                : LangUtilsHook.getBiomeName(phase.getPhaseBiome(), this.user);
        return this.user.getTranslationOrNothing(template.description(),
                TextVariables.NUMBER, phase.getBlockNumber(),
                BIOME, biomeText,
                BANK, reqs.bank(),
                ECONOMY, reqs.economy(),
                LEVEL, reqs.level(),
                PERMISSION, reqs.permission(),
                BLOCKS, blocksText);
    }

    /**
     * Builds the description text when no template description key is present.
     */
    private String buildDefaultDescription(OneBlockPhase phase, RequirementTexts reqs, String blocksText)
    {
        String blockText = this.user.getTranslationOrNothing(REFERENCE + "starting-block",
                TextVariables.NUMBER, phase.getBlockNumber());
        String biomeText = phase.getPhaseBiome() == null ? ""
                : this.user.getTranslationOrNothing(REFERENCE + "biome",
                        BIOME, LangUtilsHook.getBiomeName(phase.getPhaseBiome(), this.user));
        return this.user.getTranslationOrNothing(REFERENCE + "description",
                "[starting-block]", biomeText,
                BIOME, blockText,
                BANK, reqs.bank(),
                ECONOMY, reqs.economy(),
                LEVEL, reqs.level(),
                PERMISSION, reqs.permission(),
                BLOCKS, blocksText);
    }

    /**
     * Determines whether the player can jump to (apply) the given phase.
     */
    private boolean canApplyPhase(OneBlockPhase phase)
    {
        if (this.island == null || this.oneBlockIsland == null)
        {
            return false;
        }
        if (phase.getBlockNumberValue() >= this.oneBlockIsland.getLifetime())
        {
            return false;
        }
        return !this.phaseRequirementsFail(phase, this.oneBlockIsland);
    }

    /**
     * Builds the click handler for a phase button.
     */
    private PanelItem.ClickHandler buildClickHandler(List<ItemTemplateRecord.ActionRecords> actions,
            OneBlockPhase phase)
    {
        return (panel, user, clickType, i) ->
        {
            actions.forEach(action ->
            {
                if ((clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN)
                        && "SELECT".equalsIgnoreCase(action.actionType()))
                {
                    this.runCommandCall(this.addon.getSettings().getSetCountCommand().split(" ")[0], phase);
                }
            });
            return true;
        };
    }

    /**
     * Collects non-blank tooltip strings from a list of action records.
     */
    private List<String> collectTooltips(List<ItemTemplateRecord.ActionRecords> actions)
    {
        return actions.stream()
                .filter(action -> action.tooltip() != null)
                .map(action -> this.user.getTranslation(this.world, action.tooltip()))
                .filter(text -> !text.isBlank())
                .collect(Collectors.toCollection(() -> new ArrayList<>(actions.size())));
    }

    private String getMaterialName(User user, Material m) {
        return addon.getPlugin().getHooks().getHook("LangUtils").map(hook -> LangUtilsHook.getMaterialName(m, user))
                .orElse(Util.prettifyText(m.name()));
    }

    private static String insertNewlines(String input, int interval) {
        StringBuilder result = new StringBuilder(input.length());
        int index = 0;
        char activeColor = 'a';
        int lastAmpIndex = -2;

        while (index < input.length()) {
            if (input.charAt(index) == ChatColor.COLOR_CHAR && index < (input.length() - 1)) {
                lastAmpIndex = index;
                activeColor = input.charAt(index + 1);
            }
            if (input.length() < index + interval) {
                result.append(input.substring(index));
                break;
            }

            // Find the space near the interval to break the line without cutting a word
            int breakPoint = input.lastIndexOf(' ', index + interval);
            if (breakPoint <= index) {
                breakPoint = index + interval; // In case there are no spaces, break at exact interval
            }

            result.append(input, index, breakPoint).append('\n');
            if (lastAmpIndex >= 0) {
                // Append color code
                result.append(ChatColor.COLOR_CHAR);
                result.append(activeColor);
                result.append(" ");
            }
            index = breakPoint + 1; // Move past the last space
        }

        return result.toString();
    }


    /**
     * This method checks if phase requirements fails.
     * @param phase Phase object.
     * @return {@code true} if phase requirements fails, {@code false} otherwise.
     */
    private boolean phaseRequirementsFail(OneBlockPhase phase, OneBlockIslands is)
    {
        // Check requirements
        for (Requirement requirement : phase.getRequirements())
        {
            // Check all the requirements and if one fails, then exit
            if (switch (requirement.getType()) {
            case LEVEL -> this.addon.getAddonByName("Level").filter(Addon::isEnabled).map(a ->
            ((Level) a).getIslandLevel(this.world, this.island.getOwner()) < requirement.getLevel()).orElse(false);

            case BANK -> this.addon.getAddonByName("Bank").filter(Addon::isEnabled).map(a ->
            ((Bank) a).getBankManager().getBalance(this.island).getValue() < requirement.getBank()).orElse(false);

            case ECO -> this.addon.getPlugin().getVault().map(a -> a.getBalance(this.user, this.world) < requirement.getEco()).orElse(false);

            case PERMISSION -> this.user != null && !this.user.hasPermission(requirement.getPermission());

            case COOLDOWN -> (requirement.getCooldown() - (System.currentTimeMillis() - is.getLastPhaseChangeTime()) / 1000) > 0;
            }) {
                return true;
            }
        }
        return false;
    }


    /**
     * This method runs command call that allows player to visit clicked island.
     */
    private void runCommandCall(String command, OneBlockPhase phase)
    {
        // Get first player command label.
        this.addon.getPlayerCommand().ifPresent(mainCommand ->
        mainCommand.getSubCommand(command).ifPresent(subCommand -> {
            // Check if subcommand is setCount command.
            if (Arrays.stream(this.addon.getSettings().getSetCountCommand().split(" ")).
                    anyMatch(text -> text.equalsIgnoreCase(subCommand.getLabel())))
            {
                this.addon.log(this.user.getName() + " called: `" + mainCommand.getTopLabel() + " " + subCommand.getLabel() + " " + phase.getBlockNumber());
                // Confirmation is done via GUI. Bypass.
                this.user.performCommand(mainCommand.getTopLabel() + " " + subCommand.getLabel() + " " + phase.getBlockNumber());
            }
        }));

        // Close inventory
        this.user.closeInventory();
    }


    // ---------------------------------------------------------------------
    // Section: Static methods
    // ---------------------------------------------------------------------


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param addon AOneBLock object
     * @param world World where user is located.
     * @param user User who opens panel
     */
    public static void openPanel(AOneBlock addon,
            World world,
            User user)
    {
        new PhasesPanel(addon, world, user).build();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable allows to access addon object.
     */
    private final AOneBlock addon;

    /**
     * This variable holds user who opens panel. Without it panel cannot be opened.
     */
    private final User user;

    /**
     * This variable holds world where panel is opened. Without it panel cannot be opened.
     */
    private final World world;

    /**
     * This variable stores filtered elements.
     */
    private final List<Map.Entry<Integer, OneBlockPhase>> elementList;

    /**
     * This variable stores oneblock island data.
     */
    private final OneBlockIslands oneBlockIsland;

    /**
     * This variable stores oneblock island.
     */
    private final Island island;

    /**
     * This variable holds current pageIndex for multi-page island choosing.
     */
    private int pageIndex;
}
