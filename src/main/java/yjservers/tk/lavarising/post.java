package yjservers.tk.lavarising;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.Objects;

import static yjservers.tk.lavarising.LavaRising.config;
import static yjservers.tk.lavarising.LavaRising.state;
import static yjservers.tk.lavarising.main.winner;

public class post {

    public static void post(){
        core.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        state = "post";
        core.timer(config.getInt("post.duration"), config.getString("post.timername"), BarColor.WHITE, BarStyle.SOLID);
        if (Objects.equals(config.getString("post.creativemode"), "winner")) {
            winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(config.getString("post.creativemode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }
    }
}
