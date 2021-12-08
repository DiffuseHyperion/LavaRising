package yjservers.tk.lavarising;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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

public class reroll implements CommandExecutor, Listener {

    int agreedplayers = 0;
    int requiredplayers;
    ArrayList<Player> agreedlist = new ArrayList<>();
    static int allowedtoreroll = 0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        requiredplayers = BigDecimal.valueOf(config.getInt("pregame.rerolling.percentagetopass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
        if (requiredplayers == 0) {
           requiredplayers = 1;
        }
        if (Objects.equals(state, "pregame") && sender instanceof Player && config.getBoolean("pregame.rerolling.enabled") && !agreedlist.contains(sender) && allowedtoreroll == 1) {
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
        } else if (allowedtoreroll == 0) {
            sender.sendMessage("The world is (probably) still generating. Please wait for a bit before trying to reroll again!");
        } else {
            sender.sendMessage("You already voted to reroll the world!");
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (Bukkit.getOnlinePlayers().size() == 1 && config.getBoolean("pregame.rerolling.enabled") && allowedtoreroll == 0) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    requiredplayers = BigDecimal.valueOf(config.getInt("pregame.rerolling.percentagetopass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
                    if (requiredplayers == 0) {
                        requiredplayers = 1;
                    }
                   allowedtoreroll = 1;
                   Bukkit.broadcastMessage(ChatColor.YELLOW + "Rerolling of the world is now enabled! Do /reroll to vote to reroll. " + requiredplayers + " players needs to vote to reroll.");
                }
            };
            task.runTaskLater(plugin, 300);
        }
    }
}
