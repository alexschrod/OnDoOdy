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

package net.alexanderschroeder.OnDoOdy.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfigurationManager {

	private static final String EXTRAPERMS_PREFIX = "doody.duty.extraperms.";
	private static final String DUTY_COMMANDS_PREFIX = "doody.duty.dutycommands.";

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

	private static final String ONDUTY_COMMANDS_KEY = "onduty-commands";
	private static final String OFFDUTY_COMMANDS_KEY = "offduty-commands";

	private static final String EXTRA_PERMISSIONS_KEY = "extra-permissions";

	private static final String INTERACTION_BLOCK_KEY = "blocks.interactions";

	private final OnDoOdy plugin;

	public ConfigurationManager(final OnDoOdy plugin) {
		this.plugin = plugin;
	}

	private FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	@SuppressWarnings("deprecation")
	private static Material getMaterial(final String itemValue) {
		int intValue;
		Material material;
		try {
			intValue = Integer.parseInt(itemValue);
			material = Material.getMaterial(intValue);
		} catch (final NumberFormatException e) {
			material = Material.getMaterial(itemValue);
			if (material == null) {
				material = Material.AIR;
			}
		}
		return material;
	}

	private static ItemStack getItem(final String itemValue) {
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

	public ItemStack getDutyItem(final int number) {
		final String configKey = String.format("%s-%d", DUTY_ITEM_KEY, number);
		final String configValue = getConfig().getString(configKey, "0");

		return getItem(configValue);
	}

	private List<Material> placeBlockListCache;

	public List<Material> getPlaceBlockList() {
		if (placeBlockListCache == null) {
			final List<String> placeBlockList = getConfig().getStringList(PLACE_BLOCK_KEY);
			placeBlockListCache = new ArrayList<Material>();
			for (final String block : placeBlockList) {
				final Material material = getMaterial(block);
				if (material == Material.AIR) {
					continue;
				}
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
		disallowedCommandListCache = null;
		interactionBlockListCache = null;

		final boolean previousHideSetting = hidePlayerOnDuty();
		plugin.reloadConfig();

		if (hidePlayerOnDuty() != previousHideSetting) {
			if (previousHideSetting) {
				plugin.getDutyManager().showAllDutyPlayers();
			} else {
				plugin.getDutyManager().hideAllDutyPlayers();
			}
		}

		for (final Player player : plugin.getDutyManager().getDutyPlayerSet()) {
			plugin.getDutyManager().giveExtraPermissions(player);
		}
	}

	private List<Material> breakBlockListCache;

	public List<Material> getBreakBlockList() {
		if (breakBlockListCache == null) {
			final List<String> breakBlockList = getConfig().getStringList(BREAK_BLOCK_KEY);
			breakBlockListCache = new ArrayList<Material>();
			for (final String block : breakBlockList) {
				final Material material = getMaterial(block);
				if (material == Material.AIR) {
					continue;
				}
				breakBlockListCache.add(material);
			}
		}
		return breakBlockListCache;
	}

	private List<Material> interactionBlockListCache;

	public List<Material> getInteractionBlockList() {
		if (interactionBlockListCache == null) {
			final List<String> interactionBlockList = getConfig().getStringList(INTERACTION_BLOCK_KEY);
			interactionBlockListCache = new ArrayList<Material>();
			for (final String block : interactionBlockList) {
				final Material material = getMaterial(block);
				if (material == Material.AIR) {
					continue;
				}
				interactionBlockListCache.add(material);
			}
		}
		return interactionBlockListCache;
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
			final List<String> disallowedCommandList = getConfig().getStringList(DISALLOWED_COMMAND_KEY);
			disallowedCommandListCache = new ArrayList<Command>();
			for (final String commandName : disallowedCommandList) {
				final Command command = plugin.getServer().getPluginCommand(commandName);
				disallowedCommandListCache.add(command);
			}
		}
		return disallowedCommandListCache;
	}

	private List<Material> itemDropListCache;

	public List<Material> getItemDropList() {
		if (itemDropListCache == null) {
			final List<String> dropList = getConfig().getStringList(DROP_KEY);
			itemDropListCache = new ArrayList<Material>();
			for (final String item : dropList) {
				final Material material = getMaterial(item);
				if (material == Material.AIR) {
					continue;
				}
				itemDropListCache.add(material);
			}
		}
		return itemDropListCache;
	}

	private List<Material> itemPickupListCache;

	public List<Material> getItemPickupList() {
		if (itemPickupListCache == null) {
			final List<String> dropList = getConfig().getStringList(PICKUP_KEY);
			itemPickupListCache = new ArrayList<Material>();
			for (final String item : dropList) {
				final Material material = getMaterial(item);
				if (material == Material.AIR) {
					continue;
				}
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

	public Set<String> getExtraPermissionSetFor(final Player player) {
		final ConfigurationSection extraPermissionsSection = getConfig().getConfigurationSection(EXTRA_PERMISSIONS_KEY);

		final Set<String> extraPermissions = new HashSet<String>();
		for (final String key : extraPermissionsSection.getKeys(false)) {
			final String permissionRequired = EXTRAPERMS_PREFIX + key;
			if (!player.hasPermission(permissionRequired)) {
				continue;
			}

			extraPermissions.addAll(extraPermissionsSection.getStringList(key));
		}
		return extraPermissions;
	}

	public class DutyCommand {

		private final String command;
		private final List<String> permissions;

		public DutyCommand(final String command, final List<String> permissions) {
			this.command = command;
			this.permissions = permissions;
		}

		public String getCommand() {
			return command;
		}

		public List<String> getPermissions() {
			return permissions;
		}
	}

	public List<DutyCommand> getOnDutyCommandsFor(final Player player) {
		final ConfigurationSection onDutyCommandsSection = getConfig().getConfigurationSection(ONDUTY_COMMANDS_KEY);
		return onDutyCommandsSection != null ? getDutyCommands(player, onDutyCommandsSection) : new ArrayList<DutyCommand>();
	}

	public List<DutyCommand> getOffDutyCommandsFor(final Player player) {
		final ConfigurationSection offDutyCommandsSection = getConfig().getConfigurationSection(OFFDUTY_COMMANDS_KEY);
		return offDutyCommandsSection != null ? getDutyCommands(player, offDutyCommandsSection) : new ArrayList<DutyCommand>();
	}

	private List<DutyCommand> getDutyCommands(final Player player, final ConfigurationSection commandsSection) {
		final List<DutyCommand> dutyCommands = new ArrayList<DutyCommand>();
		for (final String permissionName : commandsSection.getKeys(false)) {
			final String permissionRequired = DUTY_COMMANDS_PREFIX + permissionName;
			if (!player.hasPermission(permissionRequired)) {
				continue;
			}

			final ConfigurationSection permissionSection = commandsSection.getConfigurationSection(permissionName);
			for (final String commandName : permissionSection.getKeys(false)) {
				final ConfigurationSection commandSection = permissionSection.getConfigurationSection(commandName);

				final String command = commandSection.getString("command");
				if (command == null) {
					continue;
				}

				List<String> permissionList = commandSection.getStringList("permissions");
				if (permissionList == null) {
					permissionList = new ArrayList<String>();
				}

				dutyCommands.add(new DutyCommand(command, permissionList));
			}
		}

		return dutyCommands;
	}

}
