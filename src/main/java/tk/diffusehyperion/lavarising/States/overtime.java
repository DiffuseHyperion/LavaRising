package tk.diffusehyperion.lavarising.States;

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

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.States.main.mainBossbars;

public class overtime {
    public static void triggerOvertime() {
        state = States.OVERTIME;
        gm.GamePlayer.playSoundToAll(Sound.ITEM_TOTEM_USE);

        for (BossBar b : mainBossbars.values()) {
            b.removeAll();
        }
        mainBossbars.clear();

        world.getWorldBorder().setSize(config.getInt("game.overtime.finalBorderSize"), config.getInt("game.overtime.speed"));
        BossBar bossbar = Bukkit.createBossBar(config.getString("timers.overtime.name"),
                BarColor.valueOf(config.getString("timers.overtime.colour")),
                BarStyle.valueOf(config.getString("timers.overtime.style")),
                BarFlag.PLAY_BOSS_MUSIC);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        double[] timer = {0};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] != config.getInt("game.overtime.speed")) {
                    timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
                }
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("game.overtime.speed")), 2, RoundingMode.HALF_EVEN).doubleValue());
                if (state == States.POST) {
                    bossbar.removeAll();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(plugin, 0, 2);
    }
}
