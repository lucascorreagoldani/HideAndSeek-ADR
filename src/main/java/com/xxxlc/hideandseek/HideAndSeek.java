package com.xxxlc.hideandseek;

import com.xxxlc.hideandseek.command.HideSeekCommand;
import com.xxxlc.hideandseek.listener.GameListener;
import com.xxxlc.hideandseek.manager.ConfigManager;
import com.xxxlc.hideandseek.manager.GameManager;
import com.xxxlc.hideandseek.manager.TeamManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HideAndSeek extends JavaPlugin {

    private ConfigManager configManager;
    private TeamManager teamManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.teamManager = new TeamManager(this);
        this.gameManager = new GameManager(this, teamManager, configManager);

        if (getCommand("hs") != null) {
            HideSeekCommand cmd = new HideSeekCommand(this, gameManager, configManager, teamManager);
            getCommand("hs").setExecutor(cmd);
            getCommand("hs").setTabCompleter(cmd);
        }

        getServer().getPluginManager().registerEvents(new GameListener(this, gameManager, teamManager, configManager), this);

        gameManager.iniciarLoop();

        getLogger().info("HideAndSeek carregado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.cancelarLoop();
        }
        getLogger().info("HideAndSeek desativado.");
    }
}