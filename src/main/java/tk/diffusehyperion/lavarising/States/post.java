package tk.diffusehyperion.lavarising.States;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tk.diffusehyperion.gamemaster.ActionBars.ActionBarSender;
import tk.diffusehyperion.gamemaster.GamePlayer;
import tk.diffusehyperion.gamemaster.GameServer;
import tk.diffusehyperion.gamemaster.Util.CompletableStringBuffer;
import tk.diffusehyperion.lavarising.Commands.TimerColourParser;
import tk.diffusehyperion.lavarising.LavaRising;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.States.overtime.overtimeTimer;

public class post implements Listener {

    public static CompletableStringBuffer postTimer;

    public static void triggerPost(){
        state = States.POSTGAME;
        GamePlayer.playSoundToAll(Sound.FIREWORK_BLAST);

        if (Objects.equals(LavaRising.config.getString("game.post.creativeMode"), "winner")) {
            main.winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(LavaRising.config.getString("game.post.creativeMode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", main.winner.getDisplayName());

        postTimer = GamePlayer.timer(config.getInt("game.post.duration"), config.getString("timers.post.name"), new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(config.getString("game.post.kickMessage"));
                    GameServer.restart();
                }
            }
        }, hashMap, config.getInt("timers.post.style.size"), TimerColourParser.getTimerColour("timers.post.style")).getValue0();

        if (Objects.nonNull(overtimeTimer)) {
            overtimeTimer.getValue0().complete();
            overtimeTimer.getValue1().cancel();
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            ActionBarSender.sendUpdatingActionBar(p, postTimer, 2);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (state == States.POSTGAME) {
            ActionBarSender.sendUpdatingActionBar(e.getPlayer(), postTimer, 2);
        }
    }
}
