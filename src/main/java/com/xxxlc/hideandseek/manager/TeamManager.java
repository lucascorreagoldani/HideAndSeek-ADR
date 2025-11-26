package com.xxxlc.hideandseek.manager;

import com.xxxlc.hideandseek.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class TeamManager {

    private final HideAndSeek plugin;
    private final List<String> listaPegadores = new ArrayList<>();
    private final List<String> listaEscondedores = new ArrayList<>();

    public TeamManager(HideAndSeek plugin) {
        this.plugin = plugin;
    }

    public void clearLists() {
        listaPegadores.clear();
        listaEscondedores.clear();
    }

    public void addPegador(Player p) {
        listaEscondedores.remove(p.getName());
        if (!listaPegadores.contains(p.getName())) listaPegadores.add(p.getName());
        p.setGlowing(true);
    }

    public void addEscondedor(Player p) {
        listaPegadores.remove(p.getName());
        if (!listaEscondedores.contains(p.getName())) listaEscondedores.add(p.getName());
        p.setGlowing(false);
    }

    public void removePlayer(Player p) {
        listaPegadores.remove(p.getName());
        listaEscondedores.remove(p.getName());
    }

    public boolean isPegador(Player p) { return listaPegadores.contains(p.getName()); }
    public boolean isEscondedor(Player p) { return listaEscondedores.contains(p.getName()); }
    public boolean isEspectador(Player p) { return !isPegador(p) && !isEscondedor(p); }

    public int getQtdPegadores() { return listaPegadores.size(); }
    public int getQtdEscondedores() { return listaEscondedores.size(); }
    public List<String> getListaEscondedores() { return listaEscondedores; }
    public List<String> getListaPegadores() { return listaPegadores; }

    public void atualizarScoreboard(Player p, String estado, int tempo) {
        Scoreboard board = p.getScoreboard();
        if (board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        if (board.getObjective("HideSeek") != null) board.getObjective("HideSeek").unregister();
        Objective obj = board.registerNewObjective("HideSeek", Criteria.DUMMY, ChatColor.YELLOW + "" + ChatColor.BOLD + "ESCONDE-ESCONDE");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        setLine(obj, ChatColor.GRAY + "----------------", 7);
        setLine(obj, ChatColor.WHITE + "Fase: " + ChatColor.GREEN + estado, 6);
        setLine(obj, ChatColor.WHITE + "Tempo: " + ChatColor.YELLOW + formatTime(tempo), 5);
        setLine(obj, " ", 4);
        setLine(obj, ChatColor.RED + "Procuradores: " + listaPegadores.size(), 3);
        setLine(obj, ChatColor.BLUE + "Escondedores: " + listaEscondedores.size(), 2);
        setLine(obj, ChatColor.GRAY + "---------------- ", 1);

        Team timePeg = getOrCreateTeam(board, "Pegadores", ChatColor.RED, "[PROCURADOR] ");
        Team timeEsc = getOrCreateTeam(board, "Escondedores", ChatColor.BLUE, "[ESCONDEDOR] ");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isPegador(online)) timePeg.addEntry(online.getName());
            else if (isEscondedor(online)) timeEsc.addEntry(online.getName());
        }

        if (p.getScoreboard() != board) p.setScoreboard(board);
    }

    private void setLine(Objective obj, String text, int score) {
        Score s = obj.getScore(text);
        s.setScore(score);
    }

    private String formatTime(int s) { return String.format("%02d:%02d", s / 60, s % 60); }

    private Team getOrCreateTeam(Scoreboard board, String name, ChatColor color, String prefix) {
        Team t = board.getTeam(name);
        if (t == null) t = board.registerNewTeam(name);
        t.setColor(color);
        t.setPrefix(color + prefix);
        t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        return t;
    }
}