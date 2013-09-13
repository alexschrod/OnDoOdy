/*
 *  OnDoOdy v1: Separates Admin/Mod duties so everyone can enjoy the game.
 *  Copyright (C) 2013  M.Y.Azad
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

package com.angelofdev.DoOdy.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.angelofdev.DoOdy.DoOdy;
import com.angelofdev.DoOdy.config.ConfigurationManager;
import com.angelofdev.DoOdy.util.Debug;
import com.angelofdev.DoOdy.util.DutyManager;
import com.angelofdev.DoOdy.util.MessageSender;

public class PlayerListener implements Listener {

	private DoOdy plugin;

	public PlayerListener(DoOdy plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player))
			return;

		String playerName = player.getName();
		String label = event.getMessage().substring(1).split(" ", 1)[0];

		List<Command> commands = plugin.getConfigurationManager().getDisallowedCommandList();
		boolean foundCommand = false;
		for (Command command : commands) {
			if (command.getLabel().equalsIgnoreCase(label)) {
				foundCommand = true;
			} else {
				for (String alias : command.getAliases()) {
					if (alias.equalsIgnoreCase(label)) {
						foundCommand = true;
					}
				}
			}

			if (foundCommand) {
				event.setCancelled(true);
				MessageSender.send(player, "&6[OnDoOdy] &cYou're not allowed to use this command on duty!");
				plugin.getDebug().check("<onPlayerCommandPreprocess> " + playerName + " tried executing command in disallowed commands.");
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE) {
			String playerName = player.getName();
			final Debug debug = plugin.getDebug();
			final boolean isPlayerOnDuty = plugin.getDutyManager().isPlayerOnDuty(player);
			if (isPlayerOnDuty) {
				MessageSender.send(player, "&6[OnDoOdy] &cNOTE: As you logged off while on duty, you are still on duty!");
			}
			if (isPlayerOnDuty || player.hasPermission("doody.failsafe.bypass")) {
				debug.checkBroadcast("&e" + playerName + " &a<was on duty&e|or|&ahas doody.failsafe.bypass>");
			} else {
				player.setGameMode(GameMode.SURVIVAL);
				player.getInventory().clear();
				debug.checkBroadcast("&e" + playerName + " &c<was illegally in creative mode>");
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player))
			return;

		String worldName = player.getWorld().getName();
		final boolean hasWorldPermission = player.hasPermission("doody.worlds." + worldName);
		final boolean isWorldInWorldList = plugin.getConfigurationManager().getWorldList().contains(worldName);
		final boolean hasWorldAccess = hasWorldPermission || plugin.getConfigurationManager().isIncludeMode() ? isWorldInWorldList : !isWorldInWorldList;

		if (!hasWorldAccess) {
			plugin.getDutyManager().disableDutyFor(player);
			MessageSender.send(player, "&6[OnDoOdy] &cCannot go to world &e" + worldName + " &cwhile on duty!");
		} else {
			String playerName = player.getName();
			plugin.getDebug().check("<onPlayerWorldChange> " + playerName + " Player has the permission 'doody.worlds." + worldName + "'");
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		final DutyManager dutyManager = plugin.getDutyManager();
		if (dutyManager.isPlayerOnDuty(player)) {
			dutyManager.saveLocation(player);
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		final DutyManager dutyManager = plugin.getDutyManager();
		if (dutyManager.isPlayerOnDuty(player)) {
			dutyManager.sendToLocation(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player))
			return;

		final String playerName = player.getName();
		final ConfigurationManager configurationManager = plugin.getConfigurationManager();

		final ItemStack itemStack = event.getItemDrop().getItemStack();
		final Material dropMaterial = itemStack.getType();
		final boolean hasDropPermission = player.hasPermission("doody.dropitems");
		final boolean allowDrop = configurationManager.isItemDroppingAllowed();
		final boolean isMaterialInMaterialList = configurationManager.getItemDropList().contains(dropMaterial);
		final boolean hasDropAccess = hasDropPermission || (allowDrop && (configurationManager.isIncludeMode() ? isMaterialInMaterialList : !isMaterialInMaterialList));

		final String itemName = MessageSender.getNiceNameOf(dropMaterial);
		if (hasDropAccess) {
			plugin.getDebug().normal("<onPlayerDropItem> Warning! " + "Allowing " + playerName + " to drop " + itemName);
		} else {
			event.setCancelled(true);

			MessageSender.send(player, "&6[OnDoOdy] &cYou may not drop &e" + itemName + "&c while on duty.");
			plugin.getDebug().check("<onPlayerDropItem> " + playerName + " got denied item drop. <Item(" + itemName + ")>");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player))
			return;

		final String playerName = player.getName();
		final ConfigurationManager configurationManager = plugin.getConfigurationManager();

		final ItemStack itemStack = event.getItem().getItemStack();
		final Material pickupMaterial = itemStack.getType();
		final boolean hasPickupPermission = player.hasPermission("doody.pickupitems");
		final boolean allowPickup = configurationManager.isItemPickupAllowed();
		final boolean isMaterialInMaterialList = configurationManager.getItemPickupList().contains(pickupMaterial);
		final boolean hasPickupAccess = hasPickupPermission || (allowPickup && (configurationManager.isIncludeMode() ? isMaterialInMaterialList : !isMaterialInMaterialList));

		final String itemName = MessageSender.getNiceNameOf(pickupMaterial);
		if (hasPickupAccess) {
			plugin.getDebug().normal("<onPlayerPickupItem> Warning! " + "Allowing " + playerName + " to pick up " + itemName);
		} else {
			event.setCancelled(true);

			// TODO: Find a way to tell the player this without sending them 20 messages per second...
			// MessageSender.send(player, "&6[OnDoOdy] &cYou may not pick up &e" + itemName + "&c while on duty.");
			// plugin.getDebug().check("<onPlayerPickupItem> " + playerName + " got denied item pickup. <Item(" + itemName + ")>");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCreativeInventory(InventoryCreativeEvent event) {
		final HumanEntity whoClicked = event.getWhoClicked();
		if (whoClicked instanceof Player) {
			final Player player = (Player) whoClicked;
			if (!plugin.getDutyManager().isPlayerOnDuty(player))
				return;

			final String playerName = player.getName();

			final boolean hasCreativeInventoryPermission = player.hasPermission("doody.allowcreativeinventory");
			final boolean allowCreativeInventory = plugin.getConfigurationManager().isCreativeInventoryAllowed();
			final boolean hasCreativeInventoryAccess = hasCreativeInventoryPermission || allowCreativeInventory;

			if (hasCreativeInventoryAccess) {
				plugin.getDebug().normal("<onCreativeInventory> Warning! " + "Allowing " + playerName + " to access creative inventory");
			} else {
				MessageSender.send(player, "&6[OnDoOdy] &cYou may not do anything with your inventory while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		final HumanEntity whoClicked = event.getWhoClicked();
		if (whoClicked instanceof Player) {
			final Player player = (Player) whoClicked;
			if (!plugin.getDutyManager().isPlayerOnDuty(player))
				return;

			// Should be allowed to modify own inventory (will be overridden by
			// not allowing access to creative inventory, however.)
			if (event.getView().getType() == InventoryType.PLAYER)
				return;

			final String playerName = player.getName();

			final boolean hasInventoryPermission = player.hasPermission("doody.inventory") || player.hasPermission("doody.storage");
			final boolean allowInventoryInteraction = plugin.getConfigurationManager().isInventoryInteractionAllowed();
			final boolean hasInventoryAccess = hasInventoryPermission || allowInventoryInteraction;

			if (hasInventoryAccess) {
				plugin.getDebug().normal("<onInventoryClick> Warning! " + "Allowing " + playerName + " to access inventory");
			} else {
				MessageSender.send(player, "&6[OnDoOdy] &cYou may not interact with inventories while on duty.");
				event.setCancelled(true);
			}
		}
	}
}
