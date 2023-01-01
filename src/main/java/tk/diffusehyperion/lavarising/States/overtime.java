package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.diffusehyperion.gamemaster.ActionBars.ActionBarSender;
import tk.diffusehyperion.gamemaster.GameMaster;
import tk.diffusehyperion.gamemaster.GamePlayer;
import tk.diffusehyperion.gamemaster.Util.CompletableStringBuffer;
import tk.diffusehyperion.gamemaster.Util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class overtime {

    public static Pair<CompletableStringBuffer, BukkitRunnable> overtimeTimer;
    public void triggerOvertime(){
        state = States.OVERTIME;
        gm.GamePlayer.playSoundToAll(Sound.AMBIENCE_THUNDER);
        world.getWorldBorder().setSize(config.getInt("overtime.finalbordersize"), config.getInt("overtime.speed"));

        overtimeTimer = getOvertimeTimer();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ActionBarSender.sendUpdatingActionBar(p, overtimeTimer.getValue0(), 2);
        }
    }

    public Pair<CompletableStringBuffer, BukkitRunnable> getOvertimeTimer() {
        final int duration = config.getInt("overtime.speed");
        final String title = config.getString("overtime.title");

        final BigDecimal[] timer = new BigDecimal[]{BigDecimal.valueOf(duration).setScale(1, RoundingMode.HALF_UP)};
        final CompletableStringBuffer buffer = new CompletableStringBuffer();
        final StringBuffer stringBuffer = buffer.stringBuffer;
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                timer[0] = timer[0].subtract(BigDecimal.valueOf(0.1));

                stringBuffer.delete(0, stringBuffer.length());
                stringBuffer.append(gm.GamePlayer.getTimerStringWLogic(timer[0], duration, null, null));
                stringBuffer.append(" ");
                stringBuffer.append(replaceTitle(title, timer[0], BigDecimal.valueOf(duration).subtract(timer[0])));
            }
        };
        task.runTaskTimer(GameMaster.plugin, 0L, 2L);
        return new Pair<>(buffer, task);
    }

    private String replaceTitle(String title, BigDecimal timeLeft, BigDecimal timeElapsed) {
        String replacementTitle = title.replace(GamePlayer.timerReplacement.TIME_LEFT.toString(), timeLeft.toString());
        replacementTitle = replacementTitle.replace(GamePlayer.timerReplacement.TIME_ELAPSED.toString(), timeElapsed.toString());
        return replacementTitle;
    }
}
