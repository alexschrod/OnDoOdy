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

package com.angelofdev.DoOdy.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventorySaveInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<Map<String, Object>> inventory;
	public List<Map<String, Object>> armor;

	public InventorySaveInfo() {
	}

	@SuppressWarnings("deprecation")
	public InventorySaveInfo(PlayerInventory inventory) {
		// save inventory
		final ConfigurationSerializable[] inventoryContents = (ConfigurationSerializable[]) inventory.getContents();
		for (int i = 0; i < inventoryContents.length; i++) {
			if (inventoryContents[i] == null) {
				inventoryContents[i] = new ItemStack(Material.AIR);
			}
		}
		List<ConfigurationSerializable> inventoryList = Arrays.asList(inventoryContents);
		this.inventory = SerializationUtil.serializeItemList(inventoryList);

		// save armor
		final ConfigurationSerializable[] armorContents = (ConfigurationSerializable[]) inventory.getArmorContents();
		for (int i = 0; i < armorContents.length; i++) {
			if (armorContents[i] == null) {
				armorContents[i] = new ItemStack(Material.AIR);
			}
		}
		List<ConfigurationSerializable> armorList = Arrays.asList(armorContents);
		this.armor = SerializationUtil.serializeItemList(armorList);
	}

	@SuppressWarnings("deprecation")
	public void restore(PlayerInventory inventory) {
		List<ConfigurationSerializable> itemContents = SerializationUtil.deserializeItemList(this.inventory);
		ItemStack[] items = new ItemStack[itemContents.size()];
		for (int i = 0; i < itemContents.size(); i++) {
			items[i] = (ItemStack) itemContents.get(i);
		}
		inventory.setContents(items);
		List<ConfigurationSerializable> armorContents = SerializationUtil.deserializeItemList(this.armor);
		items = new ItemStack[armorContents.size()];
		for (int i = 0; i < armorContents.size(); i++) {
			items[i] = (ItemStack) armorContents.get(i);
		}
		inventory.setArmorContents(items);
	}
}