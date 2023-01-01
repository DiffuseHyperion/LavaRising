package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tk.diffusehyperion.gamemaster.ActionBars.ActionBarSender;
import tk.diffusehyperion.gamemaster.GamePlayer;
import tk.diffusehyperion.gamemaster.Util.CompletableStringBuffer;
import tk.diffusehyperion.lavarising.Commands.TimerColourParser;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class grace implements Listener {

    public static CompletableStringBuffer graceTimer;

    public static void triggerGrace(){
        state = States.GRACE;
        for (Player p: Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
        }
        WorldBorder border = world.getWorldBorder();
        border.setSize(config.getDouble("game.grace.finalBorderSize"), config.getLong("game.grace.speed"));
        border.setWarningDistance(0);
        graceTimer = GamePlayer.timer(config.getInt("game.grace.duration"), config.getString("timers.grace.name"), new BukkitRunnable() {
            @Override
            public void run() {
                main.triggerMain();
            }
        }, null, config.getInt("timers.grace.style.size"), TimerColourParser.getTimerColour("timers.grace.style")).getValue0();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ActionBarSender.sendUpdatingActionBar(p, graceTimer, 2);
        }
        GamePlayer.playSoundToAll(Sound.ENDERDRAGON_GROWL);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (state == States.GRACE) {
            ActionBarSender.sendUpdatingActionBar(e.getPlayer(), graceTimer, 2);
        }
    }

}
