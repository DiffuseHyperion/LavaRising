package tk.diffusehyperion.lavarising.Commands;

import me.tigerhix.BossbarLib.Bossbar;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.javatuples.Pair;
import tk.diffusehyperion.gamemaster.GamePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.Commands.start.starting;

public class reroll implements CommandExecutor, Listener {

    public static int agreedplayers = 0;
    public static ArrayList<Player> agreedlist = new ArrayList<>();
    public static boolean allowedtoreroll = false;
    public static boolean someonejoinedbefore = false;

    public static HashMap<Player, Pair<Bossbar, BukkitRunnable>> rerollEnablingBossbars = new HashMap<>();
    public static HashMap<Player, Bossbar> rerollEnabledBossbars = new HashMap<>();

    public static int[] countdown;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int requiredplayers = getRequiredplayers();
        if (Objects.equals(state, "pregame") && sender instanceof Player && config.getBoolean("pregame.rerolling.enabled") && !agreedlist.contains(sender) && allowedtoreroll && !starting) {
            agreedlist.add((Player) sender);
            agreedplayers++;
            if (agreedplayers < requiredplayers) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + sender.getName() + " wants to reroll the map! " + agreedplayers + "/" + requiredplayers + " players to reroll.");
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(Objects.requireNonNull(config.getString("pregame.rerolling.kickmessage")).replace("%sender%", sender.getName()));
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
            }
        } else if (!Objects.equals(state, "pregame")) {
            sender.sendMessage("You can only reroll the map before the game starts!");
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("You can only run this command as a player!");
        } else if (!(config.getBoolean("pregame.rerolling.enabled"))) {
            sender.sendMessage("This command was disabled by the operator!");
        } else if (!allowedtoreroll) {
            sender.sendMessage("The world is (probably) still generating. Please wait for a bit before trying to reroll again!");
        } else if (starting) {
            sender.sendMessage("The game is already starting! The world cannot be rerolled now.");
        } else {
            sender.sendMessage("You already voted to reroll the world!");
        }
        return true;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (Objects.equals(state, "pregame")) {
            Player p = e.getPlayer();
            if (allowedtoreroll) {
                rerollEnabledBossbars.remove(p);
                createAfterRerollBossbar(p);
            } else {
                rerollEnablingBossbars.remove(p);
                createBeforeRerollBossbar(p);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!someonejoinedbefore) {
            someonejoinedbefore = true;
            int rerollDelay = config.getInt("pregame.rerolling.rerolltimer");
            BukkitRunnable rerollEnableTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        createAfterRerollBossbar(p);
                    }
                    allowedtoreroll = true;
                }
            };

            countdown = new int[]{rerollDelay};
            BukkitRunnable decrementCountdown = new BukkitRunnable() {
                @Override
                public void run() {
                    if (countdown[0] == 0) {
                        rerollEnableTask.runTask(plugin);
                        this.cancel();
                    }
                    countdown[0]--;
                }
            };
            decrementCountdown.runTaskTimer(plugin, 20, 20);
            for (Player p : Bukkit.getOnlinePlayers()) {
                createBeforeRerollBossbar(p);
            }

        } else if (Objects.equals(state, "pregame")) {
            Player p = event.getPlayer();
            if (allowedtoreroll) {
                int requiredPlayers = getRequiredplayers();

                // update messages
                for (Bossbar bossbar : rerollEnabledBossbars.values()) {
                    bossbar.setMessage(Objects.requireNonNull(config.getString("pregame.rerolling.enabledmessage")).replace("%required%", String.valueOf(requiredPlayers)));
                }

                createAfterRerollBossbar(p);
            } else {
                createBeforeRerollBossbar(p);
            }
        }
    }

    private int getRequiredplayers() {
        int requiredplayers = BigDecimal.valueOf(config.getInt("pregame.rerolling.percentagetopass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
        if (requiredplayers == 0) {
            requiredplayers = 1;
        }
        return requiredplayers;
    }

    private void createAfterRerollBossbar(Player p) {
        int requiredPlayers = getRequiredplayers();

        Bossbar bossbar = barLib.getBossbar(p);
        bossbar.setPercentage(1f);
        bossbar.setMessage(Objects.requireNonNull(config.getString("pregame.rerolling.enabledmessage")).replace("%required%", String.valueOf(requiredPlayers)));
        rerollEnabledBossbars.put(p, bossbar);
    }

    private void createBeforeRerollBossbar(Player p) {
        final Bossbar bossbar = barLib.getBossbar(p);
        final String title = config.getString("pregame.rerolling.beforemessage");

        // base delay before reroll
        int duration = config.getInt("pregame.rerolling.rerolltimer");

        // set percentage based on the already elapsed time
        bossbar.setPercentage(BigDecimal.valueOf(countdown[0]).divide(BigDecimal.valueOf(duration), 5, RoundingMode.HALF_EVEN).floatValue());

        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                bossbar.setPercentage(BigDecimal.valueOf(countdown[0]).divide(BigDecimal.valueOf(duration), 5, RoundingMode.HALF_EVEN).floatValue());
                bossbar.setMessage(bossbarReplaceTitle(title, (double) countdown[0], (double) duration - countdown[0]));
                if (countdown[0] <= 0.0) {
                    barLib.clearBossbar(p);
                    this.cancel();
                }

            }
        };
        task.runTaskTimer(plugin, 0L, 20L);

        rerollEnablingBossbars.put(p, new Pair<>(bossbar, task));
    }

    private String bossbarReplaceTitle(String title, Double timeLeft, Double timeElapsed) {
        String replacementTitle = title.replace(GamePlayer.timerReplacement.TIME_LEFT.toString(), String.valueOf(timeLeft));
        replacementTitle = replacementTitle.replace(GamePlayer.timerReplacement.TIME_ELAPSED.toString(), String.valueOf(timeElapsed));
        return replacementTitle;
    }
}
