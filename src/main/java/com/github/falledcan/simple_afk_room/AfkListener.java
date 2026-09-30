package com.github.falledcan.simple_afk_room;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AfkListener implements Listener {

    private final AfkManager manager;

    public AfkListener(AfkManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) {
            return;
        }
        boolean changed = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()
                || from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        if (changed) {
            manager.onActivity(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e) {
        manager.onActivity(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        manager.onActivity(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // They respawn normally; don't pull them back to the old spot afterwards.
        manager.clear(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Put AFK players back before their data is saved so they don't log in inside the AFK room.
        if (manager.isAfk(e.getPlayer())) {
            manager.returnFromRoom(e.getPlayer());
        } else {
            manager.clear(e.getPlayer());
        }
    }
}
