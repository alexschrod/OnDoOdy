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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.angelofdev.DoOdy.DoOdy;
import com.angelofdev.DoOdy.util.MessageSender;

public class BlockListener implements Listener {
	private DoOdy plugin;

	public BlockListener(DoOdy plugin) {
		this.plugin = plugin;

	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player))
			return;

		final Block block = event.getBlock();
		final Material material = block.getType();
		final boolean hasPlacePermission = player.hasPermission("doody.allowplace");
		final boolean isMaterialInMaterialList = plugin.getConfigurationManager().getPlaceBlockList().contains(material);
		final boolean hasPlaceAccess = hasPlacePermission || plugin.getConfigurationManager().isIncludeMode() ? isMaterialInMaterialList : !isMaterialInMaterialList;

		if (!hasPlaceAccess) {
			event.setCancelled(true);
			MessageSender.send(player, "&cYou may not place &e" + material.toString() + " &cwhile on duty.");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getDutyManager().isPlayerOnDuty(player))
			return;

		final Block block = event.getBlock();
		final boolean hasBreakPermission = player.hasPermission("doody.allowbreak");
		final Material material = block.getType();
		final boolean isMaterialInMaterialList = plugin.getConfigurationManager().getBreakBlockList().contains(material);
		final boolean hasBreakAccess = hasBreakPermission || plugin.getConfigurationManager().isIncludeMode() ? isMaterialInMaterialList : !isMaterialInMaterialList;

		if (!hasBreakAccess) {
			event.setCancelled(true);
			MessageSender.send(player, "&cYou may not break &e" + material.toString() + " &cwhile on Duty.");
		}
	}
}
