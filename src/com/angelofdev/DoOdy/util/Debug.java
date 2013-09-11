/*
 *  DoOdy v1: Separates Admin/Mod duties so everyone can enjoy the game.
 *  Copyright (C) 2013  M.Y.Azad
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

import com.angelofdev.DoOdy.Log;
import com.angelofdev.DoOdy.config.Configuration;


public class Debug {
	private static String pre = "[DEBUG]";
	private static String codes = "&([0-9a-fA-F])";
	private static String colour = "§$1";
	
	private Debug() {
	}
	
	public static void check(String args) {
		if (Configuration.config.getBoolean("Debug.enabled")) {
			Log.info(pre + " " + args);
		}
	}
	
	public static void normal(String args) {
		Log.info(pre + " " + args);
	}
	
	public static void severe(String args) {
		Log.severe(pre + " " + args);
	}
	
	public static void checkBroadcast(String args) {
		if (Configuration.config.getBoolean("Debug.enabled")) {
			Bukkit.getConsoleSender().sendMessage(pre + " " + args.replaceAll(codes, colour));
		}
	}
}
