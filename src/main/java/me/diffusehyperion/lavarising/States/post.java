package me.diffusehyperion.lavarising.States;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.diffusehyperion.gamemaster.Utility.Pair;

import java.util.HashMap;
import java.util.Objects;

import static me.diffusehyperion.lavarising.LavaRising.*;
import static me.diffusehyperion.lavarising.States.main.lavaRiser;
import static me.diffusehyperion.lavarising.States.main.mainBossbars;
import static me.diffusehyperion.lavarising.States.overtime.*;

import me.diffusehyperion.gamemaster.Components.GamePlayer;
import me.diffusehyperion.gamemaster.Components.GameServer;

public class post {
    public static void triggerPost(Player winner){
        state = States.POST;
        GamePlayer.playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE);

        lavaRiser.cancel();
        if (overtimeTriggered) {
            overtimeBossbar.removeAll();
            overtimeTask.cancel();
        }
        for (Pair<BossBar, BukkitRunnable> pair : mainBossbars.values()) {
            pair.getValue0().removeAll();
            pair.getValue1().cancel();
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("%winner%", winner.getDisplayName());

        BossBar bossbar = GamePlayer.customTimer(config.getInt("game.post.duration"),
                config.getString("timers.post.name"),
                BarColor.valueOf(config.getString("timers.post.colour")),
                BarStyle.valueOf(config.getString("timers.post.style")),
                hashMap,
                new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(config.getString("game.post.kickMessage"));
                }
                GameServer.restart();
            }
        }).getValue0();
        GamePlayer.showBossbarToAll(bossbar);

        if (Objects.equals(config.getString("game.post.creativeMode"), "winner")) {
            winner.setGameMode(GameMode.CREATIVE);
        } else if (Objects.equals(config.getString("game.post.creativeMode"), "all")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.CREATIVE);
            }
        }

        for (String s : config.getStringList("game.post.commands")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%winner%", winner.getDisplayName()));
        }
    }
}
