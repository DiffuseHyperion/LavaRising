package me.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.diffusehyperion.gamemaster.Utility.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

import static me.diffusehyperion.lavarising.LavaRising.*;
import static me.diffusehyperion.lavarising.States.overtime.triggerOvertime;

import me.diffusehyperion.gamemaster.Components.GamePlayer;
import me.diffusehyperion.gamemaster.Components.GameWorld;

public class main {
    public static HashMap<Player, Pair<BossBar, BukkitRunnable>> mainBossbars = new HashMap<>();
    public static int lavaheight;
    public static double[] timer;
    public static BukkitRunnable lavaRiser;

    public static void triggerMain(){
        state = States.MAIN;
        GamePlayer.playSoundToAll(Sound.ENTITY_WITHER_AMBIENT);
        world.setPVP(true);

        int defaultHeight;
        String version = Bukkit.getBukkitVersion();
        int minorVersion = Integer.parseInt(version.substring(version.indexOf(".") + 1, version.indexOf(".") + 3));
        if (minorVersion >= 18) {
            defaultHeight = -63;
        } else {
            defaultHeight = 1;
        }
        lavaheight = config.getInt("game.main.beginHeight", defaultHeight);
        Bukkit.getLogger().info("Lava will begin at y=" + lavaheight);

        timer = new double[]{0};
        int coords = BigDecimal.valueOf(config.getInt("game.grace.finalBorderSize")).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();
        lavaRiser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= config.getInt("game.main.lavaInterval")) {
                    GameWorld.fillBlocks(new Location(world, -coords, lavaheight, -coords), new Location(world, coords, lavaheight, coords), Material.getMaterial(Objects.requireNonNull(config.getString("game.main.setBlock"))));
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= config.getInt("game.overtime.threshold")) {
                    triggerOvertime();
                    this.cancel();
                }
                timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
            }
        };
        lavaRiser.runTaskTimer(plugin,0, 2);
        for (Player p : Bukkit.getOnlinePlayers()) {
            mainTimer(p);
        }
        if (config.getBoolean("game.overtime.warning.enabled")) {
            BossBar bossbar = GamePlayer.timer(config.getInt("game.overtime.warning.time"),
                    Objects.requireNonNull(config.getString("timers.overtime.warning.name")).replace("%threshold%", String.valueOf(config.getInt("game.overtime.threshold"))),
                    BarColor.valueOf(config.getString("timers.overtime.warning.colour")),
                    BarStyle.valueOf(config.getString("timers.overtime.warning.style"))).getValue0();

            for (Player p : Bukkit.getOnlinePlayers()) {
                bossbar.addPlayer(p);
            }
        }
    }

    public static void mainTimer(Player player) {
        BossBar bossbar = Bukkit.createBossBar(config.getString("timers.main.name"),
                BarColor.valueOf(config.getString("timers.main.colour")),
                BarStyle.valueOf(config.getString("timers.main.style")),
                BarFlag.PLAY_BOSS_MUSIC);
        bossbar.addPlayer(player);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                bossbar.setTitle(Objects.requireNonNull(config.getString("timers.main.name")).replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight)).replace("%level%", String.valueOf(lavaheight)));
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("game.main.lavaInterval")), 2, RoundingMode.HALF_EVEN).doubleValue());
            }
        };
        mainBossbars.put(player, new Pair<>(bossbar, task));
        task.runTaskTimer(plugin, 0, 2);
    }
}
