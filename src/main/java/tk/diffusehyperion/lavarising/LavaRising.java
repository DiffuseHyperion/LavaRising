package tk.diffusehyperion.lavarising;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tk.diffusehyperion.gamemaster.GameServer;
import tk.diffusehyperion.gamemaster.GameWorld;
import tk.diffusehyperion.lavarising.Commands.reroll;
import tk.diffusehyperion.lavarising.Commands.start;
import tk.diffusehyperion.lavarising.States.States;
import tk.diffusehyperion.lavarising.States.grace;
import tk.diffusehyperion.lavarising.States.main;
import tk.diffusehyperion.lavarising.States.post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public final class LavaRising extends JavaPlugin implements Listener {
    public static FileConfiguration config;
    public static World world;
    public static States state;
    public static Plugin plugin;
    public static ArrayList<Sound> deathSounds = new ArrayList<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();

        setupFields();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new start());
        // Objects.requireNonNull(this.getCommand("state")).setExecutor(new debugstate());
        Objects.requireNonNull(this.getCommand("reroll")).setExecutor(new reroll());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new grace(), this);
        getServer().getPluginManager().registerEvents(new main(), this);
        getServer().getPluginManager().registerEvents(new post(), this);

        getServer().getPluginManager().registerEvents(new reroll(), this);
        getServer().getPluginManager().registerEvents(new start(), this);

        boolean requireResetConfig;
        boolean requireResetRestart = false;
        try {
            requireResetConfig = GameServer.checkForServerProperties(config.getBoolean("debug.ignoreconfig.disablespawnprotection"),
                    config.getBoolean("debug.ignoreconfig.disablenether"),
                    config.getBoolean("debug.ignoreconfig.disableend"),
                    config.getBoolean("debug.ignoreconfig.allowflight"));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (config.getBoolean("debug.restartsetup.enabled")) {
            try {
                requireResetRestart = GameServer.setupRestart(GameServer.OSTypes.valueOf(config.getString("debug.restartsetup.os", GameServer.getOS().toString())),
                        config.getString("debug.restartsetup.jar", GameServer.getServerJar().toString()));
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        if (requireResetConfig || requireResetRestart) {
            GameServer.restart();
        }

        world = GameWorld.createWorld(config.getString("pregame.worldname"), config.getLong("pregame.seed", new Random().nextLong()));
        GameWorld.setupWorld(world, true, config.getDouble("pregame.bordersize"), 0, 0, 0);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
    }

    public void setupFields() {
        plugin = this;
        state = States.PREGAME;
        start.starting = false;
        deathSounds.add(Sound.SUCCESSFUL_HIT);
        deathSounds.add(Sound.WITHER_SHOOT);
        deathSounds.add(Sound.EXPLODE);
        deathSounds.add(Sound.ANVIL_LAND);
        deathSounds.add(Sound.AMBIENCE_THUNDER);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        switch (state) {
            case PREGAME: player.setGameMode(GameMode.ADVENTURE);
            case GRACE: player.setGameMode(GameMode.SURVIVAL);
            case MAIN: player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        switch (state) {
            case PREGAME:
                player.setGameMode(GameMode.ADVENTURE);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            case GRACE:
                player.setGameMode(GameMode.SURVIVAL);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            case MAIN:
                player.setGameMode(GameMode.SPECTATOR);
            case OVERTIME:
                player.setGameMode(GameMode.SPECTATOR);
            case POSTGAME:
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
                if (Objects.equals(LavaRising.config.getString("post.creativemode"), "winner") && main.winner.equals(player)) {
                    player.setGameMode(GameMode.CREATIVE);
                } else if (Objects.equals(LavaRising.config.getString("post.creativemode"), "all")) {
                    player.setGameMode(GameMode.CREATIVE);
                } else {
                    player.setGameMode(GameMode.ADVENTURE);
                }
        }
    }

    public void onLoad() {
        try {
            GameWorld.deleteWorld();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
