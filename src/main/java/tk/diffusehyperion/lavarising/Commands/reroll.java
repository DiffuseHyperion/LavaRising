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
import tk.diffusehyperion.gamemaster.GamePlayer;
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
        int requiredplayers = getRequiredPlayers();
        if (state == PREGAME && sender instanceof Player && config.getBoolean("game.pregame.rerolling.enabled") && !agreedlist.contains(sender) && allowedtoreroll && !starting) {
            agreedlist.add((Player) sender);
            agreedplayers++;
            if (agreedplayers < requiredplayers) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + sender.getName() + " wants to reroll the map! " + agreedplayers + "/" + requiredplayers + " players to reroll.");
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(Objects.requireNonNull(config.getString("game.pregame.rerolling.kickMessage")).replace("%sender%", sender.getName()));
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
            }
        } else if (!(state == PREGAME)) {
            sender.sendMessage("You can only reroll the map before the game starts!");
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("You can only run this command as a player!");
        } else if (!(config.getBoolean("game.pregame.rerolling.enabled"))) {
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
            int rerollDelay = config.getInt("game.pregame.rerolling.rerollTimer");

            beforeRerollBuffer = GamePlayer.timer(rerollDelay,
                    config.getString("timers.pregame.rerolling.name"),
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            allowedtoreroll = true;
                            int requiredPlayers = getRequiredPlayers();

                            afterRerollBuffer = new CompletableStringBuffer();
                            afterRerollBuffer.stringBuffer.append(Objects.requireNonNull(config.getString("game.pregame.rerolling.rerollingEnabled"))
                                    .replace("%required%", String.valueOf(requiredPlayers)));

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                ActionBarSender.sendUpdatingActionBar(p, afterRerollBuffer, 2);
                            }
                        }
                    }, null, 10, TimerColourParser.getTimerColour("timers.pregame.rerolling.style")).getValue0();

            for (Player p : Bukkit.getOnlinePlayers()) {
                ActionBarSender.sendUpdatingActionBar(p, beforeRerollBuffer, 2);
            }

        } else if (state == PREGAME) {
            Player p = event.getPlayer();
            if (allowedtoreroll) {
                int requiredPlayers = getRequiredPlayers();

                afterRerollBuffer.stringBuffer.delete(0, afterRerollBuffer.stringBuffer.length());
                afterRerollBuffer.stringBuffer.append(Objects.requireNonNull(config.getString("game.pregame.rerolling.rerollingEnabled"))
                        .replace("%required%", String.valueOf(requiredPlayers)));

                ActionBarSender.sendUpdatingActionBar(p, afterRerollBuffer, 2);
            } else {
                ActionBarSender.sendUpdatingActionBar(p, beforeRerollBuffer, 2);
            }
        }
    }

    private int getRequiredPlayers() {
        int requiredplayers = BigDecimal.valueOf(config.getInt("game.pregame.rerolling.percentageToPass")).multiply(BigDecimal.valueOf(Bukkit.getOnlinePlayers().size())).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
        if (requiredplayers == 0) {
            requiredplayers = 1;
        }
        return requiredplayers;
    }
}
