package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tk.diffusehyperion.gamemaster.GameMaster;
import tk.diffusehyperion.gamemaster.Util.CompletableStringBuffer;
import tk.diffusehyperion.gamemaster.Util.Pair;
import tk.diffusehyperion.lavarising.LavaRising;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.state;

public class main implements Listener {
    public static int lavaheight;
    public static Player winner;
    public static int[] timer;

    public static HashMap<Player, Pair<CompletableStringBuffer, BukkitRunnable>> mainTimers = new HashMap<>();

    public static void triggerMain(){
        state = States.MAIN;

        LavaRising.gm.GamePlayer.playSoundToAll(Sound.WITHER_IDLE);
        LavaRising.world.setPVP(true);

        lavaheight = 1;
        timer = new int[]{0};
        int borderRadius = BigDecimal.valueOf(LavaRising.config.getInt("grace.finalbordersize")).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();

        BukkitRunnable lavariser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= LavaRising.config.getInt("main.lavainterval")) {
                    LavaRising.gm.GameWorld.fillBlocks(new Location(LavaRising.world, -borderRadius, lavaheight, -borderRadius), new Location(LavaRising.world, borderRadius, lavaheight, borderRadius), Material.LAVA);
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= LavaRising.config.getInt("overtime.threshold")) {
                    new overtime().triggerOvertime();
                    this.cancel();
                }
                if (state == States.POSTGAME) {
                    this.cancel();
                }
                timer[0] += 1;
            }
        };
        lavariser.runTaskTimer(LavaRising.plugin,0, 20);
        for (Player p : Bukkit.getOnlinePlayers()) {
            getMainTimer(p);
        }
        if (LavaRising.config.getBoolean("overtime.warning.enabled")) {
            overtimewarning();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == States.MAIN || state == States.OVERTIME) {
            mainTimers.remove(event.getEntity());
            if (mainTimers.size() == 1) {
                winner = (Player) mainTimers.keySet().toArray()[0];
                new post().triggerPost();
            }
        }
    }

    public static void getMainTimer(Player player) {
        final CompletableStringBuffer buffer = new CompletableStringBuffer();
        final StringBuffer stringBuffer = buffer.stringBuffer;
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                stringBuffer.delete(0, stringBuffer.length());
                stringBuffer.append(Objects.requireNonNull(LavaRising.config.getString("main.timername"))
                        .replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight))
                        .replace("%level%", String.valueOf(lavaheight)));

                if (state == States.OVERTIME || state == States.POSTGAME) {
                    buffer.complete();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(GameMaster.plugin, 0L, 2L);

        mainTimers.put(player, new Pair<>(buffer, task));
    }

    public static void overtimewarning() {
        Bukkit.broadcastMessage(LavaRising.config.getString("overtime.warning.message").replace("%threshold%", String.valueOf(LavaRising.config.getInt("overtime.threshold"))));
    }
}
