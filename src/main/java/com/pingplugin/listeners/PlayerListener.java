package com.pingplugin.listeners;

import com.pingplugin.PingAPI;
import com.pingplugin.PingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 處理玩家加入時的簡單行為（例如初始化顯示名稱）。
 */
public class PlayerListener implements Listener {
    private final PingPlugin plugin;

    public PlayerListener(PingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try {
            int ping = PingAPI.getPing(e.getPlayer());
            e.getPlayer().setPlayerListName(colorForPing(ping) + e.getPlayer().getName() + " (" + ping + ")");
        } catch (Exception ignored) {
        }
    }

    public static String colorForPing(int ping) {
        if (ping <= 50) return ChatColor.GREEN.toString();
        if (ping <= 120) return ChatColor.YELLOW.toString();
        return ChatColor.RED.toString();
    }
}
