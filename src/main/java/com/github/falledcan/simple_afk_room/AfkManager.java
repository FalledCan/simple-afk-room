package com.github.falledcan.simple_afk_room;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks idle time per player and moves players in and out of the AFK room. */
public class AfkManager {

    /** Movement right after being sent to the room is ignored (teleport settle, falling onto the floor). */
    private static final long GRACE_MILLIS = 1500L;

    private final Simple_AFK_Room plugin;
    private final Map<UUID, Integer> idleSeconds = new HashMap<>();
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final Map<UUID, Long> sentAt = new HashMap<>();
    private BukkitTask task;
    private boolean warnedMissingWorld;

    public AfkManager(Simple_AFK_Room plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        int afkTime = getAfkTime();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isAfk(p)) {
                sendTitle(p);
                continue;
            }
            if (p.isDead() || p.hasPermission("afkroom.bypass")) {
                idleSeconds.remove(p.getUniqueId());
                continue;
            }
            int idle = idleSeconds.merge(p.getUniqueId(), 1, Integer::sum);
            if (idle >= afkTime) {
                sendToRoom(p);
            }
        }
    }

    public boolean isAfk(Player p) {
        return returnLocations.containsKey(p.getUniqueId());
    }

    /** Called on any player activity: resets the idle timer and brings AFK players back. */
    public void onActivity(Player p) {
        idleSeconds.remove(p.getUniqueId());
        if (!isAfk(p)) {
            return;
        }
        Long sent = sentAt.get(p.getUniqueId());
        if (sent != null && System.currentTimeMillis() - sent < GRACE_MILLIS) {
            return;
        }
        returnFromRoom(p);
    }

    private void sendToRoom(Player p) {
        Location room = getRoomLocation();
        if (room == null) {
            // No room configured; start counting from zero again instead of retrying every second.
            idleSeconds.remove(p.getUniqueId());
            return;
        }
        Location back = p.getLocation();
        // Teleports fail while riding or carrying entities.
        p.leaveVehicle();
        p.eject();
        if (!p.teleport(room)) {
            idleSeconds.remove(p.getUniqueId());
            return;
        }
        returnLocations.put(p.getUniqueId(), back);
        sentAt.put(p.getUniqueId(), System.currentTimeMillis());
        idleSeconds.remove(p.getUniqueId());
        p.setCollidable(false);
    }

    /** Teleports an AFK player back to where they were and clears their AFK state. */
    public void returnFromRoom(Player p) {
        Location back = returnLocations.get(p.getUniqueId());
        clear(p);
        if (back != null && back.getWorld() != null) {
            p.teleport(back);
        }
    }

    /** Forgets all state for the player without teleporting them. */
    public void clear(Player p) {
        UUID id = p.getUniqueId();
        idleSeconds.remove(id);
        sentAt.remove(id);
        if (returnLocations.remove(id) != null) {
            p.setCollidable(true);
        }
    }

    public void returnAll() {
        if (task != null) {
            task.cancel();
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isAfk(p)) {
                returnFromRoom(p);
            }
        }
    }

    public int getAfkTime() {
        return Math.max(1, plugin.getConfig().getInt("afk-time", 300));
    }

    /** @return the configured room, or null if it is unset or its world isn't loaded */
    public Location getRoomLocation() {
        ConfigurationSection room = plugin.getConfig().getConfigurationSection("afk-room");
        if (room == null || !room.contains("x") || !room.contains("y") || !room.contains("z")) {
            return null;
        }
        String worldName = room.getString("world");
        World world;
        if (worldName == null) {
            // 1.0 configs had no world; treat them as the main world.
            world = Bukkit.getWorlds().get(0);
        } else {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                if (!warnedMissingWorld) {
                    plugin.getLogger().warning("AFK room world '" + worldName + "' is not loaded.");
                    warnedMissingWorld = true;
                }
                return null;
            }
        }
        return new Location(world, room.getDouble("x"), room.getDouble("y"), room.getDouble("z"),
                (float) room.getDouble("yaw"), (float) room.getDouble("pitch"));
    }

    public void setRoomLocation(Location loc) {
        ConfigurationSection room = plugin.getConfig().createSection("afk-room");
        room.set("world", loc.getWorld().getName());
        room.set("x", loc.getBlockX() + 0.5);
        room.set("y", (double) loc.getBlockY());
        room.set("z", loc.getBlockZ() + 0.5);
        room.set("yaw", (double) loc.getYaw());
        room.set("pitch", (double) loc.getPitch());
        plugin.saveConfig();
        warnedMissingWorld = false;
    }

    public void onReload() {
        warnedMissingWorld = false;
    }

    @SuppressWarnings("deprecation") // sendTitle(String, ...) works on both Spigot and Paper
    private void sendTitle(Player p) {
        // The one-argument getString falls back to the bundled config.yml; getString(path, def) would not.
        String title = color(plugin.getConfig().getString("title"));
        String subtitle = color(plugin.getConfig().getString("subtitle"));
        p.sendTitle(title, subtitle, 0, 30, 5);
    }

    @SuppressWarnings("deprecation")
    private static String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }
}
