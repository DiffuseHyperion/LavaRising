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

import static tk.diffusehyperion.lavarising.LavaRising.config;
import static tk.diffusehyperion.lavarising.LavaRising.gm;

public class post {
    public static void triggerPost(){
        LavaRising.state = States.POST;
        gm.GamePlayer.playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", main.winner.getDisplayName());

        BossBar bossbar = gm.GamePlayer.customTimer(LavaRising.config.getInt("game.post.duration"),
                LavaRising.config.getString("timers.post.name"),
                BarColor.valueOf(config.getString("timers.post.colour")),
                BarStyle.valueOf(config.getString("timers.post.style")),
                hashMap,
                new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(LavaRising.config.getString("timers.post.kickMessage"));
                }
                gm.GameServer.restart();
            }
        }).getValue0();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        if (Objects.equals(LavaRising.config.getString("game.post.creativeMode"), "winner")) {
            main.winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(LavaRising.config.getString("game.post.creativeMode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }
    }
}
