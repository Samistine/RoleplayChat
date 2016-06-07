package com.gmail.bkunkcu.roleplaychat;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public final class FileManager {

    private final RoleplayChat plugin;
    private final LoadingCache<File, YamlConfiguration> yamlCache;
    private final List<String> badWords;
    private final HashMap<String, String> mirrors = new HashMap<>();

    public FileManager(RoleplayChat plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();

        //Setup YamlCaching
        this.yamlCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .build(new CacheLoader<File, YamlConfiguration>() {
                    @Override
                    public YamlConfiguration load(File key) {
                        return YamlConfiguration.loadConfiguration(key);
                    }
                }
                );

        //Load BadWords
        this.badWords = plugin.getConfig().getStringList("settings.badWords");

        //Load Mirrors
        for (World world : plugin.getServer().getWorlds()) {
            if (plugin.getConfig().get("settings.mirrors." + world.getName()) != null) {
                for (String mirrored : plugin.getConfig().getStringList("settings.mirrors." + world.getName())) {
                    mirrors.put(mirrored, world.getName());
                }
            }
        }
    }

    public YamlConfiguration getWorldConfig(World world) {
        String worldName = mirrors.containsKey(world.getName()) ? mirrors.get(world.getName()) : world.getName();

        File folder = new File(plugin.getDataFolder(), worldName);
        File file = new File(folder, "chat.yml");

        if (!file.exists()) {
            folder.mkdirs();
            copy(plugin.getResource("chat.yml"), file);
        }

        return yamlCache.getUnchecked(file);
    }

    public Collection<String> getCommands(World world) {
        return getWorldConfig(world).getKeys(false);
    }

    public List<String> getBadWords() {
        return badWords;
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
