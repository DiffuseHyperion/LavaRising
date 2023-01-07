package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import static tk.diffusehyperion.lavarising.States.main.mainTimers;

public class overtime {

    public static Pair<CompletableStringBuffer, BukkitRunnable> overtimeTimer;
    public static void triggerOvertime(){
        state = States.OVERTIME;
        GamePlayer.playSoundToAll(Sound.AMBIENCE_THUNDER);
        world.getWorldBorder().setSize(config.getInt("game.overtime.finalBorderSize"), config.getInt("game.overtime.speed"));

        for (Pair<CompletableStringBuffer, BukkitRunnable> pair : mainTimers.values()) {
            pair.getValue0().complete();
            pair.getValue1().cancel();
        }

        overtimeTimer = getOvertimeTimer();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ActionBarSender.sendUpdatingActionBar(p, overtimeTimer.getValue0(), 2);
        }
    }

    public static Pair<CompletableStringBuffer, BukkitRunnable> getOvertimeTimer() {
        final int duration = config.getInt("game.overtime.speed");
        final String title = config.getString("timers.overtime.name");

        final BigDecimal[] timer = new BigDecimal[]{BigDecimal.valueOf(duration).setScale(1, RoundingMode.HALF_UP)};
        final CompletableStringBuffer buffer = new CompletableStringBuffer();
        final StringBuffer stringBuffer = buffer.stringBuffer;
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                timer[0] = timer[0].subtract(BigDecimal.valueOf(0.1));

                stringBuffer.delete(0, stringBuffer.length());
                stringBuffer.append(GamePlayer.getTimerStringWLogic(timer[0], duration, null, null));
                stringBuffer.append(" ");
                stringBuffer.append(ChatColor.RESET);
                for (String s : config.getStringList("timers.overtime.style.delimiter")) {
                    stringBuffer.append(s);
                }
                stringBuffer.append("/");
                stringBuffer.append(ChatColor.RESET);
                stringBuffer.append(" ");
                stringBuffer.append(replaceTitle(title, timer[0], BigDecimal.valueOf(duration).subtract(timer[0])));
            }
        };
        task.runTaskTimer(GameMaster.plugin, 0L, 2L);
        return new Pair<>(buffer, task);
    }

    private static String replaceTitle(String title, BigDecimal timeLeft, BigDecimal timeElapsed) {
        String replacementTitle = title.replace(GamePlayer.timerReplacement.TIME_LEFT.toString(), timeLeft.toString());
        replacementTitle = replacementTitle.replace(GamePlayer.timerReplacement.TIME_ELAPSED.toString(), timeElapsed.toString());
        return replacementTitle;
    }
}
