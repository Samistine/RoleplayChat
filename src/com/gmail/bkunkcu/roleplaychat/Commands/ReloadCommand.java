package com.gmail.bkunkcu.roleplaychat.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.bkunkcu.roleplaychat.RoleplayChat;

final class ReloadCommand implements RoleplayChatCommand {

    private final RoleplayChat plugin;

    public ReloadCommand(RoleplayChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {

        plugin.load();

        plugin.getLogger().info("RoleplayChat reloaded!");
        if (player != null) {
            player.sendMessage(ChatColor.DARK_GRAY + "RoleplayChat reloaded!");
        }

        return true;
    }

    @Override
    public String getHelp(Player player) {
        if (plugin.hasPermission(player, getPermission())) {
            return "/rc reload - Reloads the plugin.";
        }

        return null;
    }

    @Override
    public String getPermission() {
        return "roleplaychat.reload";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

}
