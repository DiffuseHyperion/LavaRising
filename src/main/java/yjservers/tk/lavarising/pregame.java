package yjservers.tk.lavarising;

import org.apache.commons.io.FileUtils;
import org.bukkit.*;

import java.io.File;
import java.util.Objects;

import static yjservers.tk.lavarising.LavaRising.config;
import static yjservers.tk.lavarising.LavaRising.world;
import static yjservers.tk.lavarising.LavaRising.waitforworld;

public class pregame {

    public void deleteWorld() {
        String worldname = config.getString("pregame.worldname");
        assert worldname != null;
        for (; true; ) {
            Bukkit.unloadWorld(worldname, false);
            File oldworld = new File(Bukkit.getWorldContainer().getAbsolutePath() + "/" + worldname);
            FileUtils.deleteQuietly(oldworld);
            if (!oldworld.exists()) {
                waitforworld.countDown();
                break;
            }
        }
    }

    public void createWorld() {
        String worldname = config.getString("pregame.worldname");
        WorldCreator worldcreator = new WorldCreator(worldname);
        world = Bukkit.createWorld(worldcreator);
    }

    public void setupworld() {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(config.getDouble("pregame.bordersize"));
        for (int i = 255; true; i = i - 1) {
            if (!Objects.equals(world.getBlockAt(0, i, 0).getType(),Material.AIR)) {
                if (Objects.equals(world.getBlockAt(0, i, 0).getType(), Material.WATER)) {
                    core.fillBlocks(new Location(world, -1, i, -1), new Location(world, 1, i, 1), Material.DIRT);
                }
                world.setSpawnLocation(0, i + 1, 0);
                break;
            }
        }
        world.setGameRule(GameRule.SPAWN_RADIUS, 1);
        world.getWorldBorder().setWarningTime(0);
        world.getWorldBorder().setWarningDistance(0);
        world.setPVP(false);
    }

}
