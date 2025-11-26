package com.xxxlc.hideandseek.manager;

import com.xxxlc.hideandseek.HideAndSeek;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {

    public enum GameState { AGUARDANDO, INICIANDO, ESCONDENDO, JOGANDO, ENCERRANDO }

    private final HideAndSeek plugin;
    private final TeamManager teamManager;
    private final ConfigManager configManager;

    private GameState estadoAtual = GameState.AGUARDANDO;
    private BukkitTask task;
    private int timer = 0;

    public GameManager(HideAndSeek plugin, TeamManager teamManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.configManager = configManager;
    }

    public GameState getEstado() { return estadoAtual; }

    public void iniciarLoop() {
        cancelarLoop();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancelarLoop() {
        if (task != null && !task.isCancelled()) task.cancel();
    }

    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            teamManager.atualizarScoreboard(p, estadoAtual.name(), timer);
        }

        switch (estadoAtual) {
            case AGUARDANDO:
                if (Bukkit.getOnlinePlayers().size() >= configManager.getMinPlayers()) {
                    estadoAtual = GameState.INICIANDO;
                    timer = 10;
                }
                break;

            case INICIANDO:
                if (Bukkit.getOnlinePlayers().size() < configManager.getMinPlayers()) {
                    estadoAtual = GameState.AGUARDANDO;
                    Bukkit.broadcastMessage(ChatColor.RED + "Jogadores insuficientes. Cancelando...");
                    return;
                }
                timer--;
                if (timer <= 5 && timer > 0) {
                    broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1f);
                    broadcastTitle(ChatColor.YELLOW + "" + timer, "");
                }
                if (timer <= 0) iniciarFaseEsconder();
                break;

            case ESCONDENDO:
                timer--;
                if (timer <= 5 && timer > 0) {
                    broadcastTitle(ChatColor.RED + "" + timer, ChatColor.YELLOW + "A Fera vai sair!");
                    broadcastSound(Sound.UI_BUTTON_CLICK, 1f);
                }
                if (timer <= 0) liberarFera();
                break;

            case JOGANDO:
                timer--;
                if (timer == 900 || timer == 600 || timer == 300 || timer == 60) {
                    ativarRadar();
                }
                verificarFim();
                break;

            case ENCERRANDO:
                timer--;
                if (timer <= 0) reiniciarCiclo();
                break;
        }
    }

    private void iniciarFaseEsconder() {
        estadoAtual = GameState.ESCONDENDO;
        timer = configManager.getTempoEsconder();

        List<Player> validos = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!configManager.isBlacklisted(p.getName())) validos.add(p);
            else {
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(ChatColor.GRAY + "Você está na Blacklist e será espectador.");
            }
        }

        if (validos.isEmpty()) {
            estadoAtual = GameState.AGUARDANDO;
            return;
        }

        teamManager.clearLists();

        Player pegador = validos.get(new Random().nextInt(validos.size()));
        setupPegador(pegador, false);

        Location espera = configManager.getLocation("pegador_espera");
        if (espera != null) pegador.teleport(espera);
        else pegador.sendMessage(ChatColor.RED + "AVISO: Spawn de espera não definido.");

        broadcastTitle(ChatColor.RED + "PROCURADOR: " + pegador.getName(), ChatColor.YELLOW + "Escondam-se por 2 minutos!");
        Bukkit.broadcastMessage(ChatColor.RED + "O PROCURADOR É: " + ChatColor.BOLD + pegador.getName());

        Location spawnEscondedor = configManager.getLocation("escondedor_spawn");
        for (Player p : validos) {
            if (!p.equals(pegador)) {
                setupEscondedor(p);
                if (spawnEscondedor != null) p.teleport(spawnEscondedor);
                p.sendMessage(ChatColor.BLUE + "Você tem 2 minutos para se esconder!");
            }
        }
    }

    private void liberarFera() {
        estadoAtual = GameState.JOGANDO;
        timer = configManager.getTempoJogo();

        broadcastTitle(ChatColor.DARK_RED + "A FERA SAIU!", ChatColor.YELLOW + "Sobrevivam por 20 minutos!");
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "A FERA SAIU! O PROCURADOR FOI LIBERADO!");
        broadcastSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f);

        Location mapStart = configManager.getLocation("pegador_spawn");
        for (String nick : teamManager.getListaPegadores()) {
            Player p = Bukkit.getPlayer(nick);
            if (p != null && mapStart != null) {
                p.teleport(mapStart);
                p.sendMessage(ChatColor.RED + "VÁ PROCURAR!");
            }
        }
    }

    private void verificarFim() {
        if (teamManager.getQtdEscondedores() == 0) encerrar(ChatColor.RED + "OS PROCURADORES VENCERAM!");
        else if (timer <= 0) encerrar(ChatColor.BLUE + "OS ESCONDEDORES VENCERAM!");
    }

    private void encerrar(String motivo) {
        broadcastSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f);
        broadcastTitle(ChatColor.GOLD + "FIM DE JOGO", motivo);
        Bukkit.broadcastMessage(ChatColor.GOLD + "=========================");
        Bukkit.broadcastMessage("      " + motivo);
        Bukkit.broadcastMessage(ChatColor.GOLD + "=========================");

        estadoAtual = GameState.ENCERRANDO;
        timer = 10;

        Location end = configManager.getLocation("end_spawn");
        if (end != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(end);
                resetarJogador(p);
                p.setGameMode(GameMode.ADVENTURE);
            }
        }
    }

    private void reiniciarCiclo() {
        teamManager.clearLists();
        estadoAtual = GameState.AGUARDANDO;
        Bukkit.broadcastMessage(ChatColor.GREEN + "Ciclo reiniciado.");
        for (Player p : Bukkit.getOnlinePlayers()) {
            teamManager.addEscondedor(p);
            resetarJogador(p);
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void tornarPegador(Player p, boolean foiPego) {
        setupPegador(p, true);

        Location map = configManager.getLocation("pegador_spawn");
        if (map != null) p.teleport(map);

        if (foiPego) {
            p.sendMessage(ChatColor.RED + "Você foi encontrado! Agora é um Procurador!");
            p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1f, 1f);
            p.sendTitle(ChatColor.DARK_RED + "ENCONTRADO!", ChatColor.YELLOW + "Ajude a procurar os outros!", 5, 40, 5);
        }
    }

    public void setupPegador(Player p, boolean inGame) {
        teamManager.addPegador(p);
        resetarJogador(p);
        configManager.darKit(p, "pegador");
        p.setGameMode(GameMode.ADVENTURE);
        if(inGame) p.updateInventory();
    }

    public void setupEscondedor(Player p) {
        teamManager.addEscondedor(p);
        resetarJogador(p);
        configManager.darKit(p, "escondedor");
        p.setGameMode(GameMode.ADVENTURE);
    }

    public void resetarJogador(Player p) {
        p.getInventory().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setGlowing(false);
        p.setWalkSpeed(0.2f);
        for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
    }

    private void ativarRadar() {
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⚠ RADAR ATIVADO! ⚠");
        broadcastSound(Sound.BLOCK_BEACON_ACTIVATE, 1f);
        broadcastTitle(ChatColor.GOLD + "RADAR", ChatColor.YELLOW + "Escondedores visíveis!");

        for (String nick : teamManager.getListaEscondedores()) {
            Player p = Bukkit.getPlayer(nick);
            if (p != null) p.setGlowing(true);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (estadoAtual == GameState.JOGANDO) {
                    for (String nick : teamManager.getListaEscondedores()) {
                        Player p = Bukkit.getPlayer(nick);
                        if (p != null) p.setGlowing(false);
                    }
                    Bukkit.broadcastMessage(ChatColor.GREEN + "O Radar foi desativado.");
                }
            }
        }.runTaskLater(plugin, 30 * 20L);
    }

    private void broadcastSound(Sound s, float vol) {
        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), s, vol, 1f);
    }
    private void broadcastTitle(String t, String s) {
        for (Player p : Bukkit.getOnlinePlayers()) p.sendTitle(t, s, 10, 70, 20);
    }

    public void forcarInicio() {
        if (estadoAtual == GameState.AGUARDANDO || estadoAtual == GameState.INICIANDO) {
            iniciarFaseEsconder();
        }
    }
}