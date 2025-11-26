package com.xxxlc.hideandseek.util;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static void playNotification(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 2.0f);
    }

    public static void playError(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }

    public static void sendHelp(CommandSender sender, String sistema, String... comandos) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Comandos disponíveis para " + sistema));
        for (String cmd : comandos) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', cmd));
        }
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6OBS.: &7As informações com <> são obrigatórios e [] são opcionais"));
        sender.sendMessage("");

        if (sender instanceof Player) playNotification((Player) sender);
    }

    public static void sendUsage(CommandSender sender, String uso) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUtilize: " + uso + "."));
        if (sender instanceof Player) playError((Player) sender);
    }

    public static void sendErrorMsg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.RED + msg);
        if (sender instanceof Player) playError((Player) sender);
    }

    public static void sendSuccess(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.GREEN + msg);
        if (sender instanceof Player) playNotification((Player) sender);
    }

    public static void sendInfo(CommandSender sender, String titulo, String... infos) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Informações sobre " + titulo));
        for (String info : infos) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', info));
        }
        sender.sendMessage("");
        if (sender instanceof Player) playError((Player) sender);
    }

    public static void sendNoPermission(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cApenas o grupo Administrador ou superior pode executar esse comando."));
        if (sender instanceof Player) playError((Player) sender);
    }
}