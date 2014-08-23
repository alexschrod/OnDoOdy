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

package net.alexanderschroeder.OnDoOdy.listeners;

import java.util.List;
import java.util.logging.Logger;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import net.alexanderschroeder.OnDoOdy.exceptions.DutyException;
import net.alexanderschroeder.OnDoOdy.managers.ConfigurationManager;
import net.alexanderschroeder.OnDoOdy.managers.DutyManager;
import net.alexanderschroeder.bukkitutil.MessageSender;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	private final OnDoOdy plugin;

	public PlayerListener(final OnDoOdy plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
			return;
		}

		final String playerName = player.getName();
		final String label = event.getMessage().substring(1).split(" ", 1)[0];

		final List<Command> commands = plugin.getConfigurationManager().getDisallowedCommandList();
		boolean foundCommand = false;
		for (final Command command : commands) {
			if (command.getLabel().equalsIgnoreCase(label)) {
				foundCommand = true;
			} else {
				for (final String alias : command.getAliases()) {
					if (alias.equalsIgnoreCase(label)) {
						foundCommand = true;
					}
				}
			}

			if (foundCommand) {
				event.setCancelled(true);
				plugin.getMessageSender().sendWithPrefix(player, "&cYou're not allowed to use this command on duty!");
				plugin.getDebug().info(playerName + " tried executing command in disallowed commands.");
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final DutyManager dutyManager = plugin.getDutyManager();
		if (player.getGameMode() == GameMode.CREATIVE) {
			final String playerName = player.getName();
			final Logger debug = plugin.getDebug();
			final boolean isPlayerOnDuty = dutyManager.isPlayerOnDuty(player);
			if (isPlayerOnDuty) {
				plugin.getMessageSender().sendWithPrefix(player, "&cNOTE: As you logged off while on duty, you are still on duty!");

				dutyManager.hidePlayerOnDuty(player);
				dutyManager.giveExtraPermissions(player);
			}
			if (isPlayerOnDuty || player.hasPermission("doody.failsafe.bypass")) {
				debug.info(playerName + " &aleft in creative mode, due to being on duty or having doody.failsafe.bypass permission.");
			} else {
				player.setGameMode(GameMode.SURVIVAL);
				player.getInventory().clear();

				debug.info(playerName + " &ahas been taken out of creative mode, and all their items have been removed.");
			}
		}

		// When a new player joins, hide all the on-duty players from them,
		// unless they have the doody.seehidden permission
		if (plugin.getConfigurationManager().hidePlayerOnDuty() && !player.hasPermission("doody.seehidden")) {
			for (final Player dutyPlayer : dutyManager.getDutyPlayerSet()) {
				player.hidePlayer(dutyPlayer);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerWorldChange(final PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
			return;
		}

		final String worldName = player.getWorld().getName();
		final boolean hasWorldPermission = player.hasPermission("doody.worlds." + worldName);
		final boolean isWorldInWorldList = plugin.getConfigurationManager().getWorldList().contains(worldName);
		final boolean hasWorldAccess = hasWorldPermission || plugin.getConfigurationManager().isIncludeMode() ? isWorldInWorldList : !isWorldInWorldList;

		if (!hasWorldAccess) {
			try {
				plugin.getDutyManager().disableDutyFor(player);
			} catch (final DutyException e) {
				plugin.getLogger().severe("Could not stop " + player.getName() + " from going to world " + worldName + " while on duty!");
				return;
			}
			plugin.getMessageSender().sendWithPrefix(player, "&cCannot go to world &e" + worldName + " &cwhile on duty!");
		} else {
			final String playerName = player.getName();
			plugin.getDebug().info(playerName + " Player has the permission 'doody.worlds." + worldName + "'");
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final DutyManager dutyManager = plugin.getDutyManager();
		if (dutyManager.isPlayerOnDuty(player)) {
			try {
				dutyManager.saveLocation(player);
			} catch (final DutyException e) {
				plugin.getLogger().warning("Could not save the location of " + player.getName() + " on their death.");
			}
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		final DutyManager dutyManager = plugin.getDutyManager();
		if (dutyManager.isPlayerOnDuty(player)) {
			try {
				dutyManager.sendToDutyLocation(player);
			} catch (final DutyException e) {
				plugin.getLogger().warning("Failed restoring " + player.getName() + " to their death location upon respawn.");
				plugin.getMessageSender().sendWithPrefix(player, "&cFailed returning you to your death location.");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
			return;
		}

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
			plugin.getDebug().warning("Allowing " + playerName + " to drop " + itemName);
		} else {
			event.setCancelled(true);

			plugin.getMessageSender().sendWithPrefix(player, "&cYou may not drop &e" + itemName + "&c while on duty.");
			plugin.getDebug().info(playerName + " got denied item drop. <Item(" + itemName + ")>");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
			return;
		}

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
			plugin.getDebug().warning("Allowing " + playerName + " to pick up " + itemName);
		} else {
			final Material lastMaterial = (Material) plugin.getPlayerMetadataManager().getMetadata(player, "last-pickup-material");
			if (lastMaterial == null || lastMaterial != pickupMaterial) {
				plugin.getMessageSender().sendWithPrefix(player, "&cYou may not pick up &e" + itemName + "&c while on duty.");
				plugin.getPlayerMetadataManager().setMetadata(player, "last-pickup-material", pickupMaterial);
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCreativeInventory(final InventoryCreativeEvent event) {
		final HumanEntity whoClicked = event.getWhoClicked();
		if (whoClicked instanceof Player) {
			final Player player = (Player) whoClicked;
			if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
				return;
			}

			final String playerName = player.getName();

			final boolean hasCreativeInventoryPermission = player.hasPermission("doody.allowcreativeinventory");
			final boolean allowCreativeInventory = plugin.getConfigurationManager().isCreativeInventoryAllowed();
			final boolean hasCreativeInventoryAccess = hasCreativeInventoryPermission || allowCreativeInventory;

			if (hasCreativeInventoryAccess) {
				plugin.getDebug().warning("Allowing " + playerName + " to access creative inventory");
			} else {
				plugin.getMessageSender().sendWithPrefix(player, "&cYou may not do anything with your inventory while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(final InventoryClickEvent event) {
		final HumanEntity whoClicked = event.getWhoClicked();
		if (whoClicked instanceof Player) {
			final Player player = (Player) whoClicked;
			if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
				return;
			}

			// Should be allowed to modify own inventory (will be overridden by
			// not allowing access to creative inventory, however.)
			if (event.getView().getType() == InventoryType.PLAYER) {
				return;
			}

			final String playerName = player.getName();

			final boolean hasInventoryPermission = player.hasPermission("doody.inventory") || player.hasPermission("doody.storage");
			final boolean allowInventoryInteraction = plugin.getConfigurationManager().isInventoryInteractionAllowed();
			final boolean hasInventoryAccess = hasInventoryPermission || allowInventoryInteraction;

			if (hasInventoryAccess) {
				plugin.getDebug().info("Allowing " + playerName + " to access inventory");
			} else {
				plugin.getMessageSender().sendWithPrefix(player, "&cYou may not interact with inventories while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player)) {
			return;
		}

		final Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}

		final ConfigurationManager configurationManager = plugin.getConfigurationManager();
		final Material blockMaterial = block.getType();

		final boolean hasInteractPermission = player.hasPermission("doody.allowinteract");
		final boolean isMaterialInMaterialList = configurationManager.getInteractionBlockList().contains(blockMaterial);
		final boolean hasInteractionAccess = hasInteractPermission || !isMaterialInMaterialList;

		if (!hasInteractionAccess) {
			final Material lastMaterial = (Material) plugin.getPlayerMetadataManager().getMetadata(player, "last-interact-material");
			if (lastMaterial == null || lastMaterial != blockMaterial) {
				final String blockName = MessageSender.getNiceNameOf(blockMaterial);
				plugin.getMessageSender().sendWithPrefix(player, "&cYou may not interact with &e" + blockName + "&c while on duty.");
				plugin.getPlayerMetadataManager().setMetadata(player, "last-interact-material", blockMaterial);
			}
			event.setCancelled(true);
			return;
		}

		final BlockFace blockFace = event.getBlockFace();
		final Block relativeBlock = block.getRelative(blockFace);
		final Material fireMaterial = Material.FIRE;
		if (relativeBlock.getType() == fireMaterial) {

			final boolean hasBreakPermission = player.hasPermission("doody.allowbreak");
			final boolean allowBreak = configurationManager.isBlockBreakingAllowed();
			final boolean isFireInMaterialList = configurationManager.getBreakBlockList().contains(fireMaterial);
			final boolean hasBreakAccess = hasBreakPermission || (allowBreak && (configurationManager.isIncludeMode() ? isFireInMaterialList : !isFireInMaterialList));

			if (!hasBreakAccess) {
				event.setCancelled(true);
				player.sendBlockChange(relativeBlock.getLocation(), fireMaterial, (byte) 0);
				plugin.getMessageSender().sendWithPrefix(player, "&cYou may not put out &e" + MessageSender.getNiceNameOf(fireMaterial) + " &cwhile on Duty.");
			}
		}
	}
}
