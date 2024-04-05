package fr.cocoraid.prodigycape.configs;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.database.DatabaseType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Configuration {

    private String language = "en_EN";
    private String customCommand = "cape";
    private final String LANGUAGE = "language";
    private DatabaseType databaseType = DatabaseType.FILE;
    private ProdigyCape instance;

    public Configuration(ProdigyCape instance) {
        this.instance = instance;
    }

    public File init() {
        //load config file if not present
        File file = new File(instance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        return file;
    }

    public void load() {
        File file = init();
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        this.customCommand = data.getString("custom_command", "cape");
        this.language = data.getString(LANGUAGE, "en_EN");
        try {
            this.databaseType = DatabaseType.valueOf(data.getString("storage_mode", "FILE").toUpperCase());
        } catch (IllegalArgumentException e) {
            instance.getLogger().warning("Invalid database type (FILE,MYSQL), using default FILE");
        }
    }

    public String getCustomCommand() {
        return customCommand;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public String getLanguage() {
        return language;
    }
}
