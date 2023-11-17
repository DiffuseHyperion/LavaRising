package me.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static me.diffusehyperion.lavarising.LavaRising.*;


public class grace {

    public static void triggerGrace(){
        state = States.GRACE;
        GamePlayer.playSoundToAll(Sound.ENTITY_ENDER_DRAGON_AMBIENT);
        for (Player p: Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
        }
        WorldBorder border = world.getWorldBorder();
        border.setSize(config.getDouble("game.grace.finalBorderSize"), config.getLong("game.grace.speed"));
        border.setWarningDistance(0);
        BossBar bossbar = GamePlayer.timer(config.getInt("game.grace.duration"), config.getString("timers.grace.name"),
                BarColor.valueOf(config.getString("timers.grace.colour")),
                BarStyle.valueOf(config.getString("timers.grace.style")),
                new BukkitRunnable() {
            @Override
            public void run() {
                main.triggerMain();
            }
        }).getValue0();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
    }
}
