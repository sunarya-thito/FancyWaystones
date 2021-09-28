package thito.fancywaystones;

import org.bukkit.configuration.file.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class Configuration {
    private String name;
    private File file;
    private YamlConfiguration configuration;
    private boolean loadDefaults;

    public Configuration(String fileName, boolean loadDefaults) {
        name = fileName;
        this.loadDefaults = loadDefaults;
        file = new File(FancyWaystones.getPlugin().getDataFolder(), fileName);
    }

    public boolean isLoadDefaults() {
        return loadDefaults;
    }

    public void saveDefault() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            FancyWaystones.getPlugin().saveResource(name, true);
        }
    }

    public void reload() {
        saveDefault();
        configuration = YamlConfiguration.loadConfiguration(file);
        if (loadDefaults) {
            try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(FancyWaystones.getPlugin().getResource(name)), StandardCharsets.UTF_8)) {
                configuration.setDefaults(YamlConfiguration.loadConfiguration(reader));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public YamlConfiguration getConfig() {
        return configuration;
    }
}
