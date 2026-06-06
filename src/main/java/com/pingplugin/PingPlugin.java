package com.pingplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * 主插件類別，負責載入設定、排程 cache 與註冊元件。
 */
public final class PingPlugin extends JavaPlugin {
    private static PingPlugin instance;

    private final Set<String> ignored = ConcurrentHashMap.newKeySet();
    private boolean excludeBedrock = true;
    private int updateTick = 20;

    private volatile double averagePing = 0.0;
    private BukkitTask cacheTask;

    public static PingPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadConfigValues();

        // register command
        this.getCommand("ping").setExecutor(new com.pingplugin.commands.PingCommand(this));
        this.getCommand("ping").setTabCompleter(new com.pingplugin.commands.PingCommand(this));

        // listeners
        getServer().getPluginManager().registerEvents(new com.pingplugin.listeners.PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new com.pingplugin.gui.GuiManager(this), this);

        // PlaceholderAPI support removed from compile-time to avoid CI resolution issues.
        // If PlaceholderAPI is present at runtime, the expansion can be added separately.

        startCacheTask();
    }

    @Override
    public void onDisable() {
        if (cacheTask != null) cacheTask.cancel();
    }

    public void loadConfigValues() {
        if (getConfig() == null) return;
        // ensure keys exist with defaults
        if (!getConfig().isSet("exclude-bedrock")) getConfig().set("exclude-bedrock", true);
        if (!getConfig().isSet("update-tick")) getConfig().set("update-tick", 20);
        if (!getConfig().isSet("noping")) getConfig().set("noping.id", Arrays.asList("iron_127", "ExamplePlayer"));
        saveConfig();

        excludeBedrock = getConfig().getBoolean("exclude-bedrock", true);
        updateTick = getConfig().getInt("update-tick", 20);

        List<String> list = getConfig().getStringList("noping.id");
        ignored.clear();
        if (list != null) {
            for (String s : list) if (s != null && !s.isBlank()) ignored.add(s);
        }
    }

    public void reloadAll() {
        reloadConfig();
        loadConfigValues();
        restartCacheTask();
    }

    private void startCacheTask() {
        if (cacheTask != null) cacheTask.cancel();
        cacheTask = Bukkit.getScheduler().runTaskTimer(this, this::updateCacheSync, 0L, Math.max(1, updateTick));
    }

    private void restartCacheTask() {
        startCacheTask();
    }

    private void updateCacheSync() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        long total = 0;
        int count = 0;
        for (Player p : players) {
            if (p == null) continue;
            if (isIgnored(p)) continue;
            if (excludeBedrock && PingAPI.isBedrock(p)) continue;
            try {
                int ping = PingAPI.getPing(p);
                total += ping;
                count++;
                // update tab name color
                String colored = com.pingplugin.listeners.PlayerListener.colorForPing(ping) + p.getName() + " (" + ping + ")";
                p.setPlayerListName(colored);
            } catch (Exception ignoredEx) {
                // avoid NPEs or reflection errors
            }
        }
        averagePing = count == 0 ? 0.0 : ((double) total) / count;
    }

    public double getAveragePing() {
        return averagePing;
    }

    public boolean isIgnored(Player p) {
        if (p == null) return true;
        return ignored.contains(p.getName()) || ignored.contains(p.getUniqueId().toString());
    }

    public boolean isExcludeBedrock() {
        return excludeBedrock;
    }

    public void addIgnored(String name) {
        if (name == null) return;
        ignored.add(name);
        saveIgnoredToConfig();
    }

    public void removeIgnored(String name) {
        if (name == null) return;
        ignored.remove(name);
        saveIgnoredToConfig();
    }

    private void saveIgnoredToConfig() {
        getConfig().set("noping.id", new ArrayList<>(ignored));
        saveConfig();
    }
}
