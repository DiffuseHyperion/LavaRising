package tk.diffusehyperion.lavarising.Commands;

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
import tk.diffusehyperion.lavarising.States.States;
import tk.diffusehyperion.lavarising.States.grace;

import static tk.diffusehyperion.lavarising.LavaRising.*;


public class start implements CommandExecutor, Listener {

    public static boolean starting;
    public static CompletableStringBuffer startingGraceTimer;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("There needs to be at least 2 players to start the game!");
        } else {
            starting = true;
            sender.sendMessage("Start command received!");
            reroll.beforeRerollBuffer.complete();
            reroll.afterRerollBuffer.complete();

            if (config.getBoolean("game.start.countdown")) {
                startingGraceTimer = GamePlayer.timer(config.getInt("game.start.timer"),
                        config.getString("timers.start.name"),
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                grace.triggerGrace();
                            }
                }, null, 10, TimerColourParser.getTimerColour("timers.start.name.style")).getValue0();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ActionBarSender.sendUpdatingActionBar(p, startingGraceTimer, 2);
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
            ActionBarSender.sendUpdatingActionBar(e.getPlayer(), startingGraceTimer, 2);
        }
        if (state == States.PREGAME) {
            e.getPlayer().teleport(world.getSpawnLocation());
            //spawnradius does not exist in 1.8
        }
    }
}
