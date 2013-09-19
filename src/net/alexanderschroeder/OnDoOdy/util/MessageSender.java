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

package net.alexanderschroeder.OnDoOdy.util;

import java.util.Arrays;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class MessageSender {

	private MessageSender() {
	}

	public static void send(final CommandSender sender, final String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	private static final ChatColor MESSAGE_PREFIX_COLOR = ChatColor.GOLD;
	private static final String MESSAGE_PREFIX = "[" + OnDoOdy.PLUGIN_NAME + "] ";

	public static void sendWithPrefix(final CommandSender sender, final String message) {
		final String messageWithPrefix = MESSAGE_PREFIX_COLOR + MESSAGE_PREFIX + message;
		send(sender, messageWithPrefix);
	}

	private static final int HEADER_WIDTH = 42;
	private static final ChatColor HEADER_COLOR = ChatColor.GREEN;
	private static final ChatColor TITLE_COLOR = ChatColor.GOLD;
	private static final String TITLE_PREFIX = "[ ";
	private static final String TITLE_SUFFIX = " ]";
	private static final char HEADER_CHARACTER = '_';

	public static void sendTitle(final CommandSender sender, final String title) {
		final int headerLength = HEADER_WIDTH - title.length() - TITLE_PREFIX.length() - TITLE_SUFFIX.length();

		final char[] headerChars = new char[headerLength / 2];
		Arrays.fill(headerChars, HEADER_CHARACTER);
		final String headerString = new String(headerChars);

		final String header = HEADER_COLOR + headerString + TITLE_PREFIX + TITLE_COLOR + title + HEADER_COLOR + TITLE_SUFFIX + headerString;
		send(sender, header);
	}

	public static String getNiceNameOf(final Material material) {
		return material.toString().toLowerCase().replace('_', ' ');
	}
}
