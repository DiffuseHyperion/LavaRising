package yjservers.tk.lavarising;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;

import static yjservers.tk.lavarising.LavaRising.*;
import static yjservers.tk.lavarising.start.starting;

public class reroll implements CommandExecutor, Listener {

    int agreedplayers = 0;
    int requiredplayers;
    ArrayList<Player> agreedlist = new ArrayList<>();
    static boolean allowedtoreroll = false;
    static boolean someonejoinedbefore = false;
    static BossBar rerollbossbar;

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
        if (!(someonejoinedbefore)) {
            someonejoinedbefore = true;
            String bossbartitle = config.getString("pregame.rerolling.beforemessage");
            assert false;
            rerollbossbar = Bukkit.createBossBar(bossbartitle, BarColor.YELLOW, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
            double[] timer = {15};
            BukkitRunnable bossbartimer = new BukkitRunnable() {
                @Override
                public void run() {
                    rerollbossbar.setTitle(bossbartitle.replace("%timer%", String.valueOf(timer[0])));
                    rerollbossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(15), 5, RoundingMode.HALF_EVEN).doubleValue());
                    if (!(timer[0] <= 0)) {
                        timer[0] = BigDecimal.valueOf(timer[0]).subtract(BigDecimal.valueOf(0.1)).doubleValue();
                    }
                    if (timer[0] <= 0) {
                        requiredplayers = BigDecimal.valueOf(config.getInt("pregame.rerolling.percentagetopass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
                        if (requiredplayers == 0) {
                            requiredplayers = 1;
                        }
                        allowedtoreroll = true;
                        rerollbossbar.setTitle(config.getString("pregame.rerolling.enabledmessage").replace("%required%", String.valueOf(requiredplayers)));
                        rerollbossbar.setProgress(1);
                        if (starting) {
                            rerollbossbar.removeAll();
                            this.cancel();
                        }
                    }
                }
            };
            bossbartimer.runTaskTimer(plugin, 0, 2);
            rerollbossbar.addPlayer(event.getPlayer());
        } else if (Objects.equals(state, "pregame")) {
            assert false;
            rerollbossbar.addPlayer(event.getPlayer());
        }
    }
}
