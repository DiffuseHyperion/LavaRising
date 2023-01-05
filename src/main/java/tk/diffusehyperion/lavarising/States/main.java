package tk.diffusehyperion.lavarising.States;

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
import tk.diffusehyperion.lavarising.LavaRising;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class main {

    public static HashMap<Player, BossBar> mainBossbars = new HashMap<>();
    public static int lavaheight;
    public static double[] timer;

    public static void triggerMain(){
        LavaRising.state = States.MAIN;
        gm.GamePlayer.playSoundToAll(Sound.ENTITY_WITHER_AMBIENT);
        world.setPVP(true);
        String version = Bukkit.getBukkitVersion();
        int minorVersion = Integer.parseInt(version.substring(version.indexOf(".") + 1, version.indexOf(".") + 3));
        if (minorVersion >= 18) {
            Bukkit.getLogger().info("Detected version >=1.18! Lava starts at -63.");
            lavaheight = -63;
        } else {
            Bukkit.getLogger().info("Detected version <1.18! Lava starts at -1.");
            lavaheight = 1;
        }
        timer = new double[]{0};
        int coords = BigDecimal.valueOf(LavaRising.config.getInt("game.grace.finalbordersize")).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();
        BukkitRunnable lavariser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= LavaRising.config.getInt("game.main.lavaInterval")) {
                    gm.GameWorld.fillBlocks(new Location(world, -coords, lavaheight, -coords), new Location(world, coords, lavaheight, coords), Material.LAVA);
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= LavaRising.config.getInt("game.overtime.threshold")) {
                    overtime.triggerOvertime();
                    this.cancel();
                }
                if (state == States.POST) {
                    this.cancel();
                }
                timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
            }
        };
        lavariser.runTaskTimer(LavaRising.plugin,0, 2);
        for (Player p : Bukkit.getOnlinePlayers()) {
            mainTimer(p);
        }
        if (LavaRising.config.getBoolean("game.overtime.warning.enabled")) {
            BossBar bossbar = gm.GamePlayer.timer(config.getInt("game.overtime.warning.time"),
                    config.getString("timers.overtime.warning.name"),
                    BarColor.valueOf(config.getString("timers.overtime.warning.colour")),
                    BarStyle.valueOf(config.getString("timers.overtime.warning.style"))).getValue0();

            for (Player p : Bukkit.getOnlinePlayers()) {
                bossbar.addPlayer(p);
            }
        }
    }

    public static void mainTimer(Player player) {
        BossBar bossbar = Bukkit.createBossBar(LavaRising.config.getString("timers.main.name"),
                BarColor.valueOf(config.getString("timers.main.colour")),
                BarStyle.valueOf(config.getString("timers.main.style")),
                BarFlag.PLAY_BOSS_MUSIC);
        mainBossbars.put(player, bossbar);
        bossbar.addPlayer(player);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                bossbar.setTitle(Objects.requireNonNull(LavaRising.config.getString("timers.main.name")).replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight)).replace("%level%", String.valueOf(lavaheight)));
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(LavaRising.config.getInt("game.main.lavaInterval")), 2, RoundingMode.HALF_EVEN).doubleValue());
                if (state == States.OVERTIME || state == States.POST) {
                    bossbar.removeAll();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(LavaRising.plugin, 0, 2);
    }
}
