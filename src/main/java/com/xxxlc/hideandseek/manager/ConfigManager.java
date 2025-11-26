package com.xxxlc.hideandseek.manager;

import com.xxxlc.hideandseek.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final HideAndSeek plugin;
    private List<String> blacklist;

    public ConfigManager(HideAndSeek plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        FileConfiguration c = plugin.getConfig();
        if (!c.contains("tempos.min_players")) c.set("tempos.min_players", 2);
        if (!c.contains("tempos.tempo_esconder")) c.set("tempos.tempo_esconder", 120);
        if (!c.contains("tempos.tempo_jogo")) c.set("tempos.tempo_jogo", 1200);
        if (!c.contains("blacklist")) c.set("blacklist", new ArrayList<String>());
        plugin.saveConfig();

        this.blacklist = c.getStringList("blacklist");
    }

    public int getMinPlayers() { return plugin.getConfig().getInt("tempos.min_players"); }
    public void setMinPlayers(int v) { plugin.getConfig().set("tempos.min_players", v); plugin.saveConfig(); }

    public int getTempoEsconder() { return plugin.getConfig().getInt("tempos.tempo_esconder"); }
    public int getTempoJogo() { return plugin.getConfig().getInt("tempos.tempo_jogo"); }
    public void setTempoJogo(int v) { plugin.getConfig().set("tempos.tempo_jogo", v); plugin.saveConfig(); }

    public void saveLocation(String path, Location loc) {
        FileConfiguration c = plugin.getConfig();
        c.set(path + ".world", loc.getWorld().getName());
        c.set(path + ".x", loc.getX()); c.set(path + ".y", loc.getY()); c.set(path + ".z", loc.getZ());
        c.set(path + ".yaw", loc.getYaw()); c.set(path + ".pitch", loc.getPitch());
        plugin.saveConfig();
    }

    public Location getLocation(String path) {
        FileConfiguration c = plugin.getConfig();
        if (!c.contains(path)) return null;
        return new Location(
                Bukkit.getWorld(c.getString(path + ".world")),
                c.getDouble(path + ".x"), c.getDouble(path + ".y"), c.getDouble(path + ".z"),
                (float) c.getDouble(path + ".yaw"), (float) c.getDouble(path + ".pitch")
        );
    }

    public void darKit(Player p, String tipo) {
        String path = "kits." + tipo;
        if (plugin.getConfig().contains(path)) {
            List<?> list = plugin.getConfig().getList(path);
            if (list != null && !list.isEmpty()) {
                try {
                    ItemStack[] items = list.toArray(new ItemStack[0]);
                    p.getInventory().setContents(items);
                    p.updateInventory();
                } catch (Exception ignored) {}
            }
        }
    }

    public void salvarKit(String tipo, Inventory inv) {
        plugin.getConfig().set("kits." + tipo, inv.getContents());
        plugin.saveConfig();
    }

    public boolean isBlacklisted(String nick) { return blacklist.contains(nick); }
    public void addBlacklist(String nick) {
        if(!blacklist.contains(nick)) { blacklist.add(nick); saveBlacklist(); }
    }
    public void removeBlacklist(String nick) {
        if(blacklist.remove(nick)) saveBlacklist();
    }
    private void saveBlacklist() {
        plugin.getConfig().set("blacklist", blacklist);
        plugin.saveConfig();
    }
    public List<String> getBlacklist() { return blacklist; }
}