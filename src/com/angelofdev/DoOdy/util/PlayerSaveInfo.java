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
import java.util.Collection;
import org.bukkit.potion.PotionEffect;

public class PlayerSaveInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public int level;
	public float exp;

	public InventorySaveInfo inventory = new InventorySaveInfo();

	public LocationSaveInfo location = new LocationSaveInfo();

	public double health;
	public int foodLevel;

	public Collection<PotionEffect> potionEffects;
}