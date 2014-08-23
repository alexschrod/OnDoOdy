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

package net.alexanderschroeder.OnDoOdy.command;

import java.util.Set;
import java.util.logging.Logger;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import net.alexanderschroeder.OnDoOdy.exceptions.DutyException;
import net.alexanderschroeder.OnDoOdy.managers.DutyManager;
import net.alexanderschroeder.bukkitutil.MessageSender;
import net.alexanderschroeder.bukkitutil.storage.StorageException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DoOdyCommandExecutor implements CommandExecutor {

	private final OnDoOdy plugin;

	public DoOdyCommandExecutor(final OnDoOdy plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (cmd.getName().equalsIgnoreCase("ondoody")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					final Player player = (Player) sender;
					printCommandsTo(player);
					return true;
				}
				printCommandsToConsole(sender);
				return true;
			} else if (args.length == 1) {
				// /dm on
				if (args[0].equalsIgnoreCase("on")) {
					onOnDuty(sender);
					return true;
				}

				// /dm off
				else if (args[0].equalsIgnoreCase("off")) {
					onOffDuty(sender);
					return true;
				}

				// /dm list
				else if (args[0].equalsIgnoreCase("list")) {
					onList(sender, null);
					return true;
				}

				// /dm back
				else if (args[0].equalsIgnoreCase("back")) {
					onBack(sender);
					return true;
				}

				// /dm reload
				else if (args[0].equalsIgnoreCase("reload")) {
					onReload(sender);
					return true;
				}
			} else if (args.length == 2) {

				// dm debug <on/off>
				if (args[0].equalsIgnoreCase("debug")) {
					if (args[1].equalsIgnoreCase("on")) {
						onDebugOn(sender);
						return true;
					} else if (args[1].equalsIgnoreCase("off")) {
						onDebugOff(sender);
						return true;
					} else {
						return false;
					}
				}

				final Player targetPlayer = plugin.getServer().getPlayer(args[0]);

				// /dm <player> on
				if (args[1].equalsIgnoreCase("on")) {
					onOnDuty(sender, targetPlayer);
					return true;
				}

				// dm <player> off
				else if (args[1].equalsIgnoreCase("off")) {
					onOffDuty(sender, targetPlayer);
					return true;
				}
			}
		}
		return false;
	}

	private void printCommandsTo(final Player player) {
		plugin.getMessageSender().sendTitle(player, "OnDoOdy Commands", 42);
		MessageSender.send(player, "&6Aliases: &b/dm, /duty, /dooty");

		if (player.hasPermission("doody.duty")) {
			MessageSender.send(player, "&6/ondoody &bon &fTurns on duty mode.");
			MessageSender.send(player, "&6/ondoody &boff &fTurns off duty mode.");
		}
		if (player.hasPermission("doody.others")) {
			MessageSender.send(player, "&6/ondoody &b<player> <on/off> &fPut <player> <on/off> duty mode.");
		}
		if (player.hasPermission("doody.list")) {
			MessageSender.send(player, "&6/ondoody &blist &fShows players on duty.");
		}
		if (player.hasPermission("doody.back")) {
			MessageSender.send(player, "&6/ondoody &bback &fTake you back to where you last went off duty.");
		}
		if (player.hasPermission("doody.reload")) {
			MessageSender.send(player, "&6/ondoody &breload &fReload config.yml.");
		}
		if (player.hasPermission("doody.debug")) {
			MessageSender.send(player, "&6/ondoody &bdebug on/off &fEnable/Disable debug mode.");
		}
		if (player.hasPermission("doody.list")) {
			final Set<String> dutyList = plugin.getDutyManager().getDutySet();

			if (!dutyList.isEmpty()) {
				MessageSender.send(player, "");
				onList(player, dutyList);
			}
		}
	}

	private void printCommandsToConsole(final CommandSender sender) {
		plugin.getMessageSender().sendTitle(sender, "OnDoOdy Commands", 42);
		MessageSender.send(sender, "&6Aliases: /dm, /duty, /dooty");
		MessageSender.send(sender, "&6/ondoody &b<player> <on/off> &fPut <player> <on/off> duty mode.");
		MessageSender.send(sender, "&6/ondoody &blist &fShows players on duty.");
		MessageSender.send(sender, "&6/ondoody &breload &fReload config.yml.");
		MessageSender.send(sender, "&6/ondoody &bdebug on/off &fEnable/Disable debug mode.");

		final Set<String> dutyList = plugin.getDutyManager().getDutySet();
		if (!dutyList.isEmpty()) {
			MessageSender.send(sender, "");
			onList(sender, null);
		}
	}

	private void onDebugOn(final CommandSender sender) {
		final MessageSender messageSender = plugin.getMessageSender();

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.debug")) {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.debug");
				return;
			}
		}

		if (plugin.getConfigurationManager().isDebugModeEnabled()) {
			messageSender.sendWithPrefix(sender, "&cDebug mode was already on!");
			return;
		}

		plugin.getDebug().setEnabled(true);
		messageSender.sendWithPrefix(sender, "&aDebug mode enabled!.");
		messageSender.sendWithPrefix(sender, "&aDebug messages are output to server console/log.");
	}

	private void onDebugOff(final CommandSender sender) {
		final MessageSender messageSender = plugin.getMessageSender();

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.debug")) {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.debug");
				return;
			}
		}

		if (!plugin.getConfigurationManager().isDebugModeEnabled()) {
			messageSender.sendWithPrefix(sender, "&cDebug mode was not on!");
			return;
		}

		plugin.getDebug().setEnabled(false);
		messageSender.sendWithPrefix(sender, "&aDebug mode disabled!.");
		messageSender.sendWithPrefix(sender, "&aI hope debugging shed some light on any issues you have with OnDoOdy.");
	}

	private void onOnDuty(final CommandSender sender) {
		final MessageSender messageSender = plugin.getMessageSender();

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String playerName = player.getName();
			
			plugin.getDebug().info(playerName + " is attempting to go on duty. Has duty permission: " + player.hasPermission("doody.duty"));
			if (player.hasPermission("doody.duty")) {
				final DutyManager dutyManager = plugin.getDutyManager();
				boolean isPlayerOnDuty;
				try {
					isPlayerOnDuty = dutyManager.isPlayerOnDuty(player);
				} catch (StorageException e1) {
					messageSender.sendWithPrefix(player, "&cAn error occured while attempting to check if you were already on duty.");
					throw e1;
				}
				if (isPlayerOnDuty) {
					messageSender.sendWithPrefix(player, "&cYou're already on duty!");
					return;
				}

				final String worldName = player.getWorld().getName();
				final boolean hasWorldPermission = player.hasPermission("doody.worlds." + worldName);
				final boolean isWorldInWorldList = plugin.getConfigurationManager().getWorldList().contains(worldName);
				final boolean hasWorldAccess = hasWorldPermission || (plugin.getConfigurationManager().isIncludeMode() ? isWorldInWorldList : !isWorldInWorldList);
				
				plugin.getDebug().info(String.format("World: %s, hasWorldPermission: %b, isWorldInWorldList: %b, hasWorldAccess: %b", worldName, hasWorldPermission, isWorldInWorldList, hasWorldAccess));
				
				if (hasWorldAccess) {
					plugin.getDebug().info(playerName + " used /ondoody on");
					try {
						if (dutyManager.enableDutyFor(player)) {
							messageSender.sendWithPrefix(player, "&aYou're now on duty.");
						} else {
							messageSender.sendWithPrefix(player, "&cYou were prevented from going on duty!");
						}
					} catch (final DutyException e) {
						messageSender.sendWithPrefix(player, "&cFailed storing your data. Could not place you on duty.");
						throw new RuntimeException(e);
					}
				} else {
					messageSender.sendWithPrefix(player, "&cCannot go on duty in world &e" + worldName + " &c!");
				}
			} else {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.duty");
			}
		} else {
			messageSender.sendWithPrefix(sender, "&cOnly players can go on duty!");
		}
	}

	private void onOnDuty(final CommandSender sender, final Player targetPlayer) {
		final MessageSender messageSender = plugin.getMessageSender();
		if (targetPlayer == null) {
			messageSender.sendWithPrefix(sender, "&cThere is no player online with that user name!");
			return;
		}

		final String targetPlayerName = targetPlayer.getName();
		boolean isAlreadyOnDuty;
		try {
			isAlreadyOnDuty = plugin.getDutyManager().isPlayerOnDuty(targetPlayer);
		} catch (StorageException e1) {
			messageSender.sendWithPrefix(sender, "&cAn error occured while attempting to check if " + targetPlayerName + " was already on duty.");
			throw e1;
		}

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.others")) {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.others");
				return;
			}
		}

		if (isAlreadyOnDuty) {
			messageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis already on Duty!");
			return;
		}

		try {
			if (plugin.getDutyManager().enableDutyFor(targetPlayer)) {
				messageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis now on duty!");
				messageSender.sendWithPrefix(targetPlayer, "&aYou're now on duty.");
			} else {
				messageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cwas prevented from going on duty!");
			}
		} catch (final DutyException e) {
			messageSender.sendWithPrefix(sender, "&cFailed storing the data of &e" + targetPlayerName + " &c. Could not place them on duty.");
		}
	}

	private void onOffDuty(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String playerName = player.getName();
			final DutyManager dutyManager = plugin.getDutyManager();
			final MessageSender messageSender = plugin.getMessageSender();
			boolean isPlayerOnDuty;
			try {
				isPlayerOnDuty = dutyManager.isPlayerOnDuty(player);
			} catch (StorageException e1) {
				messageSender.sendWithPrefix(player, "&cAn error occured while attempting to check if you were already on duty.");
				throw e1;
			}
			if (isPlayerOnDuty) {
				plugin.getDebug().info(playerName + " used /ondoody off");
				try {
					if (dutyManager.disableDutyFor(player)) {
						messageSender.sendWithPrefix(player, "&aYou're no longer on duty.");
					} else {
						messageSender.sendWithPrefix(player, "&cYou were prevented from going off duty!");
					}
				} catch (final DutyException e) {
					messageSender.sendWithPrefix(player, "&cFailed restoring you to pre-duty state. Plugin encountered error.");
					messageSender.sendWithPrefix(player, "&cPlease try again.");
				}
			} else {
				messageSender.sendWithPrefix(player, "&cYou're not on duty!");
			}
		}
	}

	private void onOffDuty(final CommandSender sender, final Player targetPlayer) {
		final MessageSender messageSender = plugin.getMessageSender();

		if (targetPlayer == null) {
			messageSender.sendWithPrefix(sender, "&cThere is no player online with that user name!");
			return;
		}

		final String targetPlayerName = targetPlayer.getName();
		boolean isOnDuty;
		try {
			isOnDuty = plugin.getDutyManager().isPlayerOnDuty(targetPlayer);
		} catch (StorageException e1) {
			messageSender.sendWithPrefix(sender, "&cAn error occured while attempting to check if " + targetPlayerName + " was already on duty.");
			throw e1;
		}

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.others")) {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.others");
				return;
			}
		}

		if (!isOnDuty) {
			messageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis not on duty!");
			return;
		}

		try {
			if (plugin.getDutyManager().disableDutyFor(targetPlayer)) {
				messageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis now off duty!");
				messageSender.sendWithPrefix(targetPlayer, "&aYou're no longer on duty.");
			} else {
				messageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cwas prevented from going off duty!");
			}
		} catch (final DutyException e) {
			messageSender.sendWithPrefix(sender, "&cFailed restoring &e" + targetPlayerName + " &c to pre-duty state. Plugin encountered error.");
		}
	}

	private void onList(final CommandSender sender, Set<String> dutyList) {
		final MessageSender messageSender = plugin.getMessageSender();
		if (dutyList == null) {
			try {
				dutyList = plugin.getDutyManager().getDutySet();
			} catch (StorageException e) {
				messageSender.sendWithPrefix(sender, "&cAn error occurred when attempting to get the list of players on duty.");
				throw e;
			}
		}
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.list")) {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.list");
				return;
			}
		}

		messageSender.sendTitle(sender, "Players on duty", 42);
		if (!dutyList.isEmpty()) {
			MessageSender.send(sender, "&6" + dutyList);
		} else {
			MessageSender.send(sender, "&6No players are on duty.");
		}
	}

	private void onBack(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String playerName = player.getName();
			final Logger debug = plugin.getDebug();
			final DutyManager dutyManager = plugin.getDutyManager();
			final MessageSender messageSender = plugin.getMessageSender();
			boolean isPlayerOnDuty;
			try {
				isPlayerOnDuty = dutyManager.isPlayerOnDuty(player);
			} catch (StorageException e1) {
				messageSender.sendWithPrefix(player, "&cAn error occured while attempting to check if you were already on duty.");
				throw e1;
			}
			if (isPlayerOnDuty) {
				if (player.hasPermission("doody.back")) {
					boolean hasDutyLocation;
					try {
						hasDutyLocation = dutyManager.hasDutyLocation(player);
					} catch (StorageException e1) {
						messageSender.sendWithPrefix(player, "&cAn error occured while attempting to check if you have a duty location to go back to.");
						throw e1;
					}
					if (hasDutyLocation) {
						try {
							dutyManager.sendToDutyLocation(player);
							messageSender.sendWithPrefix(player, "&aBack to last known duty location.");
							debug.info(playerName + " &ateleported back to last known duty location");
						} catch (final DutyException e) {
							plugin.getLogger().warning("Could not restore " + playerName + " to their last duty location.");
							messageSender.sendWithPrefix(player, "&cFailed returning you to last duty location.");
						}
					} else {
						messageSender.sendWithPrefix(player, "&eYou have no last known duty location.");
						debug.info("Last known duty location unknown.");
					}
				} else {
					messageSender.sendWithPrefix(player, "&cNeed permission node doody.back");
				}
			} else {
				messageSender.sendWithPrefix(player, "&eYou are not on duty.");
				debug.info(playerName + " is not on duty.");
			}
		}
	}

	private void onReload(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final MessageSender messageSender = plugin.getMessageSender();
			if (player.hasPermission("doody.reload")) {
				plugin.getConfigurationManager().reload();
				messageSender.sendWithPrefix(player, "&aConfig.yml re-loaded.");
			} else {
				messageSender.sendWithPrefix(player, "&cNeed permission node doody.reload");
			}
		} else {
			plugin.reloadConfig();
			plugin.getLogger().info("Config.yml re-loaded.");
		}
	}
}
