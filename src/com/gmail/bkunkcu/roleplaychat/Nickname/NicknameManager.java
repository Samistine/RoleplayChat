package com.gmail.bkunkcu.roleplaychat.Nickname;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Server;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.command.CommandSender;

import com.gmail.bkunkcu.roleplaychat.RoleplayChat;

import java.util.Optional;

public final class NicknameManager {

    private final RoleplayChat plugin;
    private final Server server;
    private boolean vault;
    
    private Chat chat;

    public NicknameManager(RoleplayChat plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();

        if (setupChat()) {
            vault = true;
            plugin.getLogger().info("Vault found and hooked!");
        }
    }

    public String getNickname(Player player) {
        if (plugin.getConfig().getBoolean("settings.useNickname")) {

            Optional<String> optional = plugin.DatabaseManager.getNickname(player.getName()).orElseThrow(() -> new RuntimeException("Data Exception"));
            if (optional.isPresent()) {
                return plugin.getConfig().getString("settings.nicknamePrefix") + optional.get();
            } else {
                return player.getName();
            }

        } else {
            return player.getDisplayName();
        }
    }

    public String getPrefix(Player player) {
        if (vault) {
            String prefix = chat.getPlayerPrefix(player);
            return prefix;
        } else {
            return "";
        }
    }

    public String getSuffix(Player player) {
        if (vault) {
            String suffix = chat.getPlayerSuffix(player);
            return suffix;
        } else {
            return "";
        }
    }

    public void setNickname(CommandSender sender, String username, String nickname) {
        if (nickname.matches("[a-zA-Z0-9]+") && nickname.length() <= 20) {

            plugin.DatabaseManager.setNickname(username, nickname);

            //Log to console if enabled
            if (plugin.getConfig().getBoolean("settings.logging.console")) {
                server.getConsoleSender().sendMessage("Player " + username + "'s nickname has changed to " + nickname);
            }

            //Notify user who's nickname was changed
            if (server.getPlayerExact(username) != null) {
                server.getPlayerExact(username).sendMessage("Your nickname has changed to " + ChatColor.GOLD + nickname);
            }

            //Notify user who issued command, if they aren't the person's who's nick is being changed
            if (!sender.getName().equalsIgnoreCase(username)) {
                sender.sendMessage("Player " + ChatColor.GOLD + username + ChatColor.RESET + "'s nickname has changed to " + ChatColor.GOLD + nickname);
            }

        } else {
            sender.sendMessage("Nicknames should less than 20 characters and only contains a-zA-Z0-9");
        }
    }

    public void removeNickname(CommandSender sender, String username) {
        boolean removedNickname = plugin.DatabaseManager.removeNickname(username).orElseThrow(() -> new RuntimeException("Database Error"));

        if (removedNickname == true) {

            //Log to console if enabled
            if (plugin.getConfig().getBoolean("settings.logging.console")) {
                server.getConsoleSender().sendMessage("Player " + username + " is no longer using a nickname");
            }

            //Notify user who's nickname was removed
            if (server.getPlayerExact(username) != null) {
                server.getPlayerExact(username).sendMessage("You are no longer using a nickname");
            }

            //Notify user who issued command, if they aren't the person's who's nick was removed
            if (!sender.getName().equalsIgnoreCase(username)) {
                sender.sendMessage("Player " + ChatColor.GOLD + username + ChatColor.RESET + " is no longer using a nickname");
            }

        } else if (sender.getName().equalsIgnoreCase(username)) {
            sender.sendMessage("You are not using a nickname");
        } else {
            sender.sendMessage("Player " + ChatColor.GOLD + username + ChatColor.RESET + " is not using a nickname");
        }
    }
    
    private boolean setupChat() {
        try {
            RegisteredServiceProvider<Chat> rsp = server.getServicesManager().getRegistration(Chat.class);
            chat = rsp.getProvider();
            return chat != null;
        } catch (NoClassDefFoundError ex) {
            return false;
        }
    }
}
