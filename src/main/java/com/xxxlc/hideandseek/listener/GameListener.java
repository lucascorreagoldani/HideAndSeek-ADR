package com.xxxlc.hideandseek.listener;

import com.xxxlc.hideandseek.HideAndSeek;
import com.xxxlc.hideandseek.manager.ConfigManager;
import com.xxxlc.hideandseek.manager.GameManager;
import com.xxxlc.hideandseek.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameListener implements Listener {

    private final HideAndSeek plugin;
    private final GameManager gameManager;
    private final TeamManager teamManager;
    private final ConfigManager configManager;

    public GameListener(HideAndSeek plugin, GameManager gm, TeamManager tm, ConfigManager cm) {
        this.plugin = plugin;
        this.gameManager = gm;
        this.teamManager = tm;
        this.configManager = cm;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (gameManager.getEstado() != GameManager.GameState.AGUARDANDO && !e.getPlayer().hasPermission("hs.admin")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        GameManager.GameState state = gameManager.getEstado();
        if (state != GameManager.GameState.JOGANDO && state != GameManager.GameState.ESCONDENDO) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        boolean pegador = teamManager.isPegador(p);
        boolean escondedor = teamManager.isEscondedor(p);
        boolean espectador = !pegador && !escondedor;

        if (espectador) {
            String msg = ChatColor.GRAY + "[ESPECTADOR] " + p.getName() + ": " + e.getMessage();
            for (Player rec : Bukkit.getOnlinePlayers()) {
                if (teamManager.isEspectador(rec)) rec.sendMessage(msg);
            }
            if(!teamManager.isEspectador(p)) p.sendMessage(msg);
        } else {
            String prefix = pegador ? ChatColor.RED + "[PROCURADOR] " : ChatColor.BLUE + "[ESCONDEDOR] ";
            String msg = prefix + p.getName() + ChatColor.WHITE + ": " + e.getMessage();
            for (Player rec : Bukkit.getOnlinePlayers()) rec.sendMessage(msg);
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent e) {
        if (gameManager.getEstado() != GameManager.GameState.JOGANDO) {
            if (e.getEntity() instanceof Player && !e.getEntity().hasPermission("hs.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        if (!(e.getEntity() instanceof Player)) return;
        Player vitima = (Player) e.getEntity();

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
            return;
        }

        if (e instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                return;
            }
        }

        if (vitima.getHealth() - e.getFinalDamage() <= 0) {

            if (teamManager.isEscondedor(vitima)) {
                e.setCancelled(true);
                Bukkit.broadcastMessage(ChatColor.RED + "☠ " + ChatColor.YELLOW + vitima.getName() +
                        ChatColor.GRAY + " morreu para o ambiente e virou um " + ChatColor.RED + "PROCURADOR!");
                gameManager.tornarPegador(vitima, true);
            }

            else if (teamManager.isPegador(vitima)) {
                e.setCancelled(true);

                Location respawn = configManager.getLocation("pegador_spawn");
                if (respawn != null) vitima.teleport(respawn);

                vitima.setHealth(20);
                vitima.setFoodLevel(20);

                vitima.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 0));
                configManager.darKit(vitima, "pegador");

                vitima.sendMessage(ChatColor.RED + "☠ VOCÊ MORREU!");
                vitima.sendMessage(ChatColor.GRAY + "Cuidado com o ambiente. Você renasceu no início.");
            }
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (gameManager.getEstado() != GameManager.GameState.JOGANDO) return;
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;

        Player vitima = (Player) e.getEntity();
        Player atacante = (Player) e.getDamager();

        boolean vPeg = teamManager.isPegador(vitima);
        boolean aPeg = teamManager.isPegador(atacante);
        boolean vEsc = teamManager.isEscondedor(vitima);
        boolean aEsc = teamManager.isEscondedor(atacante);

        if ((vPeg && aPeg) || (vEsc && aEsc)) {
            e.setCancelled(true);
            return;
        }

        if (vEsc && aPeg) {
            if (vitima.getHealth() - e.getFinalDamage() <= 0) {
                e.setCancelled(true);
                Bukkit.broadcastMessage(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + vitima.getName() +
                        ChatColor.GRAY + " foi encontrado por " + ChatColor.RED + atacante.getName() + "!");
                gameManager.tornarPegador(vitima, true);
            }
        }

        if (vPeg && aEsc) {
            if (vitima.getHealth() - e.getFinalDamage() <= 0) {
                e.setCancelled(true);
                Bukkit.broadcastMessage(ChatColor.RED + "🛡 O Procurador " + ChatColor.BOLD + vitima.getName() +
                        ChatColor.RED + " foi abatido pelo Escondedor " + ChatColor.BLUE + atacante.getName() + "!");

                Location respawn = configManager.getLocation("pegador_spawn");
                if (respawn != null) vitima.teleport(respawn);

                vitima.setHealth(20);
                vitima.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 0));

                configManager.darKit(vitima, "pegador");

                vitima.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "VOCÊ FOI ABATIDO!");
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (gameManager.getEstado() == GameManager.GameState.JOGANDO) {
            Player p = e.getPlayer();
            if (teamManager.isPegador(p)) {
                Location spawn = configManager.getLocation("pegador_spawn");
                if (spawn != null) e.setRespawnLocation(spawn);

                configManager.darKit(p, "pegador");
                p.setGlowing(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        e.getDrops().clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player p = e.getPlayer();
        gameManager.resetarJogador(p);

        if (gameManager.getEstado() == GameManager.GameState.JOGANDO || gameManager.getEstado() == GameManager.GameState.ESCONDENDO) {
            p.setGameMode(GameMode.SPECTATOR);
            p.sendMessage(ChatColor.GRAY + "Partida em andamento. Você é um espectador.");
        } else {
            if (configManager.isBlacklisted(p.getName())) {
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(ChatColor.RED + "Você está na Blacklist.");
            } else {
                gameManager.setupEscondedor(p);
                Location loc = configManager.getLocation("escondedor_spawn");
                if (loc != null) p.teleport(loc);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        teamManager.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        String title = e.getView().getTitle();
        if (title.equals("Config: pegador")) {
            configManager.salvarKit("pegador", e.getInventory());
            e.getPlayer().sendMessage(ChatColor.GREEN + "Kit Procurador salvo!");
        } else if (title.equals("Config: escondedor")) {
            configManager.salvarKit("escondedor", e.getInventory());
            e.getPlayer().sendMessage(ChatColor.GREEN + "Kit Escondedor salvo!");
        }
    }

    @EventHandler public void onBreak(BlockBreakEvent e) { if(!e.getPlayer().hasPermission("hs.admin")) e.setCancelled(true); }
    @EventHandler public void onPlace(BlockPlaceEvent e) { if(!e.getPlayer().hasPermission("hs.admin")) e.setCancelled(true); }
    @EventHandler public void onMob(CreatureSpawnEvent e) { if(e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) e.setCancelled(true); }
    @EventHandler public void onWeather(WeatherChangeEvent e) { if(e.toWeatherState()) e.setCancelled(true); }
    @EventHandler public void onHunger(FoodLevelChangeEvent e) { e.setFoodLevel(20); e.setCancelled(true); }
}