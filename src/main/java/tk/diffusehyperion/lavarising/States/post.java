package tk.diffusehyperion.lavarising.States;

import org.bukkit.boss.BossBar;
import tk.diffusehyperion.lavarising.LavaRising;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.gm;

public class post {

    public void triggerPost(){
        gm.GamePlayer.playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        LavaRising.state = "post";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", main.winner.getDisplayName());
        BossBar bossbar = gm.GamePlayer.customTimer(LavaRising.config.getInt("post.duration"), LavaRising.config.getString("post.timername"), BarColor.WHITE, BarStyle.SOLID, hashMap, new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(LavaRising.config.getString("post.kickmessage"));
                    gm.GameServer.restart();
                }
            }
        }).getValue0();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        if (Objects.equals(LavaRising.config.getString("post.creativemode"), "winner")) {
            main.winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(LavaRising.config.getString("post.creativemode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }
    }
}
