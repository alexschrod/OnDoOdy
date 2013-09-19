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

import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import net.alexanderschroeder.OnDoOdy.exceptions.DutyException;
import net.alexanderschroeder.OnDoOdy.managers.DutyManager;
import net.alexanderschroeder.OnDoOdy.util.Debug;
import net.alexanderschroeder.OnDoOdy.util.MessageSender;

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
				} else {
					printCommandsToConsole(sender);
					return true;
				}
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
					onList(sender);
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
		MessageSender.sendTitle(player, "OnDoOdy Commands");
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
		final Set<String> dutyList = plugin.getDutyManager().getDutySet();
		if (player.hasPermission("doody.list") && !dutyList.isEmpty()) {
			MessageSender.send(player, "");
			onList(player);
		}
	}

	private void printCommandsToConsole(final CommandSender sender) {
		MessageSender.sendTitle(sender, "OnDoOdy Commands");
		MessageSender.send(sender, "&6Aliases: /dm, /duty, /dooty");
		MessageSender.send(sender, "&6/ondoody &b<player> <on/off> &fPut <player> <on/off> duty mode.");
		MessageSender.send(sender, "&6/ondoody &blist &fShows players on duty.");
		MessageSender.send(sender, "&6/ondoody &breload &fReload config.yml.");
		MessageSender.send(sender, "&6/ondoody &bdebug on/off &fEnable/Disable debug mode.");

		final Set<String> dutyList = plugin.getDutyManager().getDutySet();
		if (!dutyList.isEmpty()) {
			MessageSender.send(sender, "");
			onList(sender);
		}
	}

	private void onDebugOn(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.debug")) {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.debug");
				return;
			}
		}

		if (plugin.getConfigurationManager().isDebugModeEnabled()) {
			MessageSender.sendWithPrefix(sender, "&cDebug mode was already on!");
			return;
		}

		plugin.getDebug().enable();
		MessageSender.sendWithPrefix(sender, "&aDebug mode enabled!.");
		MessageSender.sendWithPrefix(sender, "&aDebug messages are output to server console/log.");
	}

	private void onDebugOff(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.debug")) {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.debug");
				return;
			}
		}

		if (!plugin.getConfigurationManager().isDebugModeEnabled()) {
			MessageSender.sendWithPrefix(sender, "&cDebug mode was not on!");
			return;
		}

		plugin.getDebug().disable();
		MessageSender.sendWithPrefix(sender, "&aDebug mode disabled!.");
		MessageSender.sendWithPrefix(sender, "&aI hope debugging shed some light on any issues you have with OnDoOdy.");
	}

	private void onOnDuty(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String playerName = player.getName();
			if (player.hasPermission("doody.duty")) {
				final DutyManager dutyManager = plugin.getDutyManager();
				final boolean isPlayerOnDuty = dutyManager.isPlayerOnDuty(player);
				if (isPlayerOnDuty) {
					MessageSender.sendWithPrefix(player, "&cYou're already on duty!");
					return;
				}

				final String worldName = player.getWorld().getName();
				final boolean hasWorldPermission = player.hasPermission("doody.worlds." + worldName);
				final boolean isWorldInWorldList = plugin.getConfigurationManager().getWorldList().contains(worldName);
				final boolean hasWorldAccess = hasWorldPermission || plugin.getConfigurationManager().isIncludeMode() ? isWorldInWorldList : !isWorldInWorldList;

				if (hasWorldAccess) {
					plugin.getDebug().check(playerName + " used /ondoody on");
					try {
						if (dutyManager.enableDutyFor(player)) {
							MessageSender.sendWithPrefix(player, "&aYou're now on duty.");
						} else {
							MessageSender.sendWithPrefix(player, "&cYou were prevented from going on duty!");
						}
					} catch (final DutyException e) {
						MessageSender.sendWithPrefix(player, "&cFailed storing your data. Could not place you on duty.");
					}
				} else {
					MessageSender.sendWithPrefix(player, "&cCannot go on duty in world &e" + worldName + " &c!");
				}
			} else {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.duty");
			}
		} else {
			MessageSender.sendWithPrefix(sender, "&cOnly players can go on duty!");
		}
	}

	private void onOnDuty(final CommandSender sender, final Player targetPlayer) {
		if (targetPlayer == null) {
			MessageSender.sendWithPrefix(sender, "&cThere is no player online with that user name!");
			return;
		}

		final String targetPlayerName = targetPlayer.getName();
		final boolean isAlreadyOnDuty = plugin.getDutyManager().isPlayerOnDuty(targetPlayer);

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.others")) {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.others");
				return;
			}
		}

		if (isAlreadyOnDuty) {
			MessageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis already on Duty!");
			return;
		}

		try {
			if (plugin.getDutyManager().enableDutyFor(targetPlayer)) {
				MessageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis now on duty!");
				MessageSender.sendWithPrefix(targetPlayer, "&aYou're now on duty.");
			} else {
				MessageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cwas prevented from going on duty!");
			}
		} catch (final DutyException e) {
			MessageSender.sendWithPrefix(sender, "&cFailed storing the data of &e" + targetPlayerName + " &c. Could not place them on duty.");
		}
	}

	private void onOffDuty(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String playerName = player.getName();
			final DutyManager dutyManager = plugin.getDutyManager();
			if (dutyManager.isPlayerOnDuty(player)) {
				plugin.getDebug().check(playerName + " used /ondoody off");
				try {
					if (dutyManager.disableDutyFor(player)) {
						MessageSender.sendWithPrefix(player, "&aYou're no longer on duty.");
					} else {
						MessageSender.sendWithPrefix(player, "&cYou were prevented from going off duty!");
					}
				} catch (final DutyException e) {
					MessageSender.sendWithPrefix(player, "&cFailed restoring you to pre-duty state. Plugin encountered error.");
					MessageSender.sendWithPrefix(player, "&cPlease try again.");
				}
				return;
			} else {
				MessageSender.sendWithPrefix(player, "&cYou're not on duty!");
			}
		}
	}

	private void onOffDuty(final CommandSender sender, final Player targetPlayer) {
		if (targetPlayer == null) {
			MessageSender.sendWithPrefix(sender, "&cThere is no player online with that user name!");
			return;
		}

		final String targetPlayerName = targetPlayer.getName();
		final boolean isOnDuty = plugin.getDutyManager().isPlayerOnDuty(targetPlayer);

		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.others")) {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.others");
				return;
			}
		}

		if (!isOnDuty) {
			MessageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis not on duty!");
			return;
		}

		try {
			if (plugin.getDutyManager().disableDutyFor(targetPlayer)) {
				MessageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cis now off duty!");
				MessageSender.sendWithPrefix(targetPlayer, "&aYou're no longer on duty.");
			} else {
				MessageSender.sendWithPrefix(sender, "&e" + targetPlayerName + " &cwas prevented from going off duty!");
			}
		} catch (final DutyException e) {
			MessageSender.sendWithPrefix(sender, "&cFailed restoring &e" + targetPlayerName + " &c to pre-duty state. Plugin encountered error.");
		}
	}

	private void onList(final CommandSender sender) {
		final Set<String> dutyList = plugin.getDutyManager().getDutySet();
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("doody.list")) {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.list");
				return;
			}
		}

		MessageSender.sendTitle(sender, "Players on duty");
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
			final Debug debug = plugin.getDebug();
			final DutyManager dutyManager = plugin.getDutyManager();
			if (dutyManager.isPlayerOnDuty(player)) {
				if (player.hasPermission("doody.back")) {
					if (dutyManager.hasDutyLocation(player)) {
						try {
							dutyManager.sendToDutyLocation(player);
							MessageSender.sendWithPrefix(player, "&aBack to last known duty location.");
							debug.check(playerName + " &ateleported back to last known duty location");
						} catch (final DutyException e) {
							plugin.getLog().warning("Could not restore " + playerName + " to their last duty location.");
							MessageSender.sendWithPrefix(player, "&cFailed returning you to last duty location.");
						}
					} else {
						MessageSender.sendWithPrefix(player, "&eYou have no last known duty location.");
						debug.check("<on /dm back> Last known duty location unknown.");
					}
				} else {
					MessageSender.sendWithPrefix(player, "&cNeed permission node doody.back");
				}
			} else {
				MessageSender.sendWithPrefix(player, "&eYou are not on duty.");
				debug.check("</dm back> " + playerName + " is not on duty.");
			}
		}
	}

	private void onReload(final CommandSender sender) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			player.getName();
			if (player.hasPermission("doody.reload")) {
				plugin.getConfigurationManager().reload();
				MessageSender.sendWithPrefix(player, "&aConfig.yml re-loaded.");
			} else {
				MessageSender.sendWithPrefix(player, "&cNeed permission node doody.reload");
			}
		} else {
			plugin.reloadConfig();
			plugin.getLog().info("Config.yml re-loaded.");
		}
	}
}
