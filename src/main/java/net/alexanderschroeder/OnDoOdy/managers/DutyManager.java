/*
 *  OnDoOdy v1: Separates Admin/Mod duties so everyone can enjoy the game.
 *  Copyright © 2013  Alexander Krivács Schrøder
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package net.alexanderschroeder.OnDoOdy.managers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import net.alexanderschroeder.OnDoOdy.events.PlayerGoingOffDutyEvent;
import net.alexanderschroeder.OnDoOdy.events.PlayerGoingOnDutyEvent;
import net.alexanderschroeder.OnDoOdy.events.PlayerGoneOffDutyEvent;
import net.alexanderschroeder.OnDoOdy.events.PlayerGoneOnDutyEvent;
import net.alexanderschroeder.OnDoOdy.exceptions.DutyException;
import net.alexanderschroeder.OnDoOdy.managers.ConfigurationManager.DutyCommand;
import net.alexanderschroeder.bukkitutil.storage.StorageException;
import net.minecraft.server.v1_6_R3.EntityCreature;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityPlayer;

import org.bukkit.GameMode;
import org.bukkit.command.CommandException;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;

public class DutyManager {

	private static final String DUTY_PERMISSIONS_METADATA_KEY = "duty-permissions";

	private final OnDoOdy plugin;

	private Set<String> dutyCache;

	public DutyManager(final OnDoOdy plugin) {
		this.plugin = plugin;
	}

	public boolean isPlayerOnDuty(final Player player) {
		return getDutySet().contains(player.getName());
	}

	public boolean hasDutyLocation(final Player player) {
		return plugin.getStorage().exists("location", player.getName());
	}

	// Enable Duty Mode
	public boolean enableDutyFor(final Player player) throws DutyException {
		final PluginManager pluginManager = plugin.getServer().getPluginManager();

		// Call the going-on-duty event and see if anyone wants to cancel us.
		final PlayerGoingOnDutyEvent playerGoingOnDutyEvent = new PlayerGoingOnDutyEvent(player);
		pluginManager.callEvent(playerGoingOnDutyEvent);
		if (playerGoingOnDutyEvent.isCancelled()) {
			return false;
		}

		final String playerName = player.getName();

		final PlayerSaveInfo playerSaveInfo = new PlayerSaveInfo();

		// Save EXP
		playerSaveInfo.level = player.getLevel();
		playerSaveInfo.exp = player.getExp();

		// Save health
		playerSaveInfo.health = player.getHealth();

		// Save food stats
		playerSaveInfo.foodLevel = player.getFoodLevel();
		playerSaveInfo.saturation = player.getSaturation();
		playerSaveInfo.exhaustion = player.getExhaustion();

		// Save other stat variables
		playerSaveInfo.fallDistance = player.getFallDistance();
		playerSaveInfo.fireTicks = player.getFireTicks();
		playerSaveInfo.remainingAir = player.getRemainingAir();
		playerSaveInfo.velocity = player.getVelocity().clone();

		// Save potion effects
		final Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
		playerSaveInfo.potionEffects = activePotionEffects;

		// Save player's inventory
		final PlayerInventory inventory = player.getInventory();
		playerSaveInfo.inventory = new InventorySaveInfo(inventory);

		// Save player location to file.
		playerSaveInfo.location = new LocationSaveInfo(player.getLocation());

		try {
			// Save to file
			plugin.getStorage().save("duty", playerName, playerSaveInfo);

			getDutySet().add(playerName);
			plugin.getDebug().info(playerName + "'s data has been saved.");
		} catch (final Exception e) {
			plugin.getLogger().severe("Failed storing data on /ondoody on");
			final DutyException dutyException = new DutyException("Failed saving player's data.", e);
			plugin.getLogger().throwing("DutyManager", "enableDutyFor", dutyException);
			throw dutyException;
		}

		// Now we're certain the player's data is safely stored on disk, we can
		// take it all away from them.

		// Remove experience
		player.setLevel(0);
		player.setExp(0);

		// Remove inventory
		inventory.clear();
		inventory.setHelmet(null);
		inventory.setChestplate(null);
		inventory.setLeggings(null);
		inventory.setBoots(null);

		// Remove potion effects
		for (final PotionEffect effect : activePotionEffects) {
			player.removePotionEffect(effect.getType());
		}

		// Put player in creative mode.
		player.setGameMode(GameMode.CREATIVE);

		// Stop all mobs that are currently targeting the player
		// from targeting them.
		stopMobsTargeting(player);

		// Give duty tools
		dutyItems(player);

		// Hide player
		hidePlayerOnDuty(player);

		// Give the player extra permissions
		giveExtraPermissions(player);

		// Run going-on-duty commands
		runOnDutyCommands(player);

		// Call the gone-on-duty event
		pluginManager.callEvent(new PlayerGoneOnDutyEvent(player));

		// Report success
		return true;
	}

	// Remove Duty Mode
	public boolean disableDutyFor(final Player player) throws DutyException {
		final PluginManager pluginManager = plugin.getServer().getPluginManager();

		// Call the going-off-duty event and see if anyone wants to cancel us.
		final PlayerGoingOffDutyEvent playerGoingOffDutyEvent = new PlayerGoingOffDutyEvent(player);
		pluginManager.callEvent(playerGoingOffDutyEvent);
		if (playerGoingOffDutyEvent.isCancelled()) {
			return false;
		}

		final String playerName = player.getName();
		PlayerSaveInfo playerSaveInfo;
		try {
			playerSaveInfo = (PlayerSaveInfo)plugin.getStorage().load("duty", playerName); //SLAPI.loadFromFile(onDoodyFile.getPath());
		} catch (final Exception e) {
			plugin.getLogger().severe("Failed restoring data on /ondoody off");
			final DutyException dutyException = new DutyException("Failed restoring player's data from disk.", e);
			plugin.getLogger().throwing("DutyManager", "disableDutyFor", dutyException);
			throw dutyException;
		}

		// Run going-off-duty commands
		runOffDutyCommands(player);

		// Remove the extra permissions
		removeExtraPermissions(player);

		// Restore player's game mode
		player.setGameMode(GameMode.SURVIVAL);

		// Save current duty location for use with /dm back
		saveLocation(player);

		// Place player back where they were
		player.teleport(playerSaveInfo.location.getLocation());

		// Make player visible
		showPlayer(player);

		// Restore player inventory
		playerSaveInfo.inventory.restore(player.getInventory());

		// Restore experience
		player.setLevel(playerSaveInfo.level);
		player.setExp((float) playerSaveInfo.exp);

		// Restore health
		player.setHealth(playerSaveInfo.health);

		// Restore food stats
		player.setFoodLevel(playerSaveInfo.foodLevel);
		player.setSaturation((float) playerSaveInfo.saturation);
		player.setExhaustion((float) playerSaveInfo.exhaustion);

		// Restore other stat variables
		player.setFallDistance((float) playerSaveInfo.fallDistance);
		player.setFireTicks(playerSaveInfo.fireTicks);
		player.setRemainingAir(playerSaveInfo.remainingAir);
		player.setVelocity(playerSaveInfo.velocity);

		// Restore potion effects
		for (final PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.addPotionEffects(playerSaveInfo.potionEffects);

		// We've completely restored our player. Delete their on-duty entry.
		try {
			plugin.getStorage().deleteKey("duty", playerName);
			
			getDutySet().remove(playerName);
			plugin.getDebug().info(playerName + "'s data restored & saved data deleted.");
		} catch (StorageException e) {
			plugin.getLogger().severe("Failed deleting duty data on /ondoody off. OnDoOdy will think the player is still on duty if not resolved before the next server restart.");
		}

		// Call the gone-off-duty event
		pluginManager.callEvent(new PlayerGoneOffDutyEvent(player));

		// Report success
		return true;
	}

	public static void stopMobsTargeting(final Player player) {
		// These numbers can probably be reduced... I couldn't find at what
		// range mobs can't or won't ever target a player...
		final List<Entity> nearbyEntities = player.getNearbyEntities(128, 128, 128);
		for (final Entity entity : nearbyEntities) {
			if (entity instanceof CraftCreature) {
				final CraftPlayer craftPlayer = (CraftPlayer) player;
				final CraftCreature creature = (CraftCreature) entity;

				final EntityCreature entityCreature = creature.getHandle();
				final EntityPlayer entityPlayer = craftPlayer.getHandle();

				final EntityLiving goalTarget = entityCreature.getGoalTarget();
				if (goalTarget != null && goalTarget.equals(entityPlayer)) {
					entityCreature.setGoalTarget(null);
				}
			}
		}
	}

	public void giveExtraPermissions(final Player player) {
		removeExtraPermissions(player);

		final Set<String> extraPermissionSet = plugin.getConfigurationManager().getExtraPermissionSetFor(player);
		if (extraPermissionSet.size() == 0) {
			return;
		}

		final PermissionAttachment attachment = player.addAttachment(plugin);
		for (final String permission : extraPermissionSet) {
			attachment.setPermission(permission, true);
		}
		player.setMetadata(DUTY_PERMISSIONS_METADATA_KEY, new FixedMetadataValue(plugin, attachment));
	}

	public void removeExtraPermissions(final Player player) {
		final PermissionAttachment attachment = (PermissionAttachment) plugin.getPlayerMetadataManager().getMetadata(player, DUTY_PERMISSIONS_METADATA_KEY);
		if (attachment != null) {
			attachment.remove();
			plugin.getPlayerMetadataManager().removeMetadata(player, DUTY_PERMISSIONS_METADATA_KEY);
		}
	}

	private void runOnDutyCommands(final Player player) {
		final List<DutyCommand> onDutyCommands = plugin.getConfigurationManager().getOnDutyCommandsFor(player);
		runDutyCommands(player, onDutyCommands);
	}

	private void runOffDutyCommands(final Player player) {
		final List<DutyCommand> offDutyCommands = plugin.getConfigurationManager().getOffDutyCommandsFor(player);
		runDutyCommands(player, offDutyCommands);
	}

	private void runDutyCommands(final Player player, final List<DutyCommand> dutyCommands) {
		final PermissionAttachment temporaryAttachment = player.addAttachment(plugin);
		for (final DutyCommand command : dutyCommands) {
			final List<String> permissions = command.getPermissions();
			for (final String permission : permissions) {
				temporaryAttachment.setPermission(permission, true);
			}
		}
		for (final DutyCommand command : dutyCommands) {
			try {
				plugin.getServer().dispatchCommand(player, command.getCommand());
			} catch (final CommandException e) {
				plugin.getDebug().severe("Failed running duty command '" + command.getCommand() + "' for player '" + player.getName() + "'");
			}
		}
		temporaryAttachment.remove();
	}

	private void dutyItems(final Player player) {
		final Inventory playerInv = player.getInventory();
		final ConfigurationManager configurationManager = plugin.getConfigurationManager();
		for (int i = 0; i < 9; i++) {
			playerInv.setItem(i, configurationManager.getDutyItem(i + 1));
		}
	}

	private void loadDutyCache() {
		if (dutyCache == null) {
			dutyCache = new HashSet<String>(plugin.getStorage().getKeys("duty"));
		}
	}

	public Set<String> getDutySet() {
		loadDutyCache();
		return dutyCache;
	}

	public Set<Player> getDutyPlayerSet() {
		final HashSet<Player> dutyPlayerSet = new HashSet<Player>();
		for (final String dutyPlayerName : getDutySet()) {
			final Player dutyPlayer = plugin.getServer().getPlayerExact(dutyPlayerName);
			if (dutyPlayer != null) {
				dutyPlayerSet.add(dutyPlayer);
			}
		}
		return dutyPlayerSet;
	}

	public void sendToDutyLocation(final Player player) throws DutyException {
		try {
			//final File locationFile = getLocationFileFor(player);
			final LocationSaveInfo locationSaveInfo = (LocationSaveInfo) plugin.getStorage().load("location", player.getName()); // SLAPI.loadFromFile(locationFile.getPath());
			if (locationSaveInfo == null) {
				return;
			}
			plugin.getStorage().deleteKey("location", player.getName());

			player.teleport(locationSaveInfo.getLocation());
		} catch (final StorageException e) {
			final DutyException dutyException = new DutyException("Failed retoring or deleting player's location.", e);
			plugin.getLogger().throwing("DutyManager", "sendToDutyLocation", dutyException);
			throw dutyException;
		}
	}

	public void saveLocation(final Player player) throws DutyException {
		final LocationSaveInfo locationSaveInfo = new LocationSaveInfo(player.getLocation());
		try {
			//SLAPI.saveToFile(locationSaveInfo, getLocationFileFor(player).getPath());
			plugin.getStorage().save("location", player.getName(), locationSaveInfo);
		} catch (final StorageException e) {
			final DutyException dutyException = new DutyException("Failed saving player's location.", e);
			plugin.getLogger().throwing("DutyManager", "saveLocation", dutyException);
			throw dutyException;
		}
	}

	public void hidePlayerOnDuty(final Player player) {
		if (plugin.getConfigurationManager().hidePlayerOnDuty()) {
			for (final Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
				if (otherPlayer.hasPermission("doody.seehidden")) {
					continue;
				}

				otherPlayer.hidePlayer(player);
			}
		}
	}

	public void showPlayer(final Player player) {
		for (final Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
			otherPlayer.showPlayer(player);
		}
	}

	public void showAllDutyPlayers() {
		for (final Player player : getDutyPlayerSet()) {
			showPlayer(player);
		}
	}

	public void hideAllDutyPlayers() {
		for (final Player player : getDutyPlayerSet()) {
			hidePlayerOnDuty(player);
		}
	}
}
