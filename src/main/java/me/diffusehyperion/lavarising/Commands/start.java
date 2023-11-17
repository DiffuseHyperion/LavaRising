package me.diffusehyperion.lavarising.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static me.diffusehyperion.lavarising.States.pregame.*;


public class start implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("There is not enough players...");
            return true;
        }

        if (starting) {
            sender.sendMessage("The game is already starting!");
            return true;
        }

        if (rerollingEnabled && !allowedToReroll) {
            sender.sendMessage("You need to wait for the reroll cooldown before starting!");
            return true;
        }

        sender.sendMessage("Start command received!");
        startGame();
        return true;
    }
}
