package com.gmail.bkunkcu.roleplaychat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.bkunkcu.roleplaychat.Commands.RoleplayChatCommandExecutor;
import com.gmail.bkunkcu.roleplaychat.Nickname.DatabaseManager;
import com.gmail.bkunkcu.roleplaychat.Nickname.NicknameManager;

import java.util.UUID;
import org.bukkit.World;

public final class RoleplayChat extends JavaPlugin implements Listener {

    private final List<UUID> playersSpying = new ArrayList<>();

    public FileManager FileManager;
    public DatabaseManager DatabaseManager;
    public NicknameManager NicknameManager;
    public MessageBuilder MessageBuilder;

    @Override
    public void onEnable() {
        load();
        super.getServer().getPluginManager().registerEvents(this, this);
        super.getCommand("roleplaychat").setExecutor(new RoleplayChatCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        DatabaseManager.close();
    }

    public void load() {
        this.MessageBuilder = new MessageBuilder(this);
        this.NicknameManager = new NicknameManager(this);
        this.DatabaseManager = new DatabaseManager(this);
        this.FileManager = new FileManager(this);
    }

    public boolean hasPermission(Player player, String permission) {
        return player == null || player.hasPermission(permission);
    }

    //Listeners
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final String message = event.getMessage();

        if (MessageBuilder.isDefault(player)) {
            event.setCancelled(true);
            MessageBuilder.sendMessage(player, "default", message);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        // begin swear filter
        String chat = event.getMessage();
        String result = chat.replaceAll("[-+.^:,!*%$£|/]", "");
        String result2 = result.replaceAll(" ", "");
        for (String sword : FileManager.getBadWords()) {
            if (result2.toLowerCase().contains(sword)) {
                event.getPlayer().kickPlayer(ChatColor.AQUA + "Nope, thats not in my dictionary");
                return;
            }
        }
        //end swear filter

        final Player player = event.getPlayer();
        final World world = player.getWorld();

        final String[] input = event.getMessage().split(" ", 2);
        final String command = input[0].replace("/", "");

        if (input.length != 1) {
            String message = input[1];

            for (String key : FileManager.getCommands(world)) {
                final YamlConfiguration yml = FileManager.getWorldConfig(player.getWorld());
                for (String s : yml.getStringList(key + ".commands")) {
                    if (s.equalsIgnoreCase(command)) {
                        event.setCancelled(true);
                        MessageBuilder.sendMessage(player, key, message);
                    }
                }
            }
        }
    }

    public List<UUID> getPlayersSpying() {
        return playersSpying;
    }

}
