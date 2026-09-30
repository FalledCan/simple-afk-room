package com.github.falledcan.simple_afk_room;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation") // ChatColor keeps the plugin compatible with Spigot as well as Paper
public class AFKCommand implements TabExecutor {

    private static final String PREFIX = ChatColor.GREEN + "[afkroom] ";
    private static final List<String> SUBCOMMANDS = List.of("set", "tp", "time", "reload");

    private final Simple_AFK_Room plugin;

    public AFKCommand(Simple_AFK_Room plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("afkroom.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "For admin only");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        AfkManager manager = plugin.getAfkManager();
        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "This command can only be run by a player.");
                    return true;
                }
                Location loc = p.getLocation();
                manager.setRoomLocation(loc);
                sender.sendMessage(PREFIX + "Set AFK room to " + loc.getWorld().getName() + " "
                        + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            }
            case "tp" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "This command can only be run by a player.");
                    return true;
                }
                Location room = manager.getRoomLocation();
                if (room == null) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "The AFK room is not set. Use /" + label + " set");
                    return true;
                }
                p.teleport(room);
            }
            case "time" -> {
                if (args.length < 2) {
                    sender.sendMessage(PREFIX + "AFK time: " + manager.getAfkTime() + " sec. (/" + label + " time <sec>)");
                    return true;
                }
                int time;
                try {
                    time = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    time = -1;
                }
                if (time < 1) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Time must be a positive whole number of seconds.");
                    return true;
                }
                plugin.getConfig().set("afk-time", time);
                plugin.saveConfig();
                sender.sendMessage(PREFIX + "Set AFK time to " + time + " sec.");
            }
            case "reload" -> {
                plugin.reloadConfig();
                manager.onReload();
                sender.sendMessage(PREFIX + "Config reloaded.");
            }
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(PREFIX + "/" + label + " set - set the AFK room to your position");
        sender.sendMessage(PREFIX + "/" + label + " tp - teleport to the AFK room");
        sender.sendMessage(PREFIX + "/" + label + " time <sec> - set the idle time");
        sender.sendMessage(PREFIX + "/" + label + " reload - reload config.yml");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission("afkroom.admin")) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String sub : SUBCOMMANDS) {
            if (sub.startsWith(args[0].toLowerCase())) {
                result.add(sub);
            }
        }
        return result;
    }
}
