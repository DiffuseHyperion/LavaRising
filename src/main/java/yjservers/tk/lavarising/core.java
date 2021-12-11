package yjservers.tk.lavarising;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static yjservers.tk.lavarising.LavaRising.state;
import static yjservers.tk.lavarising.LavaRising.config;
import static yjservers.tk.lavarising.LavaRising.plugin;
import static yjservers.tk.lavarising.main.winner;

public class core {

    static boolean requiredtorestart = false;

    public static void timer (int duration, String name, BarColor colour, BarStyle style) {
        BossBar bossbar = Bukkit.createBossBar(name, colour, style, BarFlag.PLAY_BOSS_MUSIC);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        double[] timer = {duration};
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                timer[0] = BigDecimal.valueOf(timer[0]).subtract(BigDecimal.valueOf(0.1)).doubleValue();
                if (timer[0] <= 0) {
                    bossbar.removeAll();
                    switch (state) {
                        case "pregame" -> grace.gracesetup();
                        case "grace" -> main.mainsetup();
                        case "post" -> {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.kickPlayer(config.getString("post.kickmessage"));
                            }
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                        }
                    }
                    this.cancel();
                }
                if (Objects.equals(state, "post")) {
                    bossbar.setTitle(name.replace("%winner%", winner.getName()).replace("%timer%", String.valueOf(timer[0])));
                } else {
                    bossbar.setTitle(name.replace("%timer%", String.valueOf(timer[0])));
                }
                bossbar.setProgress(BigDecimal.valueOf(timer[0]).divide(BigDecimal.valueOf(duration), 5, RoundingMode.HALF_EVEN).doubleValue());

                }
            };
        task.runTaskTimer(plugin, 0, 2);
    }

    public static void fillBlocks(Location loc1, Location loc2, Material blocktype) {
        if (loc1.getWorld() != loc2.getWorld()) {
            Bukkit.getLogger().severe("bruh loc1 and loc2 worlds not the same");
        }
        World world = loc1.getWorld();
        int X1 = loc1.getBlockX();
        int Y1 = loc1.getBlockY();
        int Z1 = loc1.getBlockZ();
        int X2 = loc2.getBlockX();
        int Y2 = loc2.getBlockY();
        int Z2 = loc2.getBlockZ();
        int startX;
        int endX;
        int startY;
        int endY;
        int startZ;
        int endZ;
        if (X1 > X2) {
            startX = X2;
            endX = X1;
        } else {
            startX = X1;
            endX = X2;
        }
        if (Y1 > Y2) {
            startY = Y2;
            endY = Y1;
        } else {
            startY = Y1;
            endY = Y2;
        }
        if (Z1 > Z2) {
            startZ = Z2;
            endZ = Z1;
        } else {
            startZ = Z1;
            endZ = Z2;
        }
        for (int y = startY; y <= endY; y++) {
            for (int z = startZ; z <= endZ; z++) {
                for (int x = startX; x <= endX; x++) {
                    assert world != null;
                    world.getBlockAt(x, y, z).setType(blocktype);
                }
            }
        }
    }

    public static void playSound(Sound sound) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, Float.parseFloat("0.6"), 1);
        }
    }

    public static void editServerProperties(String propertyToCheck, String correctConfig, String oldContent, String newContent, String attemptMessage, String successMessage) {
        String checkedproperty = null;
        try {
            BufferedReader is = new BufferedReader(new FileReader("server.properties"));
            Properties props = new Properties();
            props.load(is);
            checkedproperty = props.getProperty(propertyToCheck);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Objects.equals(checkedproperty, correctConfig)) {
            Bukkit.getLogger().severe(attemptMessage);
            StringBuilder oldcontent = new StringBuilder();
            try {
                File serverpropfile = new File("server.properties");
                BufferedReader br = new BufferedReader(new FileReader(serverpropfile));
                String line = br.readLine();
                while (line != null)
                {
                    oldcontent.append(line).append(System.lineSeparator());
                    line = br.readLine();
                }
                String newcontent = oldcontent.toString().replaceAll(oldContent, newContent);
                FileWriter writer = new FileWriter(serverpropfile);
                writer.write(newcontent);
                br.close();
                writer.close();
                Bukkit.getLogger().severe(successMessage);
                requiredtorestart = true;
            } catch (IOException e) {
                Bukkit.getLogger().severe("Something went wrong while trying to disable " + propertyToCheck + "! Error log below: ");
                e.printStackTrace();
            }
        }
    }

    public static void editServerPropertiesYAML(File filetocheck, String propertyToCheck, Object correctConfig, String oldContent, String newContent, String attemptMessage, String successMessage) {
        try {
            FileReader fr = new FileReader(filetocheck);
            Yaml yaml = new Yaml();
            String fullyaml = yaml.load(fr).toString();
            Pattern pattern = Pattern.compile(propertyToCheck);
            Matcher matcher = pattern.matcher(fullyaml);
            String neededproperty = null;
            if (matcher.find()) {
                neededproperty = matcher.group(0);
            } else {
                Bukkit.getLogger().severe("Something went wrong while editing " + filetocheck.getName() + ", This is a plugin issue, please wait for a new update! Inform me in spigot fourms, when i check it lol");
            }
            assert neededproperty != null;
            String valueofproperty = neededproperty.substring(neededproperty.lastIndexOf("=") + 1);
            if (!Objects.equals(valueofproperty, correctConfig)) {
                Bukkit.getLogger().severe(attemptMessage);
                StringBuilder oldcontent = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(filetocheck));
                    String line = br.readLine();
                    while (line != null)
                    {
                        oldcontent.append(line).append(System.lineSeparator());
                        line = br.readLine();
                    }
                    String newcontent = oldcontent.toString().replaceAll(oldContent, newContent);
                    FileWriter writer = new FileWriter(filetocheck);
                    writer.write(newcontent);
                    br.close();
                    writer.close();
                    Bukkit.getLogger().severe(successMessage);
                    requiredtorestart = true;
                } catch (IOException e) {
                    Bukkit.getLogger().severe("Something went wrong while trying to disable " + propertyToCheck + "! Error log below: ");
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void restartForConfig() {
        if (requiredtorestart) {
            Bukkit.getLogger().severe("Restarting the server now for edits to take effect. This might take a while!");
            Bukkit.spigot().restart();
        }
    }
}
