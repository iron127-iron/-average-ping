package com.pingplugin.gui;

import com.pingplugin.PingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 簡單的 GUI 管理，用來加入/移除排除名單。
 */
public class GuiManager implements Listener {
    private final PingPlugin plugin;

    public GuiManager(PingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, "Ping Ignore Manager");
        int i = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack skull = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            ItemMeta m = skull.getItemMeta();
            if (m != null) {
                m.setDisplayName(online.getName());
                List<String> lore = new ArrayList<>();
                lore.add("Ping: " + PingPlugin.getInstance().getAveragePing());
                lore.add(plugin.isIgnored(online) ? ChatColor.RED + "Ignored" : ChatColor.GREEN + "Tracked");
                m.setLore(lore);
                skull.setItemMeta(m);
            }
            inv.setItem(i++, skull);
            if (i >= 9) break;
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Ping Ignore Manager")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String name = e.getCurrentItem().getItemMeta() == null ? null : e.getCurrentItem().getItemMeta().getDisplayName();
            if (name == null) return;
            if (plugin.isIgnored(Bukkit.getPlayerExact(name))) {
                plugin.removeIgnored(name);
                e.getWhoClicked().sendMessage("已從排除名單移除: " + name);
            } else {
                plugin.addIgnored(name);
                e.getWhoClicked().sendMessage("已加入排除名單: " + name);
            }
            plugin.saveConfig();
            ((Player) e.getWhoClicked()).closeInventory();
        }
    }
}
