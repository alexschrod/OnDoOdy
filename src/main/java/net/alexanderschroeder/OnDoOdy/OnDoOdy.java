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
import java.util.logging.Logger;

import net.alexanderschroeder.OnDoOdy.command.DoOdyCommandExecutor;
import net.alexanderschroeder.OnDoOdy.listeners.BlockListener;
import net.alexanderschroeder.OnDoOdy.listeners.EntityListener;
import net.alexanderschroeder.OnDoOdy.listeners.PlayerListener;
import net.alexanderschroeder.OnDoOdy.managers.ConfigurationManager;
import net.alexanderschroeder.OnDoOdy.managers.DutyManager;
import net.alexanderschroeder.OnDoOdy.managers.InventorySaveInfo;
import net.alexanderschroeder.OnDoOdy.managers.LocationSaveInfo;
import net.alexanderschroeder.OnDoOdy.managers.PlayerMetadataManager;
import net.alexanderschroeder.OnDoOdy.managers.PlayerSaveInfo;
import net.alexanderschroeder.bukkitutil.DebugLogger;
import net.alexanderschroeder.bukkitutil.MessageSender;
import net.alexanderschroeder.bukkitutil.storage.FileConfigurationStorage;
import net.alexanderschroeder.bukkitutil.storage.Storage;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OnDoOdy extends JavaPlugin {
	private static final String PLUGIN_NAME = "OnDoOdy";

	private ConfigurationManager configurationManager;
	private DebugLogger debug;
	private DutyManager dutyManager;
	private PlayerMetadataManager playerMetadataManager;
	private MessageSender messageSender;
	private Storage storage;

	@Override
	public void onDisable() {
		PlayerSaveInfo.unregister();
		InventorySaveInfo.unregister();
		LocationSaveInfo.unregister();

		getLogger().info(PLUGIN_NAME + " disabled!");
	}

	@Override
	public void onEnable() {
		PlayerSaveInfo.register();
		InventorySaveInfo.register();
		LocationSaveInfo.register();

		final Logger logger = getLogger();

		logger.info("Loading configs...");

		saveDefaultConfig();
		configurationManager = new ConfigurationManager(this);

		logger.info("Loaded configs!");

		final FileConfigurationStorage fileConfigurationStorage = new FileConfigurationStorage();
		fileConfigurationStorage.setStorageDirectoryName("data");
		storage = fileConfigurationStorage;
		storage.initialize(this);

		messageSender = new MessageSender();
		messageSender.setPrefix(ChatColor.GOLD + "[" + PLUGIN_NAME + "] ");

		playerMetadataManager = new PlayerMetadataManager(this);
		dutyManager = new DutyManager(this);
		debug = new DebugLogger(this, configurationManager.isDebugModeEnabled());

		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new EntityListener(this), this);

		getCommand("ondoody").setExecutor(new DoOdyCommandExecutor(this));

		logger.info(PLUGIN_NAME + " v" + getDescription().getVersion() + " enabled");
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public DebugLogger getDebug() {
		return debug;
	}

	public DutyManager getDutyManager() {
		return dutyManager;
	}

	public PlayerMetadataManager getPlayerMetadataManager() {
		return playerMetadataManager;
	}

	public MessageSender getMessageSender() {
		return messageSender;
	}

	public Storage getStorage() {
		return storage;
	}

	public String getPluginDataFilePath(final String fileName) {
		final File dataFolder = getPluginDataFolder();
		final File file = new File(dataFolder, fileName);
		final String filePath = file.getPath();
		return filePath;
	}

	public File getPluginDataFolder() {
		final File pluginDataFolder = getPluginFolder();
		final File dataFolder = new File(pluginDataFolder, "data");
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		return dataFolder;
	}

	public String getPluginFilePath(final String fileName) {
		final File pluginDataFolder = getPluginFolder();
		final File file = new File(pluginDataFolder, fileName);
		final String filePath = file.getPath();
		return filePath;
	}

	public File getPluginFolder() {
		final File pluginDataFolder = getDataFolder();
		if (!pluginDataFolder.exists()) {
			pluginDataFolder.mkdirs();
		}
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
