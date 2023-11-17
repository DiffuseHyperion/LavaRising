package me.diffusehyperion.lavarising.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Objects;

import static me.diffusehyperion.lavarising.LavaRising.*;
import static me.diffusehyperion.lavarising.States.States.PREGAME;
import static me.diffusehyperion.lavarising.States.pregame.*;

import me.diffusehyperion.gamemaster.Components.GameServer;

public class reroll implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        requiredPlayers = getRequiredPlayers();
        if (state == PREGAME && sender instanceof Player && config.getBoolean("game.pregame.rerolling.enabled") && !agreedList.contains(sender) && allowedToReroll && !starting) {
            agreedList.add((Player) sender);
            agreedPlayers++;
            if (agreedPlayers < requiredPlayers) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + sender.getName() + " wants to reroll the map! " + agreedPlayers + "/" + requiredPlayers + " players to reroll.");
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(Objects.requireNonNull(config.getString("game.pregame.rerolling.kickMessage")).replace("%sender%", sender.getName()));
                }
                GameServer.restart();
            }
        } else if (!(state == PREGAME)) {
            sender.sendMessage("You can only reroll the map before the game starts!");
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("You can only run this command as a player!");
        } else if (!(config.getBoolean("game.pregame.rerolling.enabled"))) {
            sender.sendMessage("This command was disabled by the operator!");
        } else if (!allowedToReroll) {
            sender.sendMessage("The world is (probably) still generating. Please wait for a bit before trying to reroll again!");
        } else if (starting) {
            sender.sendMessage("The game is already starting! The world cannot be rerolled now.");
        } else {
            sender.sendMessage("You already voted to reroll the world!");
        }
        return true;
    }

}