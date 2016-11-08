package com.gmail.bkunkcu.roleplaychat.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

interface RoleplayChatCommand {

    public boolean onCommand(CommandSender sender, Player player, String[] args);

    public String getHelp(Player player);

    public String getPermission();

    public boolean isPlayerOnly();
}
