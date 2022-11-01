package yjservers.tk.lavarising;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

import static yjservers.tk.lavarising.LavaRising.*;
import static yjservers.tk.lavarising.main.winner;

public class post {

    public static void post(){
        gm.GamePlayer.playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        state = "post";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", winner.getDisplayName());
        gm.GamePlayer.customTimer(config.getInt("post.duration"), config.getString("post.timername"), BarColor.WHITE, BarStyle.SOLID, hashMap, new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(config.getString("post.kickmessage"));
                }
            }
        });
        if (Objects.equals(config.getString("post.creativemode"), "winner")) {
            winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(config.getString("post.creativemode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }
    }
}
