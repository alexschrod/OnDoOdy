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

package com.angelofdev.DoOdy.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.angelofdev.DoOdy.DoOdy;
import com.angelofdev.DoOdy.util.MessageSender;

public class EntityListener implements Listener {
	private DoOdy plugin;

	public EntityListener(DoOdy plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			Entity receiver = event.getEntity();

			if (!plugin.getDutyManager().isPlayerOnDuty(attacker))
				return;

			boolean allowPvP = plugin.getConfigurationManager().isPvPAllowed() || attacker.hasPermission("doody.pvp");
			boolean allowMob = plugin.getConfigurationManager().isMobDamageAllowed() || attacker.hasPermission("doody.mob");

			boolean isPlayer = receiver instanceof Player;

			if (!isPlayer && allowMob)
				return;
			else if (isPlayer && allowPvP)
				return;
			else if (!isPlayer)
				MessageSender.send(attacker, "&6[OnDoOdy] &cYou may not attack mobs while on duty.");
			else if (isPlayer)
				MessageSender.send(attacker, "&6[OnDoOdy] &cYou may not attack players while on duty.");

			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onShootEvent(EntityShootBowEvent event) {
		final LivingEntity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (plugin.getDutyManager().isPlayerOnDuty(player)) {
				MessageSender.send(player, "&6[OnDoOdy] &cYou may not shoot bows while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSplashEvent(PotionSplashEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player shooter = (Player) event.getEntity().getShooter();
			if (plugin.getDutyManager().isPlayerOnDuty(shooter)) {
				MessageSender.send(shooter, "&6[OnDoOdy] &cYou may not throw potions while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		final Entity entity = event.getTarget();
		if (entity == null)
			return;

		if (entity instanceof Player) {
			final Player target = (Player) entity;
			if (!plugin.getDutyManager().isPlayerOnDuty(target))
				return;
			
			event.setCancelled(true);
		}
	}
}
