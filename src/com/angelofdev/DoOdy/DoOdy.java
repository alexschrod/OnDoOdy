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

package com.angelofdev.DoOdy;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.angelofdev.DoOdy.command.DoOdyCommandExecutor;
import com.angelofdev.DoOdy.config.Configuration;
import com.angelofdev.DoOdy.listeners.BlockListener;
import com.angelofdev.DoOdy.listeners.EntityListener;
import com.angelofdev.DoOdy.listeners.PlayerListener;
import com.angelofdev.DoOdy.listeners.PlayerListener.SLAPI;

public class DoOdy extends JavaPlugin {
	private PlayerListener playerListener;
	private BlockListener blockListener;
	private EntityListener entityListener;
	private DoOdyCommandExecutor DoOdyCommandExecutor;
	private static String version;
	private static final String PLUGIN_NAME = "OnDoOdy";	

	public static DoOdy instance;
	
	@Override
	public void onDisable() {
		try {
			SLAPI.save(com.angelofdev.DoOdy.command.DoOdyCommandExecutor.dutyList, "plugins/DoOdy/data/dutyList.bin");
			Log.info("Saved list of players on duty.");
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
		Log.info(PLUGIN_NAME + " disabled!");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		version = pdfFile.getVersion();
		initialise();
		//initMetrics();

		Log.info("Loading configs...");
		
		//Load saved info.
		try {
			com.angelofdev.DoOdy.command.DoOdyCommandExecutor.dutyList = (ArrayList<String>)SLAPI.load("plugins/DoOdy/data/dutyList.bin");
			Log.info("Loaded list of players on duty.");
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
		
		Log.info("Cleaning up files!");
		
		File dutyList = new File ("plugins/DoOdy/data/dutyList.bin");
		try {
			dutyList.delete();
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
		
		Log.info("Files tidied!");
		
		Configuration.start();
		
		Log.info("Loaded configs!");
		
		PluginManager pm = getServer().getPluginManager();		
		pm.registerEvents(this.playerListener, this);
		pm.registerEvents(this.blockListener, this);
		pm.registerEvents(this.entityListener, this);

		DoOdyCommandExecutor = new DoOdyCommandExecutor(this);
		getCommand("doody").setExecutor(DoOdyCommandExecutor);
		
		Log.info(PLUGIN_NAME + " v" + version + " enabled");
	}	

	public static String getPluginName() {
		return PLUGIN_NAME;
	}
	
	@Override
	public String toString() {
		return getPluginName();
	}
	
	private void initialise() {
		playerListener = new PlayerListener();
		blockListener = new BlockListener();
		entityListener = new EntityListener();
		instance = this;
		
	}
	
	/*private void initMetrics() {
		try {
		    MetricsLite metrics = new MetricsLite(instance);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
	}*/	
}
