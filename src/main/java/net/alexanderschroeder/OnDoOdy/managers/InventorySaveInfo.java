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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@SerializableAs(value = "OnDoOdyInventory")
public class InventorySaveInfo implements ConfigurationSerializable {

	public static void register() {
		ConfigurationSerialization.registerClass(InventorySaveInfo.class);
	}

	public static void unregister() {
		ConfigurationSerialization.unregisterClass(InventorySaveInfo.class);
	}

	public List<ItemStack> inventory;
	public List<ItemStack> armor;

	public InventorySaveInfo() {
	}

	public InventorySaveInfo(final PlayerInventory inventory) {
		// save inventory
		this.inventory = Arrays.asList(inventory.getContents());

		// save armor
		armor = Arrays.asList(inventory.getArmorContents());
	}

	public void restore(final PlayerInventory inventory) {
		inventory.setContents(this.inventory.toArray(new ItemStack[this.inventory.size()]));

		inventory.setArmorContents(armor.toArray(new ItemStack[armor.size()]));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("inventory", inventory);
		map.put("armor", armor);

		return map;
	}

	public static InventorySaveInfo deserialize(final Map<String, Object> map) {
		InventorySaveInfo instance = new InventorySaveInfo();

		instance.inventory = (List<ItemStack>) map.get("inventory");
		instance.armor = (List<ItemStack>) map.get("armor");

		return instance;
	}
}