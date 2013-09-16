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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import net.alexanderschroeder.OnDoOdy.Log;
import net.alexanderschroeder.OnDoOdy.exceptions.DutyException;
import net.alexanderschroeder.OnDoOdy.util.Debug;
import net.alexanderschroeder.OnDoOdy.util.DutyManager;
import net.alexanderschroeder.OnDoOdy.util.MessageSender;

public class DoOdyCommandExecutor implements CommandExecutor {

	private OnDoOdy plugin;

	public DoOdyCommandExecutor(OnDoOdy plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ondoody")) {
			if (args.length == 0) {
				if ((sender instanceof Player)) {
					Player player = (Player) sender;
					printCommandsTo(player);
					return true;
				} else {
					printCommandsToConsole();
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

				Player targetPlayer = plugin.getServer().getPlayer(args[0]);

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

	private void printCommandsTo(Player player) {
		MessageSender.send(player, "&a____________[ &6OnDoOdy Commands &a]____________");
		MessageSender.send(player, "&a________[ &6Short: /dm, /duty, /dooty &a]_______");

		if (player.hasPermission("doody.duty")) {
			MessageSender.send(player, "&6/ondoody &bon &fTurns on duty mode.");
			MessageSender.send(player, "&6/ondoody &boff &fTurns off duty mode.");
		}
		if (player.hasPermission("doody.others")) {
			MessageSender.send(player, "&6/ondoody &b<player> <on/off> &fPut <player> <on/off> duty mode.");
		}
		MessageSender.send(player, "&6/ondoody &blist &fShows players on duty.");
		if (player.hasPermission("doody.reload")) {
			MessageSender.send(player, "&6/ondoody &breload &fReload config.yml.");
		}
		if (player.hasPermission("doody.debug")) {
			MessageSender.send(player, "&6/ondoody &bdebug on/off &fEnable/Disable debug mode.");
		}
		Set<String> dutyList = plugin.getDutyManager().getDutySet();
		if (!dutyList.isEmpty()) {
			MessageSender.send(player, "&a____________[ &6Players on duty &a]____________");
			MessageSender.send(player, "&6" + dutyList);
		}
	}

	private void printCommandsToConsole() {
		final Log log = plugin.getLog();
		log.info("____________[ OnDoOdy Commands ]____________");
		log.info("____________[ Short: /dm, /duty ]____________");
		log.info("/ondoody <player> <on/off> [Put <player> <on/off> duty mode.]");
		log.info("/ondoody list [Shows players on duty.]");
		log.info("/ondoody reload [Reload config.yml.]");
		log.info("/ondoody debug <on/off> [Enable/disable debug mode]");
	}

	private void onDebugOn(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			if (!player.hasPermission("doody.debug")) {
				MessageSender.send(player, "&6[OnDoOdy] &cNeed permission node doody.debug");
				return;
			}
		}

		if (plugin.getConfigurationManager().isDebugModeEnabled()) {
			MessageSender.send(sender, "&6[OnDoOdy] &cDebug mode was already on!");
			return;
		}

		plugin.getDebug().enable();
		MessageSender.send(sender, "&6[OnDoOdy] &aDebug mode enabled!.");
		MessageSender.send(sender, "&6[OnDoOdy] &aDebug messages are output to server console/log.");
	}

	private void onDebugOff(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			if (!player.hasPermission("doody.debug")) {
				MessageSender.send(player, "&6[OnDoOdy] &cNeed permission node doody.debug");
				return;
			}
		}

		if (!plugin.getConfigurationManager().isDebugModeEnabled()) {
			MessageSender.send(sender, "&6[OnDoOdy] &cDebug mode was not on!");
			return;
		}

		plugin.getDebug().disable();
		MessageSender.send(sender, "&6[OnDoOdy] &aDebug mode disabled!.");
		MessageSender.send(sender, "&6[OnDoOdy] &aI hope debugging shed some light on any issues you have with OnDoOdy.");
	}

	private void onOnDuty(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			String playerName = player.getName();
			if (player.hasPermission("doody.duty")) {
				final DutyManager dutyManager = plugin.getDutyManager();
				final boolean isPlayerOnDuty = dutyManager.isPlayerOnDuty(player);
				if (isPlayerOnDuty) {
					MessageSender.send(player, "&6[OnDoOdy] &cYou're already on duty!");
					return;
				}

				String worldName = player.getWorld().getName();
				final boolean hasWorldPermission = player.hasPermission("doody.worlds." + worldName);
				final boolean isWorldInWorldList = plugin.getConfigurationManager().getWorldList().contains(worldName);
				final boolean hasWorldAccess = hasWorldPermission || plugin.getConfigurationManager().isIncludeMode() ? isWorldInWorldList : !isWorldInWorldList;

				if (hasWorldAccess) {
					plugin.getDebug().check(playerName + " used /ondoody on");
					try {
						if (dutyManager.enableDutyFor(player)) {
							MessageSender.send(player, "&6[OnDoOdy] &aYou're now on duty.");
						} else {
							MessageSender.send(player, "&6[OnDoOdy] &cYou were prevented from going on duty!");
						}
					} catch (DutyException e) {
						MessageSender.send(player, "&6[OnDoOdy] &cFailed storing your data. Could not place you on duty.");
					}
				} else {
					MessageSender.send(player, "&6[OnDoOdy] &cCannot go on duty in world &e" + worldName + " &c!");
				}
			} else {
				MessageSender.send(player, "&6[OnDoOdy] &cNeed permission node doody.duty");
			}
		} else {
			MessageSender.send(sender, "&6[OnDoOdy] &cOnly players can go on duty!");
		}
	}

	private void onOnDuty(CommandSender sender, Player targetPlayer) {
		if (targetPlayer == null) {
			MessageSender.send(sender, "&6[OnDoOdy] &cThere is no player online with that user name!");
			return;
		}

		String targetPlayerName = targetPlayer.getName();
		boolean isAlreadyOnDuty = plugin.getDutyManager().isPlayerOnDuty(targetPlayer);

		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission("doody.others")) {
				MessageSender.send(player, "&6[OnDoOdy] &cNeed permission node doody.others");
				return;
			}
		}

		if (isAlreadyOnDuty) {
			MessageSender.send(sender, "&6[OnDoOdy] &e" + targetPlayerName + " &cis already on Duty!");
			return;
		}

		try {
			if (plugin.getDutyManager().enableDutyFor(targetPlayer)) {
				MessageSender.send(sender, "&6[OnDoOdy] &e" + targetPlayerName + " &cis now on duty!");
				MessageSender.send(targetPlayer, "&6[OnDoOdy] &aYou're now on duty.");
			} else {
				MessageSender.send(sender, "&6[OnDoOdy] &e" + targetPlayerName + " &cwas prevented from going on duty!");
			}
		} catch (DutyException e) {
			MessageSender.send(sender, "&6[OnDoOdy] &cFailed storing the data of &e" + targetPlayerName + " &c. Could not place them on duty.");
		}
	}

	private void onOffDuty(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			String playerName = player.getName();
			final DutyManager dutyManager = plugin.getDutyManager();
			if (dutyManager.isPlayerOnDuty(player)) {
				plugin.getDebug().check(playerName + " used /ondoody off");
				try {
					if (dutyManager.disableDutyFor(player)) {
						MessageSender.send(player, "&6[OnDoOdy] &aYou're no longer on duty.");
					} else {
						MessageSender.send(player, "&6[OnDoOdy] &cYou were prevented from going off duty!");
					}
				} catch (DutyException e) {
					MessageSender.send(player, "&6[OnDoOdy] &cFailed restoring you to pre-duty state. Plugin encountered error.");
					MessageSender.send(player, "&6[OnDoOdy] &cPlease try again.");
				}
				return;
			} else {
				MessageSender.send(player, "&6[OnDoOdy] &cYou're not on duty!");
			}
		}
	}

	private void onOffDuty(CommandSender sender, Player targetPlayer) {
		if (targetPlayer == null) {
			MessageSender.send(sender, "&6[OnDoOdy] &cThere is no player online with that user name!");
			return;
		}

		String targetPlayerName = targetPlayer.getName();
		boolean isOnDuty = plugin.getDutyManager().isPlayerOnDuty(targetPlayer);

		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission("doody.others")) {
				MessageSender.send(player, "&6[OnDoOdy] &cNeed permission node doody.others");
				return;
			}
		}

		if (!isOnDuty) {
			MessageSender.send(sender, "&6[OnDoOdy] &e" + targetPlayerName + " &cis not on duty!");
			return;
		}

		try {
			if (plugin.getDutyManager().disableDutyFor(targetPlayer)) {
				MessageSender.send(sender, "&6[OnDoOdy] &e" + targetPlayerName + " &cis now off duty!");
				MessageSender.send(targetPlayer, "&6[OnDoOdy] &aYou're no longer on duty.");
			} else {
				MessageSender.send(sender, "&6[OnDoOdy] &e" + targetPlayerName + " &cwas prevented from going off duty!");
			}
		} catch (DutyException e) {
			MessageSender.send(sender, "&6[OnDoOdy] &cFailed restoring &e" + targetPlayerName + " &c to pre-duty state. Plugin encountered error.");
		}
	}

	private void onList(CommandSender sender) {
		Set<String> dutyList = plugin.getDutyManager().getDutySet();
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			MessageSender.send(player, "&a____________[ &6Players on duty &a]____________");
			if (!dutyList.isEmpty()) {
				MessageSender.send(player, "&6" + dutyList);
			} else {
				MessageSender.send(player, "&6No players are on duty.");
			}
		} else {
			final Log log = plugin.getLog();
			log.info("____________[ Players on Duty ]____________");
			if (!dutyList.isEmpty()) {
				log.info("" + dutyList);
			} else {
				log.info("No players are on duty.");
			}
		}
	}

	private void onBack(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			String playerName = player.getName();
			final Debug debug = plugin.getDebug();
			final DutyManager dutyManager = plugin.getDutyManager();
			if (dutyManager.isPlayerOnDuty(player)) {
				if (dutyManager.hasDutyLocation(player)) {
					dutyManager.sendToDutyLocation(player);
					MessageSender.send(player, "&6[OnDoOdy] &aBack to last known duty location.");
					debug.check(playerName + " &ateleported back to last known duty location");
				} else {
					MessageSender.send(player, "&6[OnDoOdy] &eYou have no last known duty location.");
					debug.check("<on /dm back> Last known duty location unknown.");
				}
			} else {
				MessageSender.send(player, "&6[OnDoOdy] &eYou are not on duty.");
				debug.check("</dm back> " + playerName + " is not on duty.");
			}
		}
	}

	private void onReload(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			player.getName();
			if (player.hasPermission("doody.reload")) {
				plugin.getConfigurationManager().reload();
				MessageSender.send(player, "&6[OnDoOdy] &aConfig.yml re-loaded.");
			} else {
				MessageSender.send(player, "&6[OnDoOdy] &cNeed permission node doody.reload");
			}
		} else {
			plugin.reloadConfig();
			plugin.getLog().info("Config.yml re-loaded.");
		}
	}
}
