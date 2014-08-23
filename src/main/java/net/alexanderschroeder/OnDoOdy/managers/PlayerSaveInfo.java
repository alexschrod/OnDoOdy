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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

@SerializableAs(value = "OnDoOdyPlayer")
public class PlayerSaveInfo implements ConfigurationSerializable {

	public static void register() {
		ConfigurationSerialization.registerClass(PlayerSaveInfo.class);
	}

	public static void unregister() {
		ConfigurationSerialization.unregisterClass(PlayerSaveInfo.class);
	}

	public PlayerSaveInfo() {
	}

	public int level;
	public double exp;

	public InventorySaveInfo inventory = new InventorySaveInfo();

	public LocationSaveInfo location = new LocationSaveInfo();

	public double health;
	public int foodLevel;
	public double exhaustion;
	public double saturation;

	public double fallDistance;
	public int fireTicks;
	public int remainingAir;

	public Collection<PotionEffect> potionEffects;

	public Vector velocity;

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("level", level);
		map.put("exp", exp);

		map.put("inventory", inventory);

		map.put("location", location);

		map.put("health", health);
		map.put("foodLevel", foodLevel);
		map.put("exhaustion", exhaustion);
		map.put("saturation", saturation);

		map.put("fallDistance", fallDistance);
		map.put("fireTicks", fireTicks);
		map.put("remainingAir", remainingAir);

		if (potionEffects instanceof List<?>) {
			map.put("potionEffects", potionEffects);
		} else {
			map.put("potionEffects", new ArrayList<PotionEffect>(potionEffects));
		}

		map.put("velocity", velocity);
		
		return map;
	}

	public static PlayerSaveInfo deserialize(final Map<String, Object> map) {
		PlayerSaveInfo instance = new PlayerSaveInfo();
		
		instance.level = (Integer) map.get("level");
		instance.exp = (Double) map.get("exp");

		instance.inventory = (InventorySaveInfo) map.get("inventory");

		instance.location = (LocationSaveInfo) map.get("location");

		instance.health = (Double) map.get("health");
		instance.foodLevel = (Integer) map.get("foodLevel");
		instance.exhaustion = (Double) map.get("exhaustion");
		instance.saturation = (Double) map.get("saturation");

		instance.fallDistance = (Double) map.get("fallDistance");
		instance.fireTicks = (Integer) map.get("fireTicks");
		instance.remainingAir = (Integer) map.get("remainingAir");

		instance.potionEffects = (Collection<PotionEffect>) map.get("potionEffects");

		instance.velocity = (Vector) map.get("velocity");
		
		return instance;
	}

}
