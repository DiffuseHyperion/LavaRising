package yjservers.tk.lavarising;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
import tk.yjservers.gamemaster.GameMaster;
import tk.yjservers.gamemaster.GameServer;

import static yjservers.tk.lavarising.main.bossbars;
import static yjservers.tk.lavarising.start.starting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public final class LavaRising extends JavaPlugin implements Listener {
    static FileConfiguration config;
    static World world;
    static String state;
    static Plugin plugin;
    public static GameMaster gm;
    ArrayList<Sound> attacksounds = new ArrayList<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();
        gm = (GameMaster) getServer().getPluginManager().getPlugin("GameMaster");
        assert gm != null;

        if (config.getBoolean("debug.warnings")) {
            getLogger().warning("Yes, I know about the warns from bukkit about missing files, but its to be expected, and doesn't affect anything. Sorry for cluttering up your logs lol");
        }

        setupFields();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new start());
        // Objects.requireNonNull(this.getCommand("state")).setExecutor(new debugstate());
        Objects.requireNonNull(this.getCommand("reroll")).setExecutor(new reroll());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new main(), this);
        getServer().getPluginManager().registerEvents(new reroll(), this);

        boolean requireResetConfig;
        boolean requireResetRestart = false;
        try {
            requireResetConfig = gm.GameServer.checkForServerProperties(config.getBoolean("debug.ignoreconfig.disablespawnprotection"),
                    config.getBoolean("debug.ignoreconfig.disablenether"),
                    config.getBoolean("debug.ignoreconfig.disableend"),
                    config.getBoolean("debug.ignoreconfig.allowflight"));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (config.getBoolean("debug.restartsetup.enabled")) {
            try {
                requireResetRestart = gm.GameServer.setupRestart(GameServer.OSTypes.valueOf(config.getString("debug.restartsetup.os", gm.GameServer.getOS().toString())),
                        config.getString("debug.restartsetup.jar", gm.GameServer.getServerJar().toString()));
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        if (requireResetConfig || requireResetRestart) {
            gm.GameServer.restart();
        }

        world = gm.GameWorld.createWorld(config.getString("pregame.worldname"), config.getLong("pregame.seed", new Random().nextLong()));
        gm.GameWorld.setupWorld(world, false, config.getDouble("pregame.bordersize"), 0, 0, 0);
    }

    @Override
    public void onDisable() {
        if (config.getBoolean("debug.warnings")) {
            getLogger().warning("If people are kicked from the server closing, minecraft will complain about being unable to save their data. This is fine, and should be disregarded.");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (config.getBoolean("debug.warnings")) {
            getLogger().warning("Below this line (unless there are other plugins), minecraft will complain about not being able to save the leaving person's data. This is ok, and can be ignored.");
        }
    }

    public void setupFields() {
        plugin = this;
        state = "pregame";
        starting = false;
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_CRIT);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_STRONG);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_SWEEP);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        switch (state) {
            case "pregame" -> player.setGameMode(GameMode.ADVENTURE);
            case "grace", "starter" -> player.setGameMode(GameMode.SURVIVAL);
            case "main" -> player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        switch (state) {
            case "pregame" -> {
                player.setGameMode(GameMode.ADVENTURE);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            }
            case "grace" -> {
                player.setGameMode(GameMode.SURVIVAL);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            }
            case "main", "post", "overtime" -> {
                player.setGameMode(GameMode.SPECTATOR);
                bossbars.remove(player);
                event.setDeathMessage(ChatColor.YELLOW + Objects.requireNonNull(config.getString("main.deathmessage")).replace("%original%", Objects.requireNonNull(event.getDeathMessage()))
                        .replace("%player%", event.getEntity().getName()).replace("%left%", String.valueOf(bossbars.size())));
                gm.GamePlayer.playSoundToAll(attacksounds.get(new Random().nextInt(4)));
            }
        }
    }

    public void onLoad() {
        gm.GameWorld.deleteWorld(config.getString("pregame.worldname"));
    }

}
