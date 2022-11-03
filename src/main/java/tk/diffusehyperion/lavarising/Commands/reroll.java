package tk.diffusehyperion.lavarising.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.javatuples.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.Commands.start.starting;

public class reroll implements CommandExecutor, Listener {

    int agreedplayers = 0;
    int requiredplayers;
    ArrayList<Player> agreedlist = new ArrayList<>();
    static boolean allowedtoreroll = false;
    static boolean someonejoinedbefore = false;

    public static BossBar rerollBossbar1;
    public static BukkitRunnable rerollTask1;
    public static BossBar rerollBossbar2;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        requiredplayers = BigDecimal.valueOf(config.getInt("pregame.rerolling.percentagetopass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
        if (requiredplayers == 0) {
           requiredplayers = 1;
        }
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!someonejoinedbefore) {
            someonejoinedbefore = true;
            Pair<BossBar, BukkitRunnable> pair = gm.GamePlayer.timer(15, config.getString("pregame.rerolling.beforemessage"), BarColor.YELLOW, BarStyle.SOLID, new BukkitRunnable() {
                @Override
                public void run() {
                    requiredplayers = BigDecimal.valueOf(config.getInt("pregame.rerolling.percentagetopass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
                    if (requiredplayers == 0) {
                        requiredplayers = 1;
                    }

                    rerollBossbar2 = Bukkit.createBossBar(Objects.requireNonNull(config.getString("pregame.rerolling.enabledmessage")).replace("%required%", String.valueOf(requiredplayers)),
                            BarColor.YELLOW, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
                    rerollBossbar2.setProgress(1);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        rerollBossbar2.addPlayer(p);
                    }
                    allowedtoreroll = true;
                }
            });
            rerollBossbar1 = pair.getValue0();
            rerollTask1 = pair.getValue1();
            rerollBossbar1.addPlayer(event.getPlayer());
        } else if (Objects.equals(state, "pregame")) {
            Player p = event.getPlayer();
            if (allowedtoreroll) {
                rerollBossbar2.addPlayer(p);
            } else {
                rerollBossbar1.addPlayer(p);
            }
        }
    }
}