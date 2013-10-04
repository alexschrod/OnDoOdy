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

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSaveInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public String world;
	public double x;
	public double y;
	public double z;
	public float pitch;
	public float yaw;

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
		final Location location = new Location(world, x, y, z, yaw, pitch);
		return location;
	}
}