package com.gmail.bkunkcu.roleplaychat;

import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class MessageBuilder {

    private RoleplayChat plugin;
    private Server server;
    private YamlConfiguration yml = new YamlConfiguration();

    public MessageBuilder(RoleplayChat plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    public boolean isDefault(Player player) {
        return plugin.FileManager.getCommands(player.getWorld()).contains("default");
    }

    public void sendMessage(Player player, String key, String message) {
        try {
            yml = plugin.FileManager.getWorldConfig(player.getWorld());
        } catch (Exception e) {
            plugin.getLogger().info("Couldn't load chat.yml files. Disabling plugin!");
            server.getPluginManager().disablePlugin(plugin);
        }

        if (!yml.getBoolean(key + ".permission") || player.hasPermission("roleplaychat." + key)) {
            String displayMessage = yml.getString(key + ".format").replace("&", "§").replace("%username%", plugin.NicknameManager.getNickname(player)).replace("%message%", message).replace("%prefix%", plugin.NicknameManager.getPrefix(player)).replace("%suffix%", plugin.NicknameManager.getSuffix(player));

            if (plugin.getConfig().getBoolean("settings.logging.console")) {
                server.getConsoleSender().sendMessage(displayMessage.replace("§", "&"));
            }

            for (Player receiver : server.getOnlinePlayers()) {
                int radius = yml.getInt(key + ".radius");

                if (radius == -1) {
                    receiver.sendMessage(displayMessage);
                } else if (radius == 0) {

                    if (receiver.getWorld() == player.getWorld()) {
                        receiver.sendMessage(displayMessage);
                    } else if (receiver.getWorld() != player.getWorld() && plugin.getPlayersSpying().contains(receiver.getUniqueId())) {
                        receiver.sendMessage("§8[SPY] §r" + displayMessage);
                    }

                } else if (receiver.getWorld() == player.getWorld()) {

                    double distance = player.getLocation().distance(receiver.getLocation());

                    if (distance <= radius) {
                        receiver.sendMessage(displayMessage);
                    } else if (distance > radius && plugin.getPlayersSpying().contains(receiver.getUniqueId())) {
                        receiver.sendMessage("§8[SPY] §r" + displayMessage);
                    }

                } else if (receiver.getWorld() != player.getWorld() && plugin.getPlayersSpying().contains(receiver.getUniqueId())) {
                    receiver.sendMessage("§8[SPY] §r" + displayMessage);
                }
            }
        } else {
            player.sendMessage("§4You don't have permissions to use this command");
        }
    }

}
