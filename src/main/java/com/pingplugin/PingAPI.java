package com.pingplugin;

import org.bukkit.entity.Player;

/**
 * 提供外部其他插件使用的方法。
 */
public class PingAPI {
    public static int getPing(Player player) {
        if (player == null) return 0;
        try {
            return player.getPing();
        } catch (NoSuchMethodError | Throwable t) {
            // Fallback: attempt reflection (Paper provides getPing normally)
            try {
                Object o = player.getClass().getMethod("getPing").invoke(player);
                if (o instanceof Number) return ((Number) o).intValue();
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    public static double getAveragePing() {
        PingPlugin p = PingPlugin.getInstance();
        if (p == null) return 0.0;
        return p.getAveragePing();
    }

    public static boolean isIgnored(Player player) {
        PingPlugin p = PingPlugin.getInstance();
        if (p == null) return true;
        return p.isIgnored(player);
    }

    public static boolean isBedrock(Player player) {
        // Try Floodgate API if present
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object instance = apiClass.getMethod("getInstance").invoke(null);
            if (instance != null) {
                Object res = apiClass.getMethod("isFloodgatePlayer", player.getClass()).invoke(instance, player);
                if (res instanceof Boolean) return (Boolean) res;
            }
        } catch (Throwable ignored) {
        }
        // fallback: check player.hasPermission("floodgate.player")? Not reliable.
        return false;
    }
}
