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

package net.alexanderschroeder.OnDoOdy;

import java.io.File;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.alexanderschroeder.OnDoOdy.command.DoOdyCommandExecutor;
import net.alexanderschroeder.OnDoOdy.config.ConfigurationManager;
import net.alexanderschroeder.OnDoOdy.listeners.BlockListener;
import net.alexanderschroeder.OnDoOdy.listeners.EntityListener;
import net.alexanderschroeder.OnDoOdy.listeners.PlayerListener;
import net.alexanderschroeder.OnDoOdy.util.Debug;
import net.alexanderschroeder.OnDoOdy.util.DutyManager;

public class OnDoOdy extends JavaPlugin {
	private static final String PLUGIN_NAME = "OnDoOdy";

	private Log log;
	private ConfigurationManager configurationManager;
	private Debug debug;
	private DutyManager dutyManager;

	@Override
	public void onDisable() {
		log.info(PLUGIN_NAME + " disabled!");
	}

	@Override
	public void onEnable() {
		log = new Log(this);
		log.info("Loading configs...");

		saveDefaultConfig();
		configurationManager = new ConfigurationManager(this);

		log.info("Loaded configs!");

		dutyManager = new DutyManager(this);
		debug = new Debug(this);
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new EntityListener(this), this);

		getCommand("ondoody").setExecutor(new DoOdyCommandExecutor(this));

		log.info(PLUGIN_NAME + " v" + getDescription().getVersion() + " enabled");
	}

	public Log getLog() {
		return log;
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public Debug getDebug() {
		return debug;
	}

	public DutyManager getDutyManager() {
		return dutyManager;
	}

	public String getPluginDataFilePath(String fileName) {
		File dataFolder = getPluginDataFolder();
		File file = new File(dataFolder, fileName);
		String filePath = file.getPath();
		return filePath;
	}

	public File getPluginDataFolder() {
		File pluginDataFolder = getPluginFolder();
		File dataFolder = new File(pluginDataFolder, "data");
		if (!dataFolder.exists())
			dataFolder.mkdirs();
		return dataFolder;
	}

	public String getPluginFilePath(String fileName) {
		File pluginDataFolder = getPluginFolder();
		File file = new File(pluginDataFolder, fileName);
		String filePath = file.getPath();
		return filePath;
	}

	public File getPluginFolder() {
		File pluginDataFolder = getDataFolder();
		if (!pluginDataFolder.exists())
			pluginDataFolder.mkdirs();
		return pluginDataFolder;
	}

	public static String getPluginName() {
		return PLUGIN_NAME;
	}

	@Override
	public String toString() {
		return getPluginName();
	}
}
