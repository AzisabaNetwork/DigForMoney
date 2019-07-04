package net.azisaba.digformoney;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NormalConfig {

    private final String fileName = "config.yml";
    private List<String> enableWorlds = new ArrayList<>();

    public void load() {

        File file = new File(DigForMoney.getPlugin().getDataFolder(), fileName);

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        if ( !file.exists() ) {
            conf.set("EnableWorlds", Arrays.asList("Example1", "Example2", "Example3"));

            try {
                conf.save(file);
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            return;
        }

        if ( conf.getConfigurationSection("") == null || conf.getConfigurationSection("").getKeys(false) == null ) {
            return;
        }

        enableWorlds = conf.getStringList("EnableWorlds");
    }

    public boolean isEnabledWorld(World world) {
        return enableWorlds.contains(world.getName());
    }
}
