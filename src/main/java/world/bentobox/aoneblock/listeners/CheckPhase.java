package world.bentobox.aoneblock.listeners;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.Requirement;
import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * Performs end of phase checking
 * 
 * @author tastybento
 *
 */
public class CheckPhase {

    private final AOneBlock addon;
    private final OneBlocksManager oneBlocksManager;
    private final BlockListener blockListener;

    /**
     * @param addon         AOneBlock
     * @param blockListener
     */
    public CheckPhase(AOneBlock addon, BlockListener blockListener) {
	this.addon = addon;
	this.oneBlocksManager = addon.getOneBlockManager();
	this.blockListener = blockListener;

    }

    /**
     * Runs end phase commands, sets new phase and runs new phase commands
     *
     * @param player - player
     * @param i      - island
     * @param is     - OneBlockIslands object
     * @param phase  - current phase
     */
    void setNewPhase(@Nullable Player player, @NonNull Island i, @NonNull OneBlockIslands is,
	    @NonNull OneBlockPhase phase) {
	// Handle NPCs
	User user;
	if (player == null || player.hasMetadata("NPC")) {
	    // Default to the owner
	    user = addon.getPlayers().getUser(i.getOwner());
	} else {
	    user = User.getInstance(player);
	}

	String newPhaseName = Objects.requireNonNullElse(phase.getPhaseName(), "");

	// Run previous phase end commands
	oneBlocksManager.getPhase(is.getPhaseName()).ifPresent(oldPhase -> {
	    String oldPhaseName = oldPhase.getPhaseName() == null ? "" : oldPhase.getPhaseName();
	    Util.runCommands(user,
		    replacePlaceholders(player, oldPhaseName, phase.getBlockNumber(), i, oldPhase.getEndCommands()),
		    "Commands run for end of " + oldPhaseName);
	    // If first time
	    if (is.getBlockNumber() >= is.getLifetime()) {
		Util.runCommands(user,
			replacePlaceholders(player, oldPhaseName, phase.getBlockNumber(), i,
				oldPhase.getFirstTimeEndCommands()),
			"Commands run for first time completing " + oldPhaseName);
	    }
	});
	// Set the phase name
	is.setPhaseName(newPhaseName);
	if (user.isPlayer() && user.isOnline() && addon.inWorld(user.getWorld())) {
	    user.getPlayer().sendTitle(newPhaseName, null, -1, -1, -1);
	}
	// Run phase start commands
	Util.runCommands(user,
		replacePlaceholders(player, newPhaseName, phase.getBlockNumber(), i, phase.getStartCommands()),
		"Commands run for start of " + newPhaseName);

	blockListener.saveIsland(i);
    }

    /**
     * Checks whether the player can proceed to the next phase
     *
     * @param player - player
     * @param i      - island
     * @param phase  - one block phase
     * @param world  - world
     * @return true if the player cannot proceed to the next phase.
     */
    protected boolean phaseRequirementsFail(@Nullable Player player, @NonNull Island i, @NonNull OneBlockIslands is,
	    OneBlockPhase phase, @NonNull World world) {

	if (phase.getRequirements().isEmpty()) {
	    return false;
	}

	return phase.getRequirements().stream().anyMatch(r -> checkRequirement(r, player, i, is, world));
    }

    private boolean checkRequirement(Requirement r, Player player, Island i, OneBlockIslands is, World world) {
	return switch (r.getType()) {
	case LEVEL -> checkLevelRequirement(r, player, i, world);
	case BANK -> checkBankRequirement(r, player, i, world);
	case ECO -> checkEcoRequirement(r, player, world);
	case PERMISSION -> checkPermissionRequirement(r, player);
	case COOLDOWN -> checkCooldownRequirement(r, player, is);
	};
    }

    private boolean checkLevelRequirement(Requirement r, Player player, Island i, World world) {
	// Level checking logic
	return addon.getAddonByName("Level").map(l -> {
	    if (((Level) l).getIslandLevel(world, i.getOwner()) < r.getLevel()) {
		User.getInstance(player).sendMessage("aoneblock.phase.insufficient-level", TextVariables.NUMBER,
			String.valueOf(r.getLevel()));
		return true;
	    }
	    return false;
	}).orElse(false);
    }

    private boolean checkBankRequirement(Requirement r, Player player, Island i, World world) {
	// Bank checking logic
	return addon.getAddonByName("Bank").map(l -> {
	    if (((Bank) l).getBankManager().getBalance(i).getValue() < r.getBank()) {
		User.getInstance(player).sendMessage("aoneblock.phase.insufficient-bank-balance", TextVariables.NUMBER,
			String.valueOf(r.getBank()));
		return true;
	    }
	    return false;
	}).orElse(false);
    }

    private boolean checkEcoRequirement(Requirement r, Player player, World world) {
	// Eco checking logic
	return addon.getPlugin().getVault().map(l -> {
	    if (l.getBalance(User.getInstance(player), world) < r.getEco()) {
		User.getInstance(player).sendMessage("aoneblock.phase.insufficient-funds", TextVariables.NUMBER,
			String.valueOf(r.getEco()));
		return true;
	    }
	    return false;
	}).orElse(false);
    }

    private boolean checkPermissionRequirement(Requirement r, Player player) {
	// Permission checking logic
	if (player != null && !player.hasPermission(r.getPermission())) {
	    User.getInstance(player).sendMessage("aoneblock.phase.insufficient-permission", TextVariables.NAME,
		    String.valueOf(r.getPermission()));
	    return true;
	}
	return false;
    }

    private boolean checkCooldownRequirement(Requirement r, Player player, OneBlockIslands is) {
	// Cooldown checking logic
	long remainingTime = r.getCooldown() - (System.currentTimeMillis() - is.getLastPhaseChangeTime()) / 1000;
	if (remainingTime > 0) {
	    User.getInstance(player).sendMessage("aoneblock.phase.cooldown", TextVariables.NUMBER,
		    String.valueOf(remainingTime));
	    return true;
	}
	return false;
    }

    /**
     * Replaces placeholders in commands.
     * 
     * <pre>
     * [island] - Island name
     * [owner] - Island owner's name
     * [player] - The name of the player who broke the block triggering the commands
     * [phase] - the name of this phase
     * [blocks] - the number of blocks broken
     * [level] - island level (Requires Levels Addon)
     * [bank-balance] - island bank balance (Requires Bank Addon)
     * [eco-balance] - player's economy balance (Requires Vault and an economy plugin)
     * </pre>
     *
     * @param player      - player
     * @param phaseName   - phase name
     * @param phaseNumber - phase's block number
     * @param i           - island
     * @param commands    - list of commands
     * @return list of commands with placeholders replaced
     */
    @NonNull
    List<String> replacePlaceholders(@Nullable Player player, @NonNull String phaseName, @NonNull String phaseNumber,
	    @NonNull Island i, List<String> commands) {
	return commands.stream().map(c -> {
	    long level = addon.getAddonByName("Level")
		    .map(l -> ((Level) l).getIslandLevel(addon.getOverWorld(), i.getOwner())).orElse(0L);
	    double balance = addon.getAddonByName("Bank").map(b -> ((Bank) b).getBankManager().getBalance(i).getValue())
		    .orElse(0D);
	    double ecoBalance = addon.getPlugin().getVault()
		    .map(v -> v.getBalance(User.getInstance(player), addon.getOverWorld())).orElse(0D);

	    return c.replace("[island]", i.getName() == null ? "" : i.getName())
		    .replace("[owner]", addon.getPlayers().getName(i.getOwner())).replace("[phase]", phaseName)
		    .replace("[blocks]", phaseNumber).replace("[level]", String.valueOf(level))
		    .replace("[bank-balance]", String.valueOf(balance))
		    .replace("[eco-balance]", String.valueOf(ecoBalance));

	}).map(c -> addon.getPlugin().getPlaceholdersManager().replacePlaceholders(player, c))
		.collect(Collectors.toList());
    }
}
