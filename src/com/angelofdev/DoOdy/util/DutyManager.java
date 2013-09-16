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

package com.angelofdev.DoOdy.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;

import com.angelofdev.DoOdy.DoOdy;
import com.angelofdev.DoOdy.config.ConfigurationManager;
import com.angelofdev.DoOdy.events.PlayerGoingOffDutyEvent;
import com.angelofdev.DoOdy.events.PlayerGoingOnDutyEvent;
import com.angelofdev.DoOdy.events.PlayerGoneOffDutyEvent;
import com.angelofdev.DoOdy.events.PlayerGoneOnDutyEvent;
import com.angelofdev.DoOdy.exceptions.DutyException;

public class DutyManager {
	private static final String ONDOODY_EXTENSION = ".ondoody";
	private static final String LOCATION_EXTENSION = ".location";

	private DoOdy plugin;

	private Set<String> dutyCache;

	public DutyManager(DoOdy plugin) {
		this.plugin = plugin;
	}

	private File getOnDoodyFileFor(Player player) {
		return new File(plugin.getPluginDataFilePath(player.getName() + ONDOODY_EXTENSION));
	}

	private File getLocationFileFor(Player player) {
		return new File(plugin.getPluginDataFilePath(player.getName() + LOCATION_EXTENSION));
	}

	public boolean isPlayerOnDuty(Player player) {
		loadDutyCache();
		return dutyCache.contains(player.getName());
	}

	public boolean hasDutyLocation(Player player) {
		return getLocationFileFor(player).exists();
	}

	// Enable Duty Mode
	public boolean enableDutyFor(Player player) throws DutyException {
		final PluginManager pluginManager = plugin.getServer().getPluginManager();
		
		// Call the going-on-duty event and see if anyone wants to cancel us.
		PlayerGoingOnDutyEvent playerGoingOnDutyEvent = new PlayerGoingOnDutyEvent(player);
		pluginManager.callEvent(playerGoingOnDutyEvent);
		if (playerGoingOnDutyEvent.isCancelled())
			return false;

		String playerName = player.getName();

		PlayerSaveInfo playerSaveInfo = new PlayerSaveInfo();

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
			SLAPI.save(playerSaveInfo, getOnDoodyFileFor(player).getPath());
		} catch (Exception e) {
			plugin.getLog().severe("Failed storing data on /ondoody on");
			final DutyException dutyException = new DutyException("Failed saving player's data to disk.", e);
			plugin.getLogger().throwing("DutyManager", "enableDutyFor", dutyException);
			throw dutyException;
		}

		loadDutyCache();
		dutyCache.add(playerName);
		plugin.getDebug().check("<enableDutyFor> " + playerName + "'s data has been saved.");

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
		for (PotionEffect effect : activePotionEffects) {
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

		// Call the gone-on-duty event
		pluginManager.callEvent(new PlayerGoneOnDutyEvent(player));

		// Report success
		return true;
	}

	// Remove Duty Mode
	public boolean disableDutyFor(Player player) throws DutyException {
		final PluginManager pluginManager = plugin.getServer().getPluginManager();
		
		// Call the going-off-duty event and see if anyone wants to cancel us.
		PlayerGoingOffDutyEvent playerGoingOffDutyEvent = new PlayerGoingOffDutyEvent(player);
		pluginManager.callEvent(playerGoingOffDutyEvent);
		if (playerGoingOffDutyEvent.isCancelled())
			return false;

		String playerName = player.getName();
		final File onDoodyFile = getOnDoodyFileFor(player);
		PlayerSaveInfo playerSaveInfo;
		try {
			playerSaveInfo = (PlayerSaveInfo) SLAPI.load(onDoodyFile.getPath());
		} catch (Exception e) {
			plugin.getLog().severe("Failed restoring data on /ondoody off");
			final DutyException dutyException = new DutyException("Failed restoring player's data from disk.", e);
			plugin.getLogger().throwing("DutyManager", "disableDutyFor", dutyException);
			throw dutyException;
		}

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
		player.setExp(playerSaveInfo.exp);

		// Restore health
		player.setHealth(playerSaveInfo.health);

		// Restore food stats
		player.setFoodLevel(playerSaveInfo.foodLevel);
		player.setSaturation(playerSaveInfo.saturation);
		player.setExhaustion(playerSaveInfo.exhaustion);

		// Restore other stat variables
		player.setFallDistance(playerSaveInfo.fallDistance);
		player.setFireTicks(playerSaveInfo.fireTicks);
		player.setRemainingAir(playerSaveInfo.remainingAir);
		player.setVelocity(playerSaveInfo.velocity);

		// Restore potion effects
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.addPotionEffects(playerSaveInfo.potionEffects);

		// We've completely restored our player. Delete their on-doody file.
		onDoodyFile.delete();
		loadDutyCache();
		dutyCache.remove(playerName);
		plugin.getDebug().check("<disableDutyFor> " + playerName + "'s data restored & saved data deleted.");

		// Call the gone-off-duty event
		pluginManager.callEvent(new PlayerGoneOffDutyEvent(player));

		// Report success
		return true;
	}

	public void stopMobsTargeting(Player player) {
		// These numbers can probably be reduced... I couldn't find at what
		// range mobs can't or won't ever target a player...
		List<Entity> nearbyEntities = player.getNearbyEntities(128, 128, 128);
		for (Entity entity : nearbyEntities) {
			if (entity instanceof CraftCreature) {
				CraftPlayer craftPlayer = (CraftPlayer) player;
				CraftCreature creature = (CraftCreature) entity;

				EntityCreature entityCreature = creature.getHandle();
				EntityPlayer entityPlayer = craftPlayer.getHandle();

				final EntityLiving goalTarget = entityCreature.getGoalTarget();
				if (goalTarget != null && goalTarget.equals(entityPlayer)) {
					entityCreature.setGoalTarget(null);
				}
			}
		}
	}

	// Duty Items as per Config
	private void dutyItems(Player player) {
		Inventory playerInv = player.getInventory();
		ConfigurationManager configurationManager = plugin.getConfigurationManager();
		for (int i = 0; i < 9; i++) {
			playerInv.setItem(i, configurationManager.getDutyItem(i + 1));
		}
	}

	private void loadDutyCache() {
		if (dutyCache == null) {
			File dataFolder = plugin.getPluginDataFolder();
			String[] onDoodies = dataFolder.list(new FilenameFilter() {
				public boolean accept(File folder, String fileName) {
					return fileName.endsWith(ONDOODY_EXTENSION);
				}
			});

			HashSet<String> dutySet = new HashSet<String>();
			for (String onDoody : onDoodies) {
				onDoody = onDoody.substring(0, onDoody.indexOf(ONDOODY_EXTENSION));
				dutySet.add(onDoody);
			}
			dutyCache = dutySet;
		}
	}

	public Set<String> getDutySet() {
		loadDutyCache();
		return dutyCache;
	}

	public Set<Player> getDutyPlayerSet() {
		HashSet<Player> dutyPlayerSet = new HashSet<Player>();
		for (String dutyPlayerName : getDutySet()) {
			Player dutyPlayer = plugin.getServer().getPlayerExact(dutyPlayerName);
			if (dutyPlayer != null)
				dutyPlayerSet.add(dutyPlayer);
		}
		return dutyPlayerSet;
	}

	public void sendToDutyLocation(Player player) {
		String playerName = player.getName();
		try {
			final File locationFile = getLocationFileFor(player);
			LocationSaveInfo locationSaveInfo = (LocationSaveInfo) SLAPI.load(locationFile.getPath());
			player.teleport(locationSaveInfo.getLocation());
			locationFile.delete();
		} catch (IOException e) {
			plugin.getLog().warning("Could not restore " + playerName + " to their last duty location.");
			MessageSender.send(player, "&6[OnDoOdy] &cFailed returning you to last duty location.");
		}
	}

	public void saveLocation(Player player) {
		LocationSaveInfo locationSaveInfo = new LocationSaveInfo(player.getLocation());
		try {
			SLAPI.save(locationSaveInfo, getLocationFileFor(player).getPath());
		} catch (IOException e) {
			plugin.getLog().warning("Could not save the location of " + player.getName());
		}
	}

	public void hidePlayerOnDuty(Player player) {
		if (plugin.getConfigurationManager().hidePlayerOnDuty()) {
			for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
				if (otherPlayer.hasPermission("doody.seehidden"))
					continue;

				otherPlayer.hidePlayer(player);
			}
		}
	}

	public void showPlayer(Player player) {
		for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
			otherPlayer.showPlayer(player);
		}
	}

	public void showAllDutyPlayers() {
		for (Player player : getDutyPlayerSet()) {
			showPlayer(player);
		}
	}

	public void hideAllDutyPlayers() {
		for (Player player : getDutyPlayerSet()) {
			hidePlayerOnDuty(player);
		}
	}
}
