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

package net.alexanderschroeder.OnDoOdy.util;

import java.io.Serializable;
import java.util.Collection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class PlayerSaveInfo implements Serializable {
	private static final long serialVersionUID = 2L;

	public int level;
	public float exp;

	public InventorySaveInfo inventory = new InventorySaveInfo();

	public LocationSaveInfo location = new LocationSaveInfo();

	public double health;
	public int foodLevel;
	public float exhaustion;
	public float saturation;
	
	public float fallDistance;
	public int fireTicks;
	public int remainingAir;

	public Collection<PotionEffect> potionEffects;

	public Vector velocity;

}
