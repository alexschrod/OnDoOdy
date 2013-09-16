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

package net.alexanderschroeder.OnDoOdy.listeners;

import net.alexanderschroeder.OnDoOdy.OnDoOdy;
import net.alexanderschroeder.OnDoOdy.util.MessageSender;

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

public class EntityListener implements Listener {
	private final OnDoOdy plugin;

	public EntityListener(final OnDoOdy plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAttack(final EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			final Player attacker = (Player) event.getDamager();
			final Entity receiver = event.getEntity();

			if (!plugin.getDutyManager().isPlayerOnDuty(attacker)) {
				return;
			}

			final boolean allowPvP = plugin.getConfigurationManager().isPvPAllowed() || attacker.hasPermission("doody.pvp");
			final boolean allowMob = plugin.getConfigurationManager().isMobDamageAllowed() || attacker.hasPermission("doody.mob");

			final boolean isPlayer = receiver instanceof Player;

			if (!isPlayer && allowMob) {
				return;
			} else if (isPlayer && allowPvP) {
				return;
			} else if (!isPlayer) {
				MessageSender.send(attacker, "&6[OnDoOdy] &cYou may not attack mobs while on duty.");
			} else if (isPlayer) {
				MessageSender.send(attacker, "&6[OnDoOdy] &cYou may not attack players while on duty.");
			}

			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onShootEvent(final EntityShootBowEvent event) {
		final LivingEntity entity = event.getEntity();
		if (entity instanceof Player) {
			final Player player = (Player) entity;
			if (plugin.getDutyManager().isPlayerOnDuty(player)) {
				MessageSender.send(player, "&6[OnDoOdy] &cYou may not shoot bows while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSplashEvent(final PotionSplashEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			final Player shooter = (Player) event.getEntity().getShooter();
			if (plugin.getDutyManager().isPlayerOnDuty(shooter)) {
				MessageSender.send(shooter, "&6[OnDoOdy] &cYou may not throw potions while on duty.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityTarget(final EntityTargetEvent event) {
		final Entity entity = event.getTarget();
		if (entity == null) {
			return;
		}

		if (entity instanceof Player) {
			final Player target = (Player) entity;
			if (!plugin.getDutyManager().isPlayerOnDuty(target)) {
				return;
			}

			event.setCancelled(true);
		}
	}
}
