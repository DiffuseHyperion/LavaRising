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
import tk.diffusehyperion.lavarising.States.States;
import tk.diffusehyperion.lavarising.States.States.grace;

import java.util.Objects;

import static tk.diffusehyperion.lavarising.LavaRising.*;


public class start implements CommandExecutor, Listener {

    public static boolean starting;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("There is not enough players...");
        } else {
            starting = true;
            sender.sendMessage("Start command received!");
            for (Player p : rerollEnablingBossbars.keySet()) {
                barLib.clearBossbar(p);
                rerollEnablingBossbars.get(p).getValue1().cancel();
            }
            for (Player p : rerollEnabledBossbars.keySet()) {
                barLib.clearBossbar(p);
            }
            if (config.getBoolean("pregame.start.countdown")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    createStartingBossbar(p);
                }
                BukkitRunnable startGrace = new BukkitRunnable() {
                    @Override
                    public void run() {
                        new grace().triggerGrace();
                    }
                };
                startGrace.runTaskLater(plugin, 5 * 20);
            } else {
                new grace().triggerGrace();
            }
        }
        return true;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        if (starting) {
            createStartingBossbar(e.getPlayer());
        }
        if (state == States.PREGAME) {
            e.getPlayer().teleport(world.getSpawnLocation());
            //spawnradius does not exist in 1.8
        }
    }

    private void createStartingBossbar(Player p) {
        gm.GamePlayer.timer(p, 5, config.getString("pregame.start.timername"));
    }

}
