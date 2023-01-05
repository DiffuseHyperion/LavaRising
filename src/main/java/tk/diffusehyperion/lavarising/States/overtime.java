package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.javatuples.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.States.main.mainBossbars;

public class overtime {

    public static BossBar overtimeBossbar;
    public static BukkitRunnable overtimeTask;
    public static void triggerOvertime() {
        state = States.OVERTIME;
        gm.GamePlayer.playSoundToAll(Sound.ITEM_TOTEM_USE);

        for (Pair<BossBar, BukkitRunnable> pair : mainBossbars.values()) {
            pair.getValue0().removeAll();
            pair.getValue1().cancel();
        }

        world.getWorldBorder().setSize(config.getInt("game.overtime.finalBorderSize"), config.getInt("game.overtime.speed"));
        overtimeBossbar = Bukkit.createBossBar(config.getString("timers.overtime.name"),
                BarColor.valueOf(config.getString("timers.overtime.colour")),
                BarStyle.valueOf(config.getString("timers.overtime.style")),
                BarFlag.PLAY_BOSS_MUSIC);
        for (Player p : Bukkit.getOnlinePlayers()) {
            overtimeBossbar.addPlayer(p);
        }
        double[] timer = {0};
        overtimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] != config.getInt("game.overtime.speed")) {
                    timer[0] = BigDecimal.valueOf(timer[0]).add(BigDecimal.valueOf(0.1)).doubleValue();
                }
                overtimeBossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("game.overtime.speed")), 2, RoundingMode.HALF_EVEN).doubleValue());
            }
        };
        overtimeTask.runTaskTimer(plugin, 0, 2);
    }
}
