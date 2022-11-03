package tk.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static tk.diffusehyperion.lavarising.LavaRising.*;


public class grace {

    static Plugin plugin;

    public grace() {
        plugin = (Plugin) this;
    }

    public static void gracesetup(){
        state = "grace";
        for (Player p: Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
        }
        WorldBorder border = world.getWorldBorder();
        border.setSize(config.getDouble("grace.finalbordersize"), config.getLong("grace.speed"));
        border.setWarningDistance(0);
        BossBar bossbar = gm.GamePlayer.timer(config.getInt("grace.duration"), config.getString("grace.timername"), BarColor.GREEN, BarStyle.SOLID, new BukkitRunnable() {
            @Override
            public void run() {
                main.mainsetup();
            }
        }).getValue0();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        gm.GamePlayer.playSoundToAll(Sound.ENTITY_ENDER_DRAGON_AMBIENT);
    }
}
