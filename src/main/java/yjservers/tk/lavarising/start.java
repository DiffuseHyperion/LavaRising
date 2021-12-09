package yjservers.tk.lavarising;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class start implements CommandExecutor {

    static boolean starting;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("There is not enough players...");
        } else {
            starting = true;
            sender.sendMessage("Start command received!");
            core.timer(5, "Starting in %timer% seconds!", BarColor.GREEN, BarStyle.SOLID);
        }
        return true;
    }
}
