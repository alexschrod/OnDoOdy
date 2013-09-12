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

package com.angelofdev.DoOdy;

import java.util.logging.Level;

public class Log {
	private DoOdy plugin;

	public Log(DoOdy plugin) {
		this.plugin = plugin;
	}

	public void info(String message) {
		plugin.getLogger().log(Level.INFO, message);
	}

	public void warning(String message) {
		plugin.getLogger().log(Level.WARNING, message);
	}

	public void severe(String message) {
		plugin.getLogger().log(Level.SEVERE, message);
	}
}
