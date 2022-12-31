package tk.diffusehyperion.lavarising.States;

import tk.diffusehyperion.lavarising.LavaRising;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class post {

    public void triggerPost(){
        state = States.POSTGAME;
        gm.GamePlayer.playSoundToAll(Sound.FIREWORK_BLAST);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", main.winner.getDisplayName());
        for (Player p : Bukkit.getOnlinePlayers()) {
            gm.GamePlayer.customTimer(p, LavaRising.config.getInt("post.duration"), LavaRising.config.getString("post.timername"), hashMap);
        }
        BukkitRunnable stopServer = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(LavaRising.config.getString("post.kickmessage"));
                    gm.GameServer.restart();
                }
            }
        };
        stopServer.runTaskLater(plugin, LavaRising.config.getInt("post.duration") * 20L);

        if (Objects.equals(LavaRising.config.getString("post.creativemode"), "winner")) {
            main.winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(LavaRising.config.getString("post.creativemode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }
    }
}
