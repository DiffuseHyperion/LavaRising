package tk.diffusehyperion.lavarising.Commands;

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
import tk.diffusehyperion.gamemaster.ActionBars.ActionBarSender;
import tk.diffusehyperion.gamemaster.Util.CompletableStringBuffer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;

import static tk.diffusehyperion.lavarising.Commands.start.starting;
import static tk.diffusehyperion.lavarising.LavaRising.*;
import static tk.diffusehyperion.lavarising.States.States.PREGAME;

public class reroll implements CommandExecutor, Listener {

    public static int agreedplayers = 0;
    public static ArrayList<Player> agreedlist = new ArrayList<>();
    public static boolean allowedtoreroll = false;
    public static boolean someonejoinedbefore = false;

    public static CompletableStringBuffer beforeRerollBuffer;
    public static CompletableStringBuffer afterRerollBuffer;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int requiredplayers = getRequiredplayers();
        if (state == PREGAME && sender instanceof Player && config.getBoolean("pregame.rerolling.enabled") && !agreedlist.contains(sender) && allowedtoreroll && !starting) {
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
        } else if (!(state == PREGAME)) {
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
            int rerollDelay = config.getInt("pregame.rerolling.rerolltimer");

            beforeRerollBuffer = gm.GamePlayer.timer(rerollDelay,
                    config.getString("pregame.rerolling.beforemessage"),
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            allowedtoreroll = true;
                            int requiredPlayers = getRequiredplayers();

                            afterRerollBuffer = new CompletableStringBuffer();
                            afterRerollBuffer.stringBuffer.append(Objects.requireNonNull(config.getString("pregame.rerolling.enabledmessage"))
                                    .replace("%required%", String.valueOf(requiredPlayers)));

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                ActionBarSender.sendUpdatingActionBar(p, afterRerollBuffer, 2);
                            }
                        }
                    }, null, null, null).getValue0();

            for (Player p : Bukkit.getOnlinePlayers()) {
                ActionBarSender.sendUpdatingActionBar(p, beforeRerollBuffer, 2);
            }

        } else if (state == PREGAME) {
            Player p = event.getPlayer();
            if (allowedtoreroll) {
                int requiredPlayers = getRequiredplayers();

                afterRerollBuffer.stringBuffer.delete(0, afterRerollBuffer.stringBuffer.length());
                afterRerollBuffer.stringBuffer.append(Objects.requireNonNull(config.getString("pregame.rerolling.enabledmessage"))
                        .replace("%required%", String.valueOf(requiredPlayers)));

                ActionBarSender.sendUpdatingActionBar(p, afterRerollBuffer, 2);
            } else {
                ActionBarSender.sendUpdatingActionBar(p, beforeRerollBuffer, 2);
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
}
