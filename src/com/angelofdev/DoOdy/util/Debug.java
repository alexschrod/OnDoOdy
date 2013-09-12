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

package com.angelofdev.DoOdy.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.angelofdev.DoOdy.DoOdy;

public class Debug {
	private static String pre = "[DEBUG]";

	private DoOdy plugin;

	public Debug(DoOdy plugin) {
		this.plugin = plugin;
	}

	public void check(String args) {
		if (plugin.getConfigurationManager().isDebugModeEnabled()) {
			plugin.getLog().info(pre + " " + args);
		}
	}

	public void normal(String args) {
		plugin.getLog().info(pre + " " + args);
	}

	public void severe(String args) {
		plugin.getLog().severe(pre + " " + args);
	}

	public void checkBroadcast(String args) {
		if (plugin.getConfigurationManager().isDebugModeEnabled()) {
			Bukkit.getConsoleSender().sendMessage(pre + " " + ChatColor.translateAlternateColorCodes('&', args));
		}
	}

	public void enable() {
		plugin.getConfig().set("debug", true);
	}

	public void disable() {
		plugin.getConfig().set("debug", false);
	}
}
