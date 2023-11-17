package me.diffusehyperion.lavarising;

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
import me.diffusehyperion.gamemaster.Components.GamePlayer;
import me.diffusehyperion.gamemaster.Components.GameServer;
import me.diffusehyperion.gamemaster.Components.GameWorld;
import me.diffusehyperion.lavarising.Commands.reroll;
import me.diffusehyperion.lavarising.Commands.start;
import me.diffusehyperion.lavarising.States.States;
import me.diffusehyperion.lavarising.States.post;
import me.diffusehyperion.lavarising.States.pregame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static me.diffusehyperion.lavarising.States.main.mainBossbars;

public final class LavaRising extends JavaPlugin implements Listener {
    public static FileConfiguration config;
    public static World world;
    public static States state;
    public static Plugin plugin;
    public static Player winner;
    public static ArrayList<Sound> attacksounds = new ArrayList<>();

    public static GamePlayer GamePlayer;
    public static GameServer GameServer;
    public static GameWorld GameWorld;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();

        if (config.getBoolean("debug.warnings")) {
            getLogger().warning("Yes, I know about the warns from bukkit about missing files, but its to be expected, and doesn't affect anything. Sorry for cluttering up your logs lol");
        }

        setupFields();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new start());
        // Objects.requireNonNull(this.getCommand("state")).setExecutor(new debugstate());
        Objects.requireNonNull(this.getCommand("reroll")).setExecutor(new reroll());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new reroll(), this);
        getServer().getPluginManager().registerEvents(new pregame(), this);

        boolean requireResetConfig;
        boolean requireResetRestart = false;
        try {
            requireResetConfig = GameServer.checkForServerProperties(
                    config.getBoolean("debug.ignoreConfig.disableSpawnProtection"),
                    config.getBoolean("debug.ignoreConfig.disableNether"),
                    config.getBoolean("debug.ignoreConfig.disableEnd"),
                    config.getBoolean("debug.ignoreConfig.allowFlight"));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (config.getBoolean("debug.restartSetup.enabled")) {
            try {
                requireResetRestart = GameServer.setupRestart(me.diffusehyperion.gamemaster.Components.GameServer.OSTypes.valueOf(config.getString("debug.restartSetup.os", GameServer.getOS().toString())),
                        config.getString("debug.restartSetup.jar", GameServer.getServerJar().toString()));
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        if (requireResetConfig || requireResetRestart) {
            GameServer.restart();
        }

        world = GameWorld.createWorld(Objects.requireNonNull(config.getString("game.pregame.worldName")), config.getLong("game.pregame.seed", new Random().nextLong()));
        GameWorld.setupWorld(world, true, config.getDouble("game.pregame.borderSize"), 0, 0, 0);
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
        if (state == States.OVERTIME || state == States.MAIN) {
            Player player = event.getPlayer();
            mainBossbars.remove(player);

            event.setQuitMessage(ChatColor.YELLOW + Objects.requireNonNull(config.getString("game.main.deathMessage")).replace("%original%", Objects.requireNonNull(event.getQuitMessage()))
                    .replace("%player%", event.getPlayer().getName()).replace("%left%", String.valueOf(mainBossbars.size())));

            GamePlayer.playSoundToAll(attacksounds.get(new Random().nextInt(4)));

            if (mainBossbars.size() == 1) {
                winner = (Player) mainBossbars.keySet().toArray()[0];
                post.triggerPost(winner);
            }
        }
    }

    public void setupFields() {
        plugin = this;
        state = States.PREGAME;
        pregame.starting = false;
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_CRIT);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_STRONG);
        attacksounds.add(Sound.ENTITY_PLAYER_ATTACK_SWEEP);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        switch (state) {
            case PREGAME -> player.setGameMode(GameMode.ADVENTURE);
            case GRACE -> player.setGameMode(GameMode.SURVIVAL);
            case MAIN, OVERTIME -> player.setGameMode(GameMode.SPECTATOR);
            case POST -> setPostGamemode(player);
        }
    }

    private void setPostGamemode(Player player) {
        if (Objects.equals(LavaRising.config.getString("game.post.creativeMode"), "winner") && winner.equals(player)) {
            player.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(LavaRising.config.getString("game.post.creativeMode"), "all")) {
            player.setGameMode(GameMode.CREATIVE);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        switch (state) {
            case PREGAME -> {
                player.setGameMode(GameMode.ADVENTURE);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            }
            case GRACE -> {
                player.setGameMode(GameMode.SURVIVAL);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            }
            case MAIN, OVERTIME -> {
                player.setGameMode(GameMode.SPECTATOR);
                mainBossbars.get(player).getValue0().removeAll();
                mainBossbars.remove(player);

                event.setDeathMessage(ChatColor.YELLOW + Objects.requireNonNull(config.getString("game.main.deathMessage")).replace("%original%", Objects.requireNonNull(event.getDeathMessage()))
                        .replace("%player%", event.getEntity().getName()).replace("%left%", String.valueOf(mainBossbars.size())));

                GamePlayer.playSoundToAll(attacksounds.get(new Random().nextInt(4)));

                if (mainBossbars.size() <= 1) {
                    winner = (Player) mainBossbars.keySet().toArray()[0];
                    post.triggerPost(winner);
                }
            }
            case POST -> {
                setPostGamemode(player);
                event.setDeathMessage(ChatColor.YELLOW + event.getDeathMessage());
            }
        }
    }

    public void onLoad() {
        GamePlayer = new GamePlayer();
        GameServer = new GameServer();
        GameWorld = new GameWorld();
        try {
            GameWorld.deleteWorld();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
