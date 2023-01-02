package tk.diffusehyperion.lavarising.States;

import tk.diffusehyperion.lavarising.LavaRising;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class main implements Listener {

    public static HashMap<Player, BossBar> bossbars = new HashMap<>();
    public static int lavaheight;
    public static Player winner;
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
        int coords = BigDecimal.valueOf(LavaRising.config.getInt("grace.finalbordersize")).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();
        BukkitRunnable lavariser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= LavaRising.config.getInt("main.lavainterval")) {
                    gm.GameWorld.fillBlocks(new Location(world, -coords, lavaheight, -coords), new Location(world, coords, lavaheight, coords), Material.LAVA);
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= LavaRising.config.getInt("overtime.threshold")) {
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
        if (LavaRising.config.getBoolean("overtime.warning.enabled")) {
            overtimewarning();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == States.MAIN || state == States.OVERTIME) {
            bossbars.remove(event.getEntity());
            if (bossbars.size() == 1) {
                winner = (Player) bossbars.keySet().toArray()[0];
                post.triggerPost();
            }
        }
    }

    public static void mainTimer(Player player) {
        BossBar bossbar = Bukkit.createBossBar(LavaRising.config.getString("main.timername"), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        bossbars.put(player, bossbar);
        bossbar.addPlayer(player);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                bossbar.setTitle(Objects.requireNonNull(LavaRising.config.getString("main.timername")).replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight)).replace("%level%", String.valueOf(lavaheight)));
                if (state == States.OVERTIME || state == States.POST) {
                    bossbar.removeAll();
                    this.cancel();
                }
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(LavaRising.config.getInt("main.lavainterval")), 2, RoundingMode.HALF_EVEN).doubleValue());
            }
        };
        task.runTaskTimer(LavaRising.plugin, 0, 2);
    }

    public static void overtimewarning() {
        BossBar bossbar = Bukkit.createBossBar(Objects.requireNonNull(LavaRising.config.getString("overtime.warning.message")).replace("%threshold%", String.valueOf(LavaRising.config.getInt("overtime.threshold"))), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        double[] timer = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(LavaRising.config.getInt("overtime.warning.time")), 2, RoundingMode.HALF_EVEN).doubleValue());
                if (timer[0] >= LavaRising.config.getInt("overtime.warning.time")) {
                    bossbar.removeAll();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(LavaRising.plugin, 0, 2);
    }
}
