package yjservers.tk.lavarising;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static yjservers.tk.lavarising.LavaRising.*;

public class overtime {
    public static void overtime(){
        gm.GamePlayer.playSoundToAll(Sound.ITEM_TOTEM_USE);
        state = "overtime";
        world.getWorldBorder().setSize(config.getInt("overtime.finalbordersize"), config.getInt("overtime.speed"));
        BossBar bossbar = Bukkit.createBossBar(config.getString("overtime.bartitle"), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        double[] timer = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] != config.getInt("overtime.speed")) {
                    timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
                }
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("overtime.speed")), 2, RoundingMode.HALF_EVEN).doubleValue());
                if (Objects.equals(state, "post")) {
                    bossbar.removeAll();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(plugin, 0, 2);
    }
}
