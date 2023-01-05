package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.States.main.lavaRiser;
import static tk.diffusehyperion.lavarising.States.main.mainBossbars;

public class post {
    public static void triggerPost(Player winner){
        state = States.POST;
        gm.GamePlayer.playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE);

        lavaRiser.cancel();

        for (BossBar b : mainBossbars.values()) {
            b.removeAll();
        }
        mainBossbars.clear();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", winner.getDisplayName());

        BossBar bossbar = gm.GamePlayer.customTimer(config.getInt("game.post.duration"),
                config.getString("timers.post.name"),
                BarColor.valueOf(config.getString("timers.post.colour")),
                BarStyle.valueOf(config.getString("timers.post.style")),
                hashMap,
                new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(config.getString("timers.post.kickMessage"));
                }
                gm.GameServer.restart();
            }
        }).getValue0();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        if (Objects.equals(config.getString("game.post.creativeMode"), "winner")) {
            winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(config.getString("game.post.creativeMode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }
    }
}
