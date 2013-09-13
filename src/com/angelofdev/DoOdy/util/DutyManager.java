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
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import com.angelofdev.DoOdy.DoOdy;
import com.angelofdev.DoOdy.config.ConfigurationManager;

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
	public boolean enableDutyFor(Player player) {
		String playerName = player.getName();
		try {
			PlayerSaveInfo playerSaveInfo = new PlayerSaveInfo();

			// Save EXP
			playerSaveInfo.level = player.getLevel();
			playerSaveInfo.exp = player.getExp();

			// Save health/food stats
			playerSaveInfo.health = player.getHealth();
			playerSaveInfo.foodLevel = player.getFoodLevel();

			// Save potion effects
			final Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
			playerSaveInfo.potionEffects = activePotionEffects;

			// Save player's inventory
			final PlayerInventory inventory = player.getInventory();
			playerSaveInfo.inventory = new InventorySaveInfo(inventory);

			// Save player location to file.
			playerSaveInfo.location = new LocationSaveInfo(player.getLocation());

			// Save to file
			SLAPI.save(playerSaveInfo, getOnDoodyFileFor(player).getPath());
			loadDutyCache();
			dutyCache.add(playerName);
			plugin.getDebug().check("<enableDutyFor> " + playerName + "'s data has been saved.");

			// Now we're certain the player's stuff is safe, we can take them
			// away from them.

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
			MessageSender.send(player, "&6[OnDoOdy] &aYou're now on duty.");

			// Give duty tools
			dutyItems(player);

			return true;
		} catch (Exception e) {
			plugin.getLog().severe("Failed Storing data on /ondoody on");
			plugin.getLogger().throwing("DutyManager", "enableDutyFor", e);
			MessageSender.send(player, "&6[OnDoOdy] &cFailed storing your data. Could not place you on duty.");
			return false;
		}
	}

	// Remove Duty Mode
	public boolean disableDutyFor(Player player) {
		String playerName = player.getName();
		try {
			final File onDoodyFile = getOnDoodyFileFor(player);
			PlayerSaveInfo playerSaveInfo = (PlayerSaveInfo) SLAPI.load(onDoodyFile.getPath());

			// Restore player's game mode
			player.setGameMode(GameMode.SURVIVAL);

			// Save current duty location for use with /dm back
			saveLocation(player);

			// Place player back where they were
			player.teleport(playerSaveInfo.location.getLocation());

			// Restore player inventory
			playerSaveInfo.inventory.restore(player.getInventory());

			// Restore experience
			player.setLevel(playerSaveInfo.level);
			player.setExp(playerSaveInfo.exp);

			// Restore potion effects
			player.addPotionEffects(playerSaveInfo.potionEffects);

			// We've completely restored our player. Delete their on-doody file.
			onDoodyFile.delete();
			loadDutyCache();
			dutyCache.remove(playerName);

			MessageSender.send(player, "&6[OnDoOdy] &aYou're no longer on duty.");
			plugin.getDebug().check("<removeDoody> " + playerName + "'s data restored & saved data deleted.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			plugin.getLog().warning("Failed restoring the inventory of " + playerName + ".");
			plugin.getLog().warning("Failed restoring the location of " + playerName + ".");
			MessageSender.send(player, "&6[OnDoOdy] &cFailed restoring you to pre-duty state. Plugin encountered error.");
			MessageSender.send(player, "&6[OnDoOdy] &cPlease try again.");
			return false;
		}
	}

	// Duty Items as per Config
	public void dutyItems(Player player) {
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

			HashSet<String> dutyList = new HashSet<String>();
			for (String onDoody : onDoodies) {
				onDoody = onDoody.substring(0, onDoody.indexOf(ONDOODY_EXTENSION));
				dutyList.add(onDoody);
			}
			dutyCache = dutyList;
		}
	}

	public Set<String> getDutyList() {
		loadDutyCache();
		return dutyCache;
	}

	public void sendToLocation(Player player) {
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
}
