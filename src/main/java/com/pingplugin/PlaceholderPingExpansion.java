package com.pingplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI 擴充，用於提供 %ping_iron% 顯示全服平均 Ping
 */
public class PlaceholderPingExpansion extends PlaceholderExpansion {
    private final PingPlugin plugin;

    public PlaceholderPingExpansion(PingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "pingplugin";
    }

    @Override
    public String getAuthor() {
        return "PingPlugin";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (identifier.equalsIgnoreCase("ping_iron")) {
            return String.valueOf((int) plugin.getAveragePing());
        }
        return null;
    }
}
