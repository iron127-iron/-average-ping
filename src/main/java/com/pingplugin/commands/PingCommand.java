package com.pingplugin.commands;

import com.pingplugin.PingAPI;
import com.pingplugin.PingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /ping 指令實作
 */
public class PingCommand implements CommandExecutor, TabCompleter {
    private final PingPlugin plugin;

    public PingCommand(PingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("請在遊戲中使用此指令。");
                return true;
            }
            Player p = (Player) sender;
            int ping = PingAPI.getPing(p);
            p.sendMessage("你的 Ping: " + ping + "ms");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help":
                sender.sendMessage("/ping - 顯示自己的 Ping");
                sender.sendMessage("/ping <player> - 顯示指定玩家 Ping");
                sender.sendMessage("/ping gui - 開啟管理 GUI");
                sender.sendMessage("/ping reload - 重新載入設定檔");
                sender.sendMessage("/ping add <name> - 加入排除名單");
                sender.sendMessage("/ping remove <name> - 移除排除名單");
                return true;
            case "reload":
                if (!sender.hasPermission("ping.reload")) {
                    sender.sendMessage("沒有權限。");
                    return true;
                }
                plugin.reloadAll();
                sender.sendMessage("已重新載入設定。");
                return true;
            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("此子指令需在遊戲中使用。");
                    return true;
                }
                if (!sender.hasPermission("ping.gui")) {
                    sender.sendMessage("沒有權限。");
                    return true;
                }
                new com.pingplugin.gui.GuiManager(plugin).open((Player) sender);
                return true;
            case "add":
                if (!sender.hasPermission("ping.admin")) {
                    sender.sendMessage("沒有權限。");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("用法: /ping add <name>");
                    return true;
                }
                plugin.addIgnored(args[1]);
                sender.sendMessage("已加入排除名單: " + args[1]);
                return true;
            case "remove":
                if (!sender.hasPermission("ping.admin")) {
                    sender.sendMessage("沒有權限。");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("用法: /ping remove <name>");
                    return true;
                }
                plugin.removeIgnored(args[1]);
                sender.sendMessage("已移除排除名單: " + args[1]);
                return true;
            default:
                // treat as player name
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage("找不到玩家: " + args[0]);
                    return true;
                }
                int ping = PingAPI.getPing(target);
                sender.sendMessage(target.getName() + " 的 Ping: " + ping + "ms");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("help");
            subs.add("reload");
            subs.add("gui");
            subs.add("add");
            subs.add("remove");
            subs.addAll(Bukkit.getOnlinePlayers().stream().map(org.bukkit.entity.Player::getName).collect(Collectors.toList()));
            return subs.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
