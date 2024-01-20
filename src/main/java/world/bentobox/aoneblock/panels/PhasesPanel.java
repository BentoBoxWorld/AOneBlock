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
            // return as island has no owner. Empty button will be created.
            return null;
        }

        OneBlockPhase phase = phaseEntry.getValue();
        /* Example in locale:
         *       phase:
                    name: "&f&l [phase]"
                    description: |-
                      [starting-block]
                      [biome]
                      [bank]
                      [economy]
                      [level]
                      [permission]
         */
        final String reference = "aoneblock.gui.buttons.phase.";

        // Get settings for island.
        PanelItemBuilder builder = new PanelItemBuilder();

        // Set the icon of the button
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
        // Set the title of the button
        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                    PHASE2, phase.getPhaseName()));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name",
                    PHASE2, phase.getPhaseName()));
        }

        // Process Description of the button.
        String descriptionText;

        final StringBuilder bankText = new StringBuilder();
        final StringBuilder economyText = new StringBuilder();
        final StringBuilder permissionText = new StringBuilder();
        final StringBuilder levelText = new StringBuilder();

        // Build the requirements description
        phase.getRequirements().forEach(requirement -> {
            switch (requirement.getType())
            {
            case ECO -> economyText.append(this.user.getTranslationOrNothing(reference + "economy",
                    TextVariables.NUMBER, String.valueOf(requirement.getEco())));

            case BANK -> bankText.append(this.user.getTranslationOrNothing(reference + "bank",
                    TextVariables.NUMBER, String.valueOf(requirement.getBank())));

            case LEVEL -> levelText.append(this.user.getTranslationOrNothing(reference + "level",
                    TextVariables.NUMBER, String.valueOf(requirement.getLevel())));

            case PERMISSION -> permissionText.append(this.user.getTranslationOrNothing(reference + "permission",
                    PERMISSION, requirement.getPermission()));
            case COOLDOWN -> {
                // do nothing
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + requirement.getType());

            }
        });

        if (template.description() != null)
        {
            String biomeText = phase.getPhaseBiome() == null ? "" : LangUtilsHook.getBiomeName(phase.getPhaseBiome(), this.user);

            descriptionText = this.user.getTranslationOrNothing(template.description(),
                    TextVariables.NUMBER, phase.getBlockNumber(),
                    BIOME, biomeText,
                    BANK, bankText.toString(),
                    ECONOMY, economyText.toString(),
                    LEVEL, levelText.toString(),
                    PERMISSION, permissionText.toString());
        }
        else
        {
            // Null description, so we make our own
            String blockText = this.user.getTranslationOrNothing(reference + "starting-block",
                    TextVariables.NUMBER, phase.getBlockNumber());
            String biomeText = phase.getPhaseBiome() == null ? "" : this.user.getTranslationOrNothing(reference + "biome",
                    BIOME, LangUtilsHook.getBiomeName(phase.getPhaseBiome(), this.user));

            descriptionText = this.user.getTranslationOrNothing(reference + "description",
                    "[starting-block]", biomeText,
                    BIOME, blockText,
                    BANK, bankText.toString(),
                    ECONOMY, economyText.toString(),
                    LEVEL, levelText.toString(),
                    PERMISSION, permissionText.toString());
        }

        // Strip out or replace formating
        descriptionText = descriptionText.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                replaceAll("(?<!\\\\)\\|", "\n").
                replaceAll("\\\\\\|", "|");

        builder.description(descriptionText);

        // Glow icon if user can select phase.

        boolean canApply;

        if (this.island != null && this.oneBlockIsland != null)
        {
            long lifetime = this.oneBlockIsland.getLifetime();

            if (phase.getBlockNumberValue() < lifetime)
            {
                // Check if phase requirements are met.
                canApply = !this.phaseRequirementsFail(phase, this.oneBlockIsland);
            }
            else
            {
                canApply = false;
            }
        }
        else
        {
            canApply = false;
        }

        List<ItemTemplateRecord.ActionRecords> actions = template.actions().stream().
                filter(action -> switch (action.actionType().toUpperCase()) {
                case "SELECT" -> canApply;
                case "VIEW" -> true;
                default -> false;
                }).
                toList();

        builder.glow(this.oneBlockIsland != null && this.oneBlockIsland.getPhaseName().equals(phase.getPhaseName()));

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            actions.forEach(action -> {
                if (clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN)
                {
                    if ("SELECT".equalsIgnoreCase(action.actionType()))
                    {
                        this.runCommandCall(this.addon.getSettings().getSetCountCommand().split(" ")[0], phase);
                    }
                    else
                    {
                        // TODO: implement view phase panel and command.
                        //this.runCommandCall("view", phase);
                    }
                }
            });

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = actions.stream().
                filter(action -> action.tooltip() != null).
                map(action -> this.user.getTranslation(this.world, action.tooltip())).
                filter(text -> !text.isBlank()).
                collect(Collectors.toCollection(() -> new ArrayList<>(actions.size())));

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
