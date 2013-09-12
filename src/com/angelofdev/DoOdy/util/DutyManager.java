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
import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import com.angelofdev.DoOdy.DoOdy;
import com.angelofdev.DoOdy.config.ConfigurationManager;

public class DutyManager {
	private DoOdy plugin;

	private Set<String> dutyCache;

	public DutyManager(DoOdy plugin) {
		this.plugin = plugin;
	}

	private File getDoodyFileFor(Player player) {
		return new File(plugin.getPluginDataFilePath(player.getName() + ".doody"));
	}

	private File getLocationFileFor(Player player) {
		return new File(plugin.getPluginDataFilePath(player.getName() + ".location"));
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

			// Save player's inventory
			final PlayerInventory inventory = player.getInventory();
			playerSaveInfo.inventory = new InventorySaveInfo(inventory);

			// Save player location to file.
			playerSaveInfo.location = new LocationSaveInfo(player.getLocation());

			// Save to file
			SLAPI.save(playerSaveInfo, getDoodyFileFor(player).getPath());
			loadDutyCache();
			dutyCache.add(playerName);
			plugin.getDebug().check("<setDoody> " + playerName + "'s data has been saved.");

			// Now we're certain the player's stuff is safe, we can take them
			// away from them.
			player.setLevel(0);
			player.setExp(0);
			inventory.clear();
			inventory.setHelmet(null);
			inventory.setChestplate(null);
			inventory.setLeggings(null);
			inventory.setBoots(null);

			// Put player in creative mode.
			player.setGameMode(GameMode.CREATIVE);
			MessageSender.send(player, "&6[DoOdy] &aYou're now on duty.");

			// Give duty tools
			dutyItems(player);
			
			return true;
		} catch (Exception e) {
			plugin.getLog().severe("Failed Storing data on /doody on");
			plugin.getLogger().throwing("DutyManager", "enableDutyFor", e);
			MessageSender.send(player, "&6[DoOdy] &cFailed storing your data. Could not place you on duty.");
			return false;
		}
	}

	// Remove Duty Mode
	public boolean disableDutyFor(Player player) {
		String playerName = player.getName();
		try {
			final File doodyFile = getDoodyFileFor(player);
			PlayerSaveInfo playerSaveInfo = (PlayerSaveInfo) SLAPI.load(doodyFile.getPath());

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

			// We've completely restored our friend. Delete their doody file.
			doodyFile.delete();
			loadDutyCache();
			dutyCache.remove(playerName);

			MessageSender.send(player, "&6[DoOdy] &aYou're no longer on duty.");
			plugin.getDebug().check("<removeDoody> " + playerName + "'s data restored & saved data deleted.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			plugin.getLog().warning("Failed restoring the inventory of " + playerName + ".");
			plugin.getLog().warning("Failed restoring the location of " + playerName + ".");
			MessageSender.send(player, "&6[DoOdy] &cFailed restoring you to pre-duty state. Plugin encountered error.");
			MessageSender.send(player, "&6[DoOdy] &cPlease try again.");
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
			String[] doodies = dataFolder.list(new FilenameFilter() {
				public boolean accept(File folder, String fileName) {
					return fileName.endsWith(".doody");
				}
			});

			HashSet<String> dutyList = new HashSet<String>();
			for (String doody : doodies) {
				doody = doody.substring(0, doody.indexOf(".doody"));
				dutyList.add(doody);
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
			MessageSender.send(player, "&6[DoOdy] &cFailed returning you to last duty location.");
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
