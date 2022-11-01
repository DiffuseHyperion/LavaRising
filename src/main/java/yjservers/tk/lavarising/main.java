package yjservers.tk.lavarising;

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

import static yjservers.tk.lavarising.LavaRising.*;

public class main implements Listener {

    static HashMap<Player, BossBar> bossbars = new HashMap<>();
    static int lavaheight;
    static Player winner;
    static double[] timer;

    public static void mainsetup(){
        state = "main";
        gm.GamePlayer.playSoundToAll(Sound.ENTITY_WITHER_AMBIENT);
        world.setPVP(true);
        if (Bukkit.getServer().getClass().getPackage().getName().contains("1_18")) {
            Bukkit.getLogger().info("Detected version 1.18! Lava starts at -63.");
            lavaheight = -63;
        } else {
            Bukkit.getLogger().info("Detected version <1.18! Lava starts at -1.");
            lavaheight = 1;
        }
        timer = new double[]{0};
        int bordersize = (int) world.getWorldBorder().getSize();
        int coords = BigDecimal.valueOf(bordersize).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();
        BukkitRunnable lavariser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= config.getInt("main.lavainterval")) {
                    gm.GameWorld.fillBlocks(new Location(world, -coords, lavaheight, -coords), new Location(world, coords, lavaheight, coords), Material.LAVA);
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= config.getInt("overtime.threshold")) {
                    yjservers.tk.lavarising.overtime.overtime();
                    this.cancel();
                }
                if (Objects.equals(state, "post")) {
                    this.cancel();
                }
                timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
            }
        };
        lavariser.runTaskTimer(plugin,0, 2);
        for (Player p : Bukkit.getOnlinePlayers()) {
            mainTimer(p);
        }
        if (config.getBoolean("overtime.warning.enabled")) {
            overtimewarning();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Objects.equals(state, "main") || Objects.equals(state, "overtime")) {
            bossbars.remove(event.getEntity());
            if (bossbars.size() == 1) {
                winner = (Player) bossbars.keySet().toArray()[0];
                yjservers.tk.lavarising.post.post();
            }
        }
    }

    public static void mainTimer(Player player) {
        BossBar bossbar = Bukkit.createBossBar(config.getString("main.timername"), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        bossbars.put(player, bossbar);
        bossbar.addPlayer(player);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                bossbar.setTitle(Objects.requireNonNull(config.getString("main.timername")).replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight)).replace("%level%", String.valueOf(lavaheight)));
                if (Objects.equals(state, "overtime") || Objects.equals(state, "post")) {
                    bossbar.removeAll();
                    this.cancel();
                }
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("main.lavainterval")), 2, RoundingMode.HALF_EVEN).doubleValue());
            }
        };
        task.runTaskTimer(plugin, 0, 2);
    }

    public static void overtimewarning() {
        BossBar bossbar = Bukkit.createBossBar(config.getString("overtime.warning.message").replace("%threshold%", String.valueOf(config.getInt("overtime.threshold"))), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        double[] timer = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("overtime.warning.time")), 2, RoundingMode.HALF_EVEN).doubleValue());
                if (timer[0] >= config.getInt("overtime.warning.time")) {
                    bossbar.removeAll();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(plugin, 0, 2);
    }
}
