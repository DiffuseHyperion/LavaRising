package tk.diffusehyperion.lavarising.Commands;

import org.bukkit.ChatColor;
import tk.diffusehyperion.gamemaster.GamePlayer;
import tk.diffusehyperion.lavarising.LavaRising;

import java.util.ArrayList;
import java.util.List;

public class TimerColourParser {
    public static GamePlayer.TimerColours getTimerColour(String config) {
        List<ChatColor> borderColour = new ArrayList<>();
        List<ChatColor> delimiterColour = new ArrayList<>();
        List<ChatColor> emptyColour = new ArrayList<>();
        List<ChatColor> filledColour = new ArrayList<>();
        ChatColor[] borderArray = {};
        ChatColor[] delimiterArray = {};
        ChatColor[] emptyArray = {};
        ChatColor[] filledArray = {};


        for (String s : LavaRising.config.getStringList(config + ".border")) {
            borderColour.add(ChatColor.valueOf(s));
        }
        for (String s : LavaRising.config.getStringList(config + ".delimiter")) {
            delimiterColour.add(ChatColor.valueOf(s));
        }
        for (String s : LavaRising.config.getStringList(config + ".empty")) {
            emptyColour.add(ChatColor.valueOf(s));
        }
        for (String s : LavaRising.config.getStringList(config + ".filled")) {
            filledColour.add(ChatColor.valueOf(s));
        }

        return new GamePlayer.TimerColours(borderColour.toArray(borderArray), delimiterColour.toArray(delimiterArray), emptyColour.toArray(emptyArray), filledColour.toArray(filledArray));
    }
}
