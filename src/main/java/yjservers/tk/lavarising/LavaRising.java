package yjservers.tk.lavarising;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static yjservers.tk.lavarising.main.bossbars;
import static yjservers.tk.lavarising.start.starting;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public final class LavaRising extends JavaPlugin implements Listener {

    static pregame pregame;
    static FileConfiguration config;
    static World world;
    static String state;
    static Plugin plugin;
    static CountDownLatch waitforworld;

    ArrayList<Sound> attacksounds = new ArrayList<>();

    @Override
    public void onEnable() {
        if (config.getBoolean("debug.warnings")) {
            getLogger().warning("Yes, I know about the warns from bukkit about missing files, but its to be expected, and doesn't affect anything. Sorry for cluttering up your logs lol");
        }
        this.saveDefaultConfig();
        setupFields();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new start());
        // Objects.requireNonNull(this.getCommand("state")).setExecutor(new debugstate());
        Objects.requireNonNull(this.getCommand("reroll")).setExecutor(new reroll());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new main(), this);
        getServer().getPluginManager().registerEvents(new reroll(), this);
        try {
            waitforworld.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!config.getBoolean("debug.ignoreconfig.spawnprotection.ignore")) {
            core.editServerProperties("spawn-protection", "0", "spawn-protection=\\d+", "spawn-protection=0", "Spawn protection is not set to 0! This will cause building issues to players. Attempting to set to 0.",
                    "Spawn protection should be set to 0.");
        } else if (config.getInt("debug.ignoreconfig.spawnprotection.value") != 0) {
            core.editServerProperties("spawn-protection", String.valueOf(config.getInt("debug.ignoreconfig.spawnprotection.value")), "spawn-protection=\\d+", "spawn-protection=0", "Spawn protection is not set to 0! This will cause building issues to players. Attempting to set to 0.",
                    "Spawn protection should be set to 0.");
        }
        if (!config.getBoolean("debug.ignoreconfig.disablenether")) {
            core.editServerProperties("allow-nether", "false", "allow-nether=[a-zA-Z]+", "allow-nether=false", "Nether is enabled! This will cause cheating as players can go to the nether to escape the lava. Attempting to disable.",
                    "Nether should be disabled.");
        }
        if (!config.getBoolean("debug.ignoreconfig.disableend")) {
            core.editServerPropertiesYAML(new File("bukkit.yml"), "allow-end=[a-zA-Z]+", "false", "allow-end: true", "allow-end: false", "End is enabled! This causes unnecessary delays as the server will attempt to save the end, and slow down the server. Attempting to disable.",
                    "End should be disabled.");
        }
        if (!config.getBoolean("debug.ignoreconfig.allowflight")) {
            core.editServerProperties("allow-flight", "true", "allow-flight=[a-zA-Z]+", "allow-flight=true", "Minecraft's anticheat is enabled! This usually kicks more legit players than not. Attempting to disable for a more consistent player experience.",
                    "Anticheat should be disabled. If you want to have an anticheat, download one from spigot or skip this check in config.yml! Do be aware that you need to re-enable the anticheat after disabling the check.");
        }
        core.restartForConfig();
        pregame.createWorld();
        pregame.setupworld();
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
                core.playSound(attacksounds.get(new Random().nextInt(4)));
            }
        }
    }

    public void onLoad() {
        waitforworld = new CountDownLatch(1);
        config = this.getConfig();
        pregame = new pregame();
        pregame.deleteWorld();
    }

}
