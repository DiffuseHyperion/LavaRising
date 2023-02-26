package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tk.diffusehyperion.gamemaster.Events.FirstPlayerJoinEvent.FirstPlayerJoinEvent;
import tk.diffusehyperion.gamemaster.Utility.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.States.States.PREGAME;

public class pregame implements Listener {

    // rerolling related
    public static boolean rerollingEnabled = config.getBoolean("game.pregame.rerolling.enabled");
    public static BossBar rerollBossbar1;
    public static BukkitRunnable rerollTask1;
    public static BossBar rerollBossbar2;
    public static int requiredPlayers;
    public static int agreedPlayers = 0;
    public static ArrayList<Player> agreedList = new ArrayList<>();
    public static boolean allowedToReroll = false;

    // autostart related
    public static boolean autostartEnabled = config.getBoolean("game.pregame.start.autostart.enabled");
    public static BossBar autostartBossbar;
    public static boolean inIntermission = false;
    public static boolean inTruePregame = false;

    public static Pair<BossBar, BukkitRunnable> intermissionCountdown;

    public static int autostartPlayers = config.getInt("game.pregame.start.autostart.players");

    // global
    public static List<BossBar> pregameStaticBossbars = new ArrayList<>();
    public static boolean pregameWaitingEnabled = config.getBoolean("game.pregame.start.waitingBossbar");
    public static BossBar pregameWaitingBossbar; // waiting for game to start...
    public static boolean starting;
    public static BossBar startingBossbar;

    @EventHandler
    public void firstPlayerJoinEvent(FirstPlayerJoinEvent e) {
        if (config.getBoolean("game.pregame.rerolling.enabled")) {
            Pair<BossBar, BukkitRunnable> pair = GamePlayer.timer(config.getInt("game.pregame.rerolling.rerollTimer"),
                    config.getString("timers.pregame.rerolling.enabling.name"),
                    BarColor.valueOf(config.getString("timers.pregame.rerolling.enabling.colour")),
                    BarStyle.valueOf(config.getString("timers.pregame.rerolling.enabling.style")),
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            triggerPregame();
                        }
                    });
            rerollBossbar1 = pair.getValue0();
            rerollTask1 = pair.getValue1();
            rerollBossbar1.addPlayer(e.getPlayer());
        } else {
            triggerPregame();
        }
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent e) {
        if (tk.diffusehyperion.gamemaster.Components.GameServer.playersJoinedBefore && state == PREGAME) {
            Player p = e.getPlayer();
            if (rerollingEnabled) {
                if (allowedToReroll) {
                    requiredPlayers = getRequiredPlayers();
                    rerollBossbar2.setTitle(Objects.requireNonNull(config.getString("timers.pregame.rerolling.enabled.name")).replace("%required%", String.valueOf(requiredPlayers)));

                    rerollBossbar2.addPlayer(p);
                } else {
                    rerollBossbar1.addPlayer(p);
                }
            }
            if (autostartEnabled && inTruePregame) {
                triggerIntermission();
            }
            if (pregameWaitingEnabled && Objects.nonNull(pregameWaitingBossbar)) {
                pregameWaitingBossbar.addPlayer(p);
            }
        }
    }

    @EventHandler
    public void playerDisconnectEvent(PlayerQuitEvent e) {
        if (rerollingEnabled) {
            requiredPlayers = getRequiredPlayers();
        }
        if (autostartEnabled && inTruePregame) {
            if (inIntermission && Bukkit.getOnlinePlayers().size() - 1 < autostartPlayers) { // bukkit.getonlineplayers include the disconnecting player
                inIntermission = false;
                intermissionCountdown.getValue1().cancel();

                intermissionCountdown.getValue0().removeAll();
                GamePlayer.showBossbarToAll(autostartBossbar);
            }
        }
    }

    public static int getRequiredPlayers() {
        int requiredPlayers = BigDecimal.valueOf(config.getInt("game.pregame.rerolling.percentageToPass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
        return requiredPlayers == 0 ? 1 : requiredPlayers;
    }

    public void triggerPregame() {
        inTruePregame = true;
        if (pregameWaitingEnabled) {
            pregameWaitingBossbar = Bukkit.createBossBar(
                    Objects.requireNonNull(config.getString("timers.pregame.waiting.name")),
                    BarColor.valueOf(config.getString("timers.pregame.waiting.colour")),
                    BarStyle.valueOf(config.getString("timers.pregame.waiting.style")),
                    BarFlag.PLAY_BOSS_MUSIC);
            pregameWaitingBossbar.setProgress(1);
            pregameStaticBossbars.add(pregameWaitingBossbar);
            GamePlayer.showBossbarToAll(pregameWaitingBossbar);
        }
        if (rerollingEnabled) {
            requiredPlayers = getRequiredPlayers();

            rerollBossbar2 = Bukkit.createBossBar(
                    Objects.requireNonNull(config.getString("timers.pregame.rerolling.enabled.name")).replace("%required%", String.valueOf(requiredPlayers)),
                    BarColor.valueOf(config.getString("timers.pregame.rerolling.enabled.colour")),
                    BarStyle.valueOf(config.getString("timers.pregame.rerolling.enabled.style")),
                    BarFlag.PLAY_BOSS_MUSIC);
            rerollBossbar2.setProgress(1);
            GamePlayer.showBossbarToAll(rerollBossbar2);

            allowedToReroll = true;
        }
        if (config.getBoolean("game.pregame.start.autostart.enabled")) {
            autostartBossbar = Bukkit.createBossBar(
                    Objects.requireNonNull(config.getString("timers.pregame.autostart.waiting.name")).replace("%required%", String.valueOf(autostartPlayers)),
                    BarColor.valueOf(config.getString("timers.pregame.autostart.waiting.colour")),
                    BarStyle.valueOf(config.getString("timers.pregame.autostart.waiting.style")),
                    BarFlag.PLAY_BOSS_MUSIC);
            autostartBossbar.setProgress(1);
            pregameStaticBossbars.add(autostartBossbar);
            GamePlayer.showBossbarToAll(autostartBossbar);

            triggerIntermission();
        }
    }

    private void triggerIntermission() {
        if (!inIntermission && Bukkit.getOnlinePlayers().size() >= autostartPlayers) {
            inIntermission = true;
            intermissionCountdown = GamePlayer.timer(config.getInt("game.pregame.start.autostart.intermission"), config.getString("timers.pregame.autostart.intermission.name"),
                    BarColor.valueOf(config.getString("timers.pregame.autostart.intermission.colour")),
                    BarStyle.valueOf(config.getString("timers.pregame.autostart.intermission.style")),
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            startGame();
                        }
                    });
            autostartBossbar.removeAll();
            GamePlayer.showBossbarToAll(intermissionCountdown.getValue0());
        }
    }

    public static void startGame() {
        starting = true;
        for (BossBar b : pregameStaticBossbars) {
            b.removeAll();
        }
        if (rerollingEnabled && Objects.nonNull(rerollBossbar2)) {
            rerollBossbar2.removeAll();
        }
        if (config.getBoolean("game.pregame.start.countdown")) {
            startingBossbar = GamePlayer.timer(config.getInt("game.pregame.start.timer"), config.getString("timers.pregame.start.name"),
                    BarColor.valueOf(config.getString("timers.pregame.start.colour")),
                    BarStyle.valueOf(config.getString("timers.pregame.start.style")),
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            grace.triggerGrace();
                        }
                    }).getValue0();
            for (Player p : Bukkit.getOnlinePlayers()) {
                startingBossbar.addPlayer(p);
            }
        } else {
            grace.triggerGrace();
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        if (starting) {
            startingBossbar.addPlayer(e.getPlayer());
        }
    }
}
