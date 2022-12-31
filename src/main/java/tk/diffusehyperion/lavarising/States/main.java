package tk.diffusehyperion.lavarising.States;

import me.tigerhix.lib.bossbar.Bossbar;
import tk.diffusehyperion.lavarising.LavaRising;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.barLib;

public class main implements Listener {

    public static HashMap<Player, Bossbar> bossbars = new HashMap<>();
    public static int lavaheight;
    public static Player winner;
    public static double[] timer;

    public void triggerMain(){
        LavaRising.state = "main";
        LavaRising.gm.GamePlayer.playSoundToAll(Sound.WITHER_IDLE);
        LavaRising.world.setPVP(true);
        if (Bukkit.getServer().getClass().getPackage().getName().contains("1_18")) {
            Bukkit.getLogger().info("Detected version 1.18! Lava starts at -63.");
            lavaheight = -63;
        } else {
            Bukkit.getLogger().info("Detected version <1.18! Lava starts at -1.");
            lavaheight = 1;
        }
        timer = new double[]{0};
        int bordersize = (int) LavaRising.world.getWorldBorder().getSize();
        int coords = BigDecimal.valueOf(bordersize).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();
        BukkitRunnable lavariser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= LavaRising.config.getInt("main.lavainterval")) {
                    LavaRising.gm.GameWorld.fillBlocks(new Location(LavaRising.world, -coords, lavaheight, -coords), new Location(LavaRising.world, coords, lavaheight, coords), Material.LAVA);
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= LavaRising.config.getInt("overtime.threshold")) {
                    new overtime().triggerOvertime();
                    this.cancel();
                }
                if (Objects.equals(LavaRising.state, "post")) {
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
        if (Objects.equals(LavaRising.state, "main") || Objects.equals(LavaRising.state, "overtime")) {
            bossbars.remove(event.getEntity());
            if (bossbars.size() == 1) {
                winner = (Player) bossbars.keySet().toArray()[0];
                new post().triggerPost();
            }
        }
    }

    public void mainTimer(Player player) {
        Bossbar bossbar = barLib.getBossbar(player);
        bossbar.setMessage(LavaRising.config.getString("main.timername"));

        bossbars.put(player, bossbar);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                bossbar.setMessage(Objects.requireNonNull(LavaRising.config.getString("main.timername")).replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight)).replace("%level%", String.valueOf(lavaheight)));
                if (Objects.equals(LavaRising.state, "overtime") || Objects.equals(LavaRising.state, "post")) {
                    barLib.clearBossbar(player);
                    this.cancel();
                }
                bossbar.setPercentage(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(LavaRising.config.getInt("main.lavainterval")), 2, RoundingMode.HALF_EVEN).floatValue());
            }
        };
        task.runTaskTimer(LavaRising.plugin, 0, 2);
    }

    public void overtimewarning() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Bossbar bossbar = barLib.getBossbar(p);
            bossbar.setMessage(Objects.requireNonNull(LavaRising.config.getString("overtime.warning.message")).replace("%threshold%", String.valueOf(LavaRising.config.getInt("overtime.threshold"))));
            double[] timer = {0};
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
                    bossbar.setPercentage(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(LavaRising.config.getInt("overtime.warning.time")), 2, RoundingMode.HALF_EVEN).floatValue());
                    if (timer[0] >= LavaRising.config.getInt("overtime.warning.time")) {
                        barLib.clearBossbar(p);
                        this.cancel();
                    }
                }
            };
            task.runTaskTimer(LavaRising.plugin, 0, 2);
        }
    }
}
