package tk.diffusehyperion.lavarising.States;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tk.diffusehyperion.gamemaster.ActionBars.ActionBarSender;
import tk.diffusehyperion.gamemaster.GameMaster;
import tk.diffusehyperion.gamemaster.GamePlayer;
import tk.diffusehyperion.gamemaster.GameWorld;
import tk.diffusehyperion.gamemaster.Util.CompletableStringBuffer;
import tk.diffusehyperion.gamemaster.Util.Pair;
import tk.diffusehyperion.lavarising.LavaRising;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import static tk.diffusehyperion.lavarising.LavaRising.*;

public class main implements Listener {
    public static int lavaheight;
    public static Player winner;
    public static int[] timer;

    public static HashMap<Player, Pair<CompletableStringBuffer, BukkitRunnable>> mainTimers = new HashMap<>();

    public static void triggerMain(){
        state = States.MAIN;

        GamePlayer.playSoundToAll(Sound.WITHER_IDLE);
        LavaRising.world.setPVP(true);

        lavaheight = 1;
        timer = new int[]{0};
        int borderRadius = BigDecimal.valueOf(config.getInt("game.grace.finalBorderSize")).divide(BigDecimal.valueOf(2), RoundingMode.UP).intValue();

        BukkitRunnable lavariser = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer[0] >= config.getInt("game.main.lavaInterval")) {
                    GameWorld.fillBlocks(new Location(LavaRising.world, -borderRadius, lavaheight, -borderRadius), new Location(LavaRising.world, borderRadius, lavaheight, borderRadius), Material.LAVA);
                    lavaheight++;
                    timer[0] = 0;
                }
                if (lavaheight >= config.getInt("game.overtime.threshold")) {
                    overtime.triggerOvertime();
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
        if (config.getBoolean("game.overtime.warning.enabled")) {
            overtimewarning();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == States.MAIN || state == States.OVERTIME) {
            mainTimers.remove(event.getEntity());
            event.setDeathMessage(ChatColor.YELLOW + Objects.requireNonNull(config.getString("game.main.deathMessage"))
                    .replace("%original%", Objects.requireNonNull(event.getDeathMessage()))
                    .replace("%player%", event.getEntity().getName())
                    .replace("%left%", String.valueOf(mainTimers.size())));
            GamePlayer.playSoundToAll(deathSounds.get(new Random().nextInt(deathSounds.size())));

            if (mainTimers.size() == 1) {
                winner = (Player) mainTimers.keySet().toArray()[0];
                post.triggerPost();
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (state == States.MAIN || state == States.OVERTIME) {
            mainTimers.remove(event.getPlayer());
            event.setQuitMessage(ChatColor.YELLOW + Objects.requireNonNull(config.getString("game.main.quitMessage"))
                    .replace("%original%", Objects.requireNonNull(event.getQuitMessage()))
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%left%", String.valueOf(mainTimers.size())));
            GamePlayer.playSoundToAll(deathSounds.get(new Random().nextInt(deathSounds.size())));

            if (mainTimers.size() == 1) {
                winner = (Player) mainTimers.keySet().toArray()[0];
                post.triggerPost();
            }
        }
    }

    public static void getMainTimer(Player player) {
        Bukkit.getLogger().info("triggered for player: " + player.getDisplayName());
        final CompletableStringBuffer buffer = new CompletableStringBuffer();
        final StringBuffer stringBuffer = buffer.stringBuffer;
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                stringBuffer.delete(0, stringBuffer.length());
                stringBuffer.append(Objects.requireNonNull(config.getString("game.main.indicatorTitle"))
                        .replace("%distance%", String.valueOf(player.getLocation().getBlockY() - lavaheight))
                        .replace("%level%", String.valueOf(lavaheight)));
            }
        };
        task.runTaskTimer(GameMaster.plugin, 0L, 2L);
        ActionBarSender.sendUpdatingActionBar(player, buffer, 2);
        mainTimers.put(player, new Pair<>(buffer, task));
    }

    public static void overtimewarning() {
        Bukkit.broadcastMessage(config.getString("game.overtime.warning.message").replace("%threshold%", String.valueOf(config.getInt("game.overtime.threshold"))));
    }
}
