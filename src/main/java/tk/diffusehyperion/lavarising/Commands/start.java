package tk.diffusehyperion.lavarising.Commands;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tk.diffusehyperion.lavarising.States.grace;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

import static tk.diffusehyperion.lavarising.Commands.reroll.*;
import static tk.diffusehyperion.lavarising.LavaRising.*;


public class start implements CommandExecutor, Listener {

    public static boolean starting;
    public static BossBar bossbar;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("There is not enough players...");
        } else {
            starting = true;
            sender.sendMessage("Start command received!");
            if (!Objects.isNull(rerollBossbar1)) {
                rerollBossbar1.removeAll();
                rerollTask1.cancel();
            }
            if (!Objects.isNull(rerollBossbar2)) {
                rerollBossbar2.removeAll();
            }
            if (config.getBoolean("pregame.start.countdown")) {
                bossbar = gm.GamePlayer.timer(5, config.getString("pregame.start.timername"), BarColor.GREEN, BarStyle.SOLID, new BukkitRunnable() {
                    @Override
                    public void run() {
                        grace.triggerGrace();
                    }
                }).getValue0();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    bossbar.addPlayer(p);
                }
            } else {
                grace.triggerGrace();
            }
        }
        return true;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        if (starting) {
            bossbar.addPlayer(e.getPlayer());
        }
    }

}
