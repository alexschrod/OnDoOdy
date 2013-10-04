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

import java.util.List;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public final class PlayerMetadataManager {
	private final OnDoOdy plugin;

	public PlayerMetadataManager(final OnDoOdy plugin) {
		this.plugin = plugin;
	}

	public void setMetadata(final Player player, final String key, final Object value) {
		player.setMetadata(key, new FixedMetadataValue(plugin, value));
	}

	public Object getMetadata(final Player player, final String key) {
		final List<MetadataValue> values = player.getMetadata(key);
		for (final MetadataValue value : values) {
			if (value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())) {
				return value.value();
			}
		}
		return null;
	}

	public void removeMetadata(final Player player, final String key) {
		player.removeMetadata(key, plugin);
	}
}
