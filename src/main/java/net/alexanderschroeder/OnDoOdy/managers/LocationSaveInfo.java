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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs(value = "OnDoOdyLocation")
public class LocationSaveInfo implements ConfigurationSerializable {

	public static void register() {
		ConfigurationSerialization.registerClass(LocationSaveInfo.class);
	}

	public static void unregister() {
		ConfigurationSerialization.unregisterClass(LocationSaveInfo.class);
	}

	public String world;
	public double x;
	public double y;
	public double z;
	public double pitch;
	public double yaw;

	public LocationSaveInfo() {
	}

	public LocationSaveInfo(final Location location) {
		world = location.getWorld().getName();

		x = location.getX();
		y = location.getY();
		z = location.getZ();

		pitch = location.getPitch();
		yaw = location.getYaw();
	}

	public Location getLocation() {
		final World world = Bukkit.getServer().getWorld(this.world);
		final Location location = new Location(world, x, y, z, (float)yaw, (float)pitch);
		return location;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("world", world);

		map.put("x", x);
		map.put("y", y);
		map.put("z", z);

		map.put("pitch", pitch);
		map.put("yaw", yaw);

		return map;
	}

	public static LocationSaveInfo deserialize(final Map<String, Object> map) {
		LocationSaveInfo instance = new LocationSaveInfo();

		instance.world = (String) map.get("world");

		instance.x = (Double) map.get("x");
		instance.y = (Double) map.get("y");
		instance.z = (Double) map.get("z");

		instance.pitch = (Double) map.get("pitch");
		instance.yaw = (Double) map.get("yaw");

		return instance;
	}
}