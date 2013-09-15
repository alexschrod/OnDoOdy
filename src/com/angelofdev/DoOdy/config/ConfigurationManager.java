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

package com.angelofdev.DoOdy.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.angelofdev.DoOdy.DoOdy;

public class ConfigurationManager {

	private static final String DEBUG_KEY = "debug";
	private static final boolean DEBUG_DEFAULT = false;

	private static final String WORLD_LIST_KEY = "worlds";

	private static final String INCLUDE_MODE_KEY = "include-mode";
	private static final boolean INCLUDE_MODE_DEFAULT = false;

	private static final Object DUTY_ITEM_KEY = "duty-tools.slot";

	private static final String PLACE_BLOCK_KEY = "blocks.place";
	private static final String BREAK_BLOCK_KEY = "blocks.break";

	private static final String ALLOW_PVP_KEY = "allow-pvp";
	private static final boolean ALLOW_PVP_DEFAULT = false;

	private static final String ALLOW_MOB_DAMAGE_KEY = "allow-mob-damage";
	private static final boolean ALLOW_MOB_DAMAGE_DEFAULT = false;

	private static final String DISALLOWED_COMMAND_KEY = "disallowed-commands";

	private static final String DROP_KEY = "drops";

	private static final String CREATIVE_INVENTORY_ALLOWED_KEY = "allow-creative-inventory";
	private static final boolean CREATIVE_INVENTORY_ALLOWED_DEFAULT = false;

	private static final String PICKUP_KEY = "pickups";

	private static final String ALLOW_INVENTORY_INTERACTION_KEY = "allow-inventory-interaction";
	private static final boolean ALLOW_INVENTORY_INTERACTION_DEFAULT = false;

	private static final String ALLOW_ITEM_DROPS_KEY = "allow-item-drops";
	private static final boolean ALLOW_ITEM_DROPS_DEFAULT = false;

	private static final String ALLOW_ITEM_PICKUPS_KEY = "allow-item-pickups";
	private static final boolean ALLOW_ITEM_PICKUPS_DEFAULT = false;

	private static final String ALLOW_BLOCK_PLACING_KEY = "allow-block-placing";
	private static final boolean ALLOW_BLOCK_PLACING_DEFAULT = false;

	private static final String ALLOW_BLOCK_BREAKING_KEY = "allow-block-breaking";
	private static final boolean ALLOW_BLOCK_BREAKING_DEFAULT = false;
	private static final String HIDE_ON_DUTY_KEY = "hide-on-duty";
	private static final boolean HIDE_ON_DUTY_DEFAULT = false;

	private DoOdy plugin;

	public ConfigurationManager(DoOdy plugin) {
		this.plugin = plugin;
	}

	private FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	@SuppressWarnings("deprecation")
	private Material getMaterial(String itemValue) {
		int intValue;
		Material material;
		try {
			intValue = Integer.parseInt(itemValue);
			material = Material.getMaterial(intValue);
		} catch (NumberFormatException e) {
			material = Material.getMaterial(itemValue);
			if (material == null)
				material = Material.AIR;
		}
		return material;
	}

	private ItemStack getItem(String itemValue) {
		return new ItemStack(getMaterial(itemValue), 1, (short) 0);
	}

	public boolean isDebugModeEnabled() {
		return getConfig().getBoolean(DEBUG_KEY, DEBUG_DEFAULT);
	}

	public boolean isIncludeMode() {
		return getConfig().getBoolean(INCLUDE_MODE_KEY, INCLUDE_MODE_DEFAULT);
	}

	public List<String> getWorldList() {
		return getConfig().getStringList(WORLD_LIST_KEY);
	}

	public ItemStack getDutyItem(int number) {
		String configKey = String.format("%s-%d", DUTY_ITEM_KEY, number);
		String configValue = getConfig().getString(configKey, "0");

		return getItem(configValue);
	}

	private List<Material> placeBlockListCache;

	public List<Material> getPlaceBlockList() {
		if (placeBlockListCache == null) {
			List<String> placeBlockList = getConfig().getStringList(PLACE_BLOCK_KEY);
			placeBlockListCache = new ArrayList<Material>();
			for (String block : placeBlockList) {
				Material material = getMaterial(block);
				if (material == Material.AIR)
					continue;
				placeBlockListCache.add(material);
			}
		}
		return placeBlockListCache;
	}

	public void reload() {
		placeBlockListCache = null;
		breakBlockListCache = null;
		itemDropListCache = null;
		itemPickupListCache = null;
		
		boolean previousHideSetting = hidePlayerOnDuty();
		plugin.reloadConfig();
		
		if (hidePlayerOnDuty() != previousHideSetting)
		{
			if (previousHideSetting)
				plugin.getDutyManager().showAllDutyPlayers();
			else
				plugin.getDutyManager().hideAllDutyPlayers();
		}
	}

	private List<Material> breakBlockListCache;

	public List<Material> getBreakBlockList() {
		if (breakBlockListCache == null) {
			List<String> placeBlockList = getConfig().getStringList(BREAK_BLOCK_KEY);
			breakBlockListCache = new ArrayList<Material>();
			for (String block : placeBlockList) {
				Material material = getMaterial(block);
				if (material == Material.AIR)
					continue;
				breakBlockListCache.add(material);
			}
		}
		return breakBlockListCache;
	}

	public boolean isPvPAllowed() {
		return getConfig().getBoolean(ALLOW_PVP_KEY, ALLOW_PVP_DEFAULT);
	}

	public boolean isMobDamageAllowed() {
		return getConfig().getBoolean(ALLOW_MOB_DAMAGE_KEY, ALLOW_MOB_DAMAGE_DEFAULT);
	}

	public boolean isCreativeInventoryAllowed() {
		return getConfig().getBoolean(CREATIVE_INVENTORY_ALLOWED_KEY, CREATIVE_INVENTORY_ALLOWED_DEFAULT);
	}

	private List<Command> disallowedCommandListCache;

	public List<Command> getDisallowedCommandList() {
		if (disallowedCommandListCache == null) {
			List<String> disallowedCommandList = getConfig().getStringList(DISALLOWED_COMMAND_KEY);
			disallowedCommandListCache = new ArrayList<Command>();
			for (String commandName : disallowedCommandList) {
				Command command = plugin.getServer().getPluginCommand(commandName);
				disallowedCommandListCache.add(command);
			}
		}
		return disallowedCommandListCache;
	}

	private List<Material> itemDropListCache;

	public List<Material> getItemDropList() {
		if (itemDropListCache == null) {
			List<String> dropList = getConfig().getStringList(DROP_KEY);
			itemDropListCache = new ArrayList<Material>();
			for (String item : dropList) {
				Material material = getMaterial(item);
				if (material == Material.AIR)
					continue;
				itemDropListCache.add(material);
			}
		}
		return itemDropListCache;
	}

	private List<Material> itemPickupListCache;

	public List<Material> getItemPickupList() {
		if (itemPickupListCache == null) {
			List<String> dropList = getConfig().getStringList(PICKUP_KEY);
			itemPickupListCache = new ArrayList<Material>();
			for (String item : dropList) {
				Material material = getMaterial(item);
				if (material == Material.AIR)
					continue;
				itemPickupListCache.add(material);
			}
		}
		return itemPickupListCache;
	}
	
	public boolean isInventoryInteractionAllowed() {
		return getConfig().getBoolean(ALLOW_INVENTORY_INTERACTION_KEY, ALLOW_INVENTORY_INTERACTION_DEFAULT);
	}
	
	public boolean isItemDroppingAllowed() {
		return getConfig().getBoolean(ALLOW_ITEM_DROPS_KEY, ALLOW_ITEM_DROPS_DEFAULT);
	}
	
	public boolean isItemPickupAllowed() {
		return getConfig().getBoolean(ALLOW_ITEM_PICKUPS_KEY, ALLOW_ITEM_PICKUPS_DEFAULT);
	}
	
	public boolean isBlockPlacingAllowed() {
		return getConfig().getBoolean(ALLOW_BLOCK_PLACING_KEY, ALLOW_BLOCK_PLACING_DEFAULT);
	}
	
	public boolean isBlockBreakingAllowed() {
		return getConfig().getBoolean(ALLOW_BLOCK_BREAKING_KEY, ALLOW_BLOCK_BREAKING_DEFAULT);
	}
	
	public boolean hidePlayerOnDuty() {
		return getConfig().getBoolean(HIDE_ON_DUTY_KEY, HIDE_ON_DUTY_DEFAULT);
	}

}
