package com.gmail.bkunkcu.roleplaychat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;

public class FileManager {

    private final RoleplayChat plugin;
    public final List<String> spy = new ArrayList<>();
    public final Multimap<String, String> modes = ArrayListMultimap.create();
    public final HashMap<String, String> mirrors = new HashMap<>();

    public FileManager(RoleplayChat plugin) {
        this.plugin = plugin;
    }

    public void getFiles() {
        modes.clear();
        mirrors.clear();

        File config = new File(plugin.getDataFolder(), "config.yml");
        File config2 = new File(plugin.getDataFolder(), "filter.yml");

        if (!config.exists()) {
            plugin.getDataFolder().mkdir();
            copy(plugin.getResource("config.yml"), config);
        }

        if (!config2.exists()) {
            copy(plugin.getResource("filter.yml"), config2);
        } else {
            plugin.reloadConfig();
        }

        getMirrors();

        for (World world : plugin.getServer().getWorlds()) {

            if (!mirrors.containsKey(world.getName())) {
                getModes(world.getName());
            }
        }
    }

    private void getMirrors() {
        for (World world : plugin.getServer().getWorlds()) {

            if (plugin.getConfig().get("settings.mirrors." + world.getName()) != null) {

                for (String mirrored : plugin.getConfig().getStringList("settings.mirrors." + world.getName())) {
                    mirrors.put(mirrored, world.getName());
                }
            }
        }
    }

    private void getModes(String world) {
        File folder = new File(plugin.getDataFolder(), world);
        File file = new File(folder, "chat.yml");

        if (!file.exists()) {
            folder.mkdirs();
            copy(plugin.getResource("chat.yml"), file);
        }

        YamlConfiguration yml = new YamlConfiguration();
        try {
            yml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().info("Couldn't load chat.yml file. Disabling plugin!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        for (String key : yml.getKeys(false)) {
            modes.put(world, key);
        }
    }

    private void copy(InputStream in, File file) {
        try {
            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
