package net.azisaba.digformoney;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MoneyConfig {

    private final String fileName = "EarnBlocks.yml";
    private final HashMap<Material, Double> moneyMap = new HashMap<>();

    public void load() {

        File file = new File(DigForMoney.getPlugin().getDataFolder(), fileName);

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        if ( !file.exists() ) {
            conf.set(Material.STONE.name(), 30);
            conf.set(Material.DIRT.name(), 10);

            try {
                conf.save(file);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

            moneyMap.put(Material.STONE, 30d);
            moneyMap.put(Material.DIRT, 10d);
            return;
        }

        if ( conf.getConfigurationSection("") == null || conf.getConfigurationSection("").getKeys(false) == null ) {
            return;
        }

        for ( String key : conf.getConfigurationSection("").getKeys(false) ) {
            Material mat = null;
            try {
                mat = Material.getMaterial(key.toUpperCase());
            } catch ( Exception e ) {
                DigForMoney.getPlugin().getLogger().info("\'" + fileName + "\' の読み込みに失敗しました。 (Key=" + key + ")");
                continue;
            }

            double value = conf.getDouble(key);

            moneyMap.put(mat, value);
        }
    }

    public double getValue(Material type) {
        return moneyMap.getOrDefault(type, -1d);
    }
}
