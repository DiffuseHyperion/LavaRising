package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class grace {

    public void triggerGrace(){
        state = "grace";
        for (Player p: Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
        }
        WorldBorder border = world.getWorldBorder();
        border.setSize(config.getDouble("grace.finalbordersize"), config.getLong("grace.speed"));
        border.setWarningDistance(0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            gm.GamePlayer.timer(p, config.getInt("grace.duration"), config.getString("grace.timername"), new BukkitRunnable() {
                @Override
                public void run() {
                    new main().triggerMain();
                }
            });
        }
        gm.GamePlayer.playSoundToAll(Sound.ENDERDRAGON_GROWL);
    }
}
