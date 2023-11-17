package me.diffusehyperion.lavarising.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static me.diffusehyperion.lavarising.LavaRising.state;

public class debugstate implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Current state: " + state);
        return true;
    }
}
