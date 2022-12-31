package tk.diffusehyperion.lavarising;

import me.tigerhix.BossbarLib.BossbarLib;
import tk.diffusehyperion.lavarising.Commands.reroll;
import tk.diffusehyperion.lavarising.Commands.start;
import org.bukkit.*;
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
import tk.diffusehyperion.gamemaster.GameMaster;
import tk.diffusehyperion.gamemaster.GameServer;
import tk.diffusehyperion.lavarising.States.main;

import static tk.diffusehyperion.lavarising.States.main.bossbars;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public final class LavaRising extends JavaPlugin implements Listener {
    public static FileConfiguration config;
    public static World world;
    public static String state;
    public static Plugin plugin;
    public static GameMaster gm;

    public static BossbarLib barLib;
    ArrayList<Sound> attacksounds = new ArrayList<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();

        setupFields();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new start());
        // Objects.requireNonNull(this.getCommand("state")).setExecutor(new debugstate());
        Objects.requireNonNull(this.getCommand("reroll")).setExecutor(new reroll());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new main(), this);
        getServer().getPluginManager().registerEvents(new reroll(), this);
        getServer().getPluginManager().registerEvents(new start(), this);

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
        gm.GameWorld.setupWorld(world, true, config.getDouble("pregame.bordersize"), 0, 0, 0);

        barLib = BossbarLib.createFor(this, 2);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
    }

    public void setupFields() {
        plugin = this;
        state = "pregame";
        start.starting = false;
        attacksounds.add(Sound.SUCCESSFUL_HIT);
        attacksounds.add(Sound.WITHER_SHOOT);
        attacksounds.add(Sound.EXPLODE);
        attacksounds.add(Sound.ANVIL_LAND);
        attacksounds.add(Sound.AMBIENCE_THUNDER);
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
        gm = (GameMaster) getServer().getPluginManager().getPlugin("GameMaster");
        assert gm != null;
        try {
            gm.GameWorld.deleteWorld();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
