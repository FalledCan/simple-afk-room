package com.github.falledcan.simple_afk_room;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Simple_AFK_Room extends JavaPlugin {

    private static Simple_AFK_Room plugin;
    private AfkManager afkManager;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        migrateConfig();

        afkManager = new AfkManager(this);
        afkManager.start();

        AFKCommand command = new AFKCommand(this);
        PluginCommand pluginCommand = getCommand("afkroom");
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
        Bukkit.getPluginManager().registerEvents(new AfkListener(afkManager), this);
    }

    @Override
    public void onDisable() {
        if (afkManager != null) {
            // Don't leave anyone stranded in the AFK room across a restart/reload.
            afkManager.returnAll();
        }
    }

    /** Converts config files written by 1.0 ("afktime") and adds any keys missing from older files. */
    private void migrateConfig() {
        FileConfiguration config = getConfig();
        boolean changed = false;
        if (config.contains("afktime", true)) {
            if (!config.contains("afk-time", true)) {
                config.set("afk-time", config.getInt("afktime"));
            }
            config.set("afktime", null);
            changed = true;
        }
        for (String key : config.getDefaults().getKeys(false)) {
            if (!config.contains(key, true)) {
                config.set(key, config.getDefaults().get(key));
                changed = true;
            }
        }
        if (changed) {
            saveConfig();
        }
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    public static Simple_AFK_Room getPlugin() {
        return plugin;
    }
}
