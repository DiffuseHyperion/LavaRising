package yjservers.tk.lavarising;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import static yjservers.tk.lavarising.LavaRising.config;
import static yjservers.tk.lavarising.LavaRising.gm;

public class start implements CommandExecutor {

    static boolean starting;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("There is not enough players...");
        } else {
            starting = true;
            sender.sendMessage("Start command received!");
            if (config.getBoolean("pregame.start.countdown")) {
                gm.GamePlayer.timer(5, config.getString("pregame.start.timername"), BarColor.GREEN, BarStyle.SOLID, new BukkitRunnable() {
                    @Override
                    public void run() {
                        grace.gracesetup();
                    }
                });
            } else {
                grace.gracesetup();
            }
        }
        return true;
    }
}
