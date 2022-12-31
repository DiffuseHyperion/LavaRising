package tk.diffusehyperion.lavarising.States;

import me.tigerhix.lib.bossbar.Bossbar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class overtime {
    public void triggerOvertime(){
        gm.GamePlayer.playSoundToAll(Sound.AMBIENCE_THUNDER);
        state = "overtime";
        world.getWorldBorder().setSize(config.getInt("overtime.finalbordersize"), config.getInt("overtime.speed"));
        for (Player p : Bukkit.getOnlinePlayers()) {
            Bossbar bossbar = barLib.getBossbar(p);
            bossbar.setMessage(config.getString("overtime.bartitle"));
            int[] timer = {0};
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (timer[0] != config.getInt("overtime.speed")) {
                        timer[0] += 1;
                    }
                    bossbar.setPercentage(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(config.getInt("overtime.speed")), 2, RoundingMode.HALF_EVEN).floatValue());
                    if (Objects.equals(state, "post")) {
                        barLib.clearBossbar(p);
                        this.cancel();
                    }
                }
            };
            task.runTaskTimer(plugin, 0, 20);
        }
    }
}
