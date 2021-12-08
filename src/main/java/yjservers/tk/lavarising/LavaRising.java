package yjservers.tk.lavarising;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
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
import org.bukkit.scheduler.BukkitRunnable;

import static yjservers.tk.lavarising.main.bossbars;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public final class LavaRising extends JavaPlugin implements CommandExecutor, Listener {

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
        Objects.requireNonNull(this.getCommand("start")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("state")).setExecutor(new debugstate());
        Objects.requireNonNull(this.getCommand("reroll")).setExecutor(new reroll());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new main(), this);
        getServer().getPluginManager().registerEvents(new reroll(), this);
        try {
            waitforworld.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        core.editServerProperties("spawn-protection", "0", "spawn-protection=\\d+", "spawn-protection=0", "Spawn protection is not set to 0! This will cause building issues to players. Attempting to set to 0.",
                "Spawn protection should be set to 0.");
        core.editServerProperties("allow-nether", "false", "allow-nether=true", "allow-nether=false", "Nether is enabled! This will cause cheating as players can go to the nether to escape the lava. Attempting to disable.",
                "Nether should be disabled.");
        core.editServerPropertiesYAML(new File("bukkit.yml"), "allow-end=[a-zA-Z]+", "false", "allow-end: true", "allow-end: false", "End is enabled! This causes unnessary delays as the server will attempt to save the end, even though it should not be accessable. Attempting to disable.",
                 "End should be disabled.");
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
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_CRIT);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_STRONG);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_SWEEP);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
                sender.sendMessage("There is not enough players...");
        } else {
            sender.sendMessage("Start command received!");
            core.timer(5, "Starting in %timer% seconds!", BarColor.GREEN, BarStyle.SOLID);
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    yjservers.tk.lavarising.grace.gracesetup();
                }
            };
            task.runTaskLater(this, 100);
        }
        return true;
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
