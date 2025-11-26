package com.xxxlc.hideandseek.command;

import com.xxxlc.hideandseek.HideAndSeek;
import com.xxxlc.hideandseek.manager.ConfigManager;
import com.xxxlc.hideandseek.manager.GameManager;
import com.xxxlc.hideandseek.manager.TeamManager;
import com.xxxlc.hideandseek.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HideSeekCommand implements CommandExecutor, TabCompleter {

    private final HideAndSeek plugin;
    private final GameManager gameManager;
    private final ConfigManager configManager;
    private final TeamManager teamManager;

    public HideSeekCommand(HideAndSeek plugin, GameManager gm, ConfigManager cm, TeamManager tm) {
        this.plugin = plugin;
        this.gameManager = gm;
        this.configManager = cm;
        this.teamManager = tm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        if (!args[0].equalsIgnoreCase("info") && !args[0].equalsIgnoreCase("help") && !p.hasPermission("hs.admin")) {
            MessageUtil.sendNoPermission(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(p);
                break;

            case "info":
                MessageUtil.sendInfo(p, "Hide and Seek",
                        " &fEstado: &7" + gameManager.getEstado(),
                        " &fProcuradores: &c" + teamManager.getQtdPegadores(),
                        " &fEscondedores: &a" + teamManager.getQtdEscondedores());
                break;

            case "start":
                gameManager.forcarInicio();
                MessageUtil.sendSuccess(p, "Ciclo de jogo iniciado forçadamente.");
                break;

            case "stop":
                gameManager.forcarParada();
                MessageUtil.sendSuccess(p, "O jogo foi parado e resetado.");
                break;

            case "itens":
                if (args.length < 2) {
                    MessageUtil.sendUsage(p, "/hs itens <pegador/escondedor>");
                    return true;
                }
                abrirMenu(p, args[1]);
                break;

            case "pegadorspawn":
                handleSpawn(p, args, "pegador_spawn", "Início do Mapa");
                break;

            case "escondedorspawn":
                handleSpawn(p, args, "escondedor_spawn", "Spawn Escondedores");
                break;

            case "endspawn":
                handleSpawn(p, args, "end_spawn", "Spawn Final");
                break;

            case "setpegadorespera":
                configManager.saveLocation("pegador_espera", p.getLocation());
                MessageUtil.sendSuccess(p, "Sala de Espera (2min) definida com sucesso!");
                break;

            case "setminplayers":
                if (args.length < 2) {
                    MessageUtil.sendUsage(p, "/hs setminplayers <qtd>");
                    return true;
                }
                try {
                    configManager.setMinPlayers(Integer.parseInt(args[1]));
                    MessageUtil.sendSuccess(p, "Mínimo de jogadores definido para: " + args[1]);
                } catch (NumberFormatException e) { MessageUtil.sendErrorMsg(p, "Número inválido."); }
                break;

            case "settempopartida":
                if (args.length < 2) {
                    MessageUtil.sendUsage(p, "/hs settempopartida <minutos>");
                    return true;
                }
                try {
                    configManager.setTempoJogo(Integer.parseInt(args[1]) * 60);
                    MessageUtil.sendSuccess(p, "Tempo de jogo definido para " + args[1] + " minutos.");
                } catch (NumberFormatException e) { MessageUtil.sendErrorMsg(p, "Número inválido."); }
                break;

            case "blacklist":
                handleBlacklist(p, args);
                break;

            default:
                MessageUtil.sendErrorMsg(p, "Comando desconhecido. Use /hs help.");
                break;
        }
        return true;
    }

    private void sendHelp(Player p) {
        MessageUtil.sendHelp(p, "Hide and Seek",
                " &e /hs start &8- &7Inicia o ciclo do jogo",
                " &e /hs stop &8- &7Para o jogo imediatamente",
                " &e /hs itens <kit> &8- &7Define kits iniciais",
                " &e /hs pegadorspawn set &8- &7Define onde a fera nasce",
                " &e /hs setpegadorespera &8- &7Define sala de espera (2min)",
                " &e /hs escondedorspawn set &8- &7Spawn dos escondedores",
                " &e /hs endspawn set &8- &7Spawn do fim",
                " &e /hs setminplayers <qtd> &8- &7Mínimo de players",
                " &e /hs blacklist <args> &8- &7Gerencia banidos do evento"
        );
    }

    private void handleSpawn(Player p, String[] args, String path, String nome) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("set")) {
            MessageUtil.sendUsage(p, "/hs " + args[0] + " set");
            return;
        }
        configManager.saveLocation(path, p.getLocation());
        MessageUtil.sendSuccess(p, nome + " definido com sucesso!");
    }

    private void handleBlacklist(Player p, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendUsage(p, "/hs blacklist <add/remove/list> [nick]");
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            MessageUtil.sendInfo(p, "Blacklist", "&fJogadores: &7" + configManager.getBlacklist().toString());
        } else if (args.length > 2) {
            String nick = args[2];
            if (args[1].equalsIgnoreCase("add")) {
                configManager.addBlacklist(nick);
                MessageUtil.sendSuccess(p, nick + " adicionado à Blacklist.");
            } else if (args[1].equalsIgnoreCase("remove")) {
                configManager.removeBlacklist(nick);
                MessageUtil.sendSuccess(p, nick + " removido da Blacklist.");
            }
        }
    }

    private void abrirMenu(Player p, String tipo) {
        String title = tipo.equalsIgnoreCase("pegador") ? "Config: pegador" : "Config: escondedor";
        Inventory inv = Bukkit.createInventory(null, 27, title);

        String path = "kits." + (tipo.equalsIgnoreCase("pegador") ? "pegador" : "escondedor");
        if(plugin.getConfig().contains(path)) {
            List<?> list = plugin.getConfig().getList(path);
            if(list != null) inv.setContents(list.toArray(new ItemStack[0]));
        }

        p.openInventory(inv);
        MessageUtil.sendSuccess(p, "Coloque os itens e feche para salvar.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> s = new ArrayList<>();
        if (args.length == 1) {
            s.add("start"); s.add("stop"); s.add("itens"); s.add("help"); s.add("info"); s.add("blacklist");
            s.add("pegadorspawn"); s.add("escondedorspawn"); s.add("endspawn"); s.add("setpegadorespera");
            s.add("setminplayers"); s.add("settempopartida");
        }
        if (args.length == 2 && args[0].equals("itens")) { s.add("pegador"); s.add("escondedor"); }
        if (args.length == 2 && args[0].equals("blacklist")) { s.add("add"); s.add("remove"); s.add("list"); }
        if (args.length == 2 && args[0].contains("spawn")) { s.add("set"); }
        return s;
    }
}