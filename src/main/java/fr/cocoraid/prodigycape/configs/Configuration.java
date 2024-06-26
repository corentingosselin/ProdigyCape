package fr.cocoraid.prodigycape.configs;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.database.DatabaseType;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;


public class Configuration {


    public static record DatabaseCredentials(String host, int port, String name, String user, String password) {}

    @Getter
    private String language = "en_EN";
    @Getter
    private String customCommand = "cape";
    private final String LANGUAGE = "language";
    @Getter
    private DatabaseType databaseType = DatabaseType.FILE;
    @Getter
    private DatabaseCredentials databaseCredentials;

    private final ProdigyCape instance;

    private static final String DB_CREDENTIALS = "database_credentials";
    private static final String DB_HOST = DB_CREDENTIALS + ".host";
    private static final String DB_PORT = DB_CREDENTIALS + ".port";
    private static final String DB_NAME = DB_CREDENTIALS + ".name";
    private static final String DB_USER = DB_CREDENTIALS + ".user";
    private static final String DB_PASSWORD = DB_CREDENTIALS + ".password";

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

            if (databaseType == DatabaseType.MYSQL) {

                String host = data.getString(DB_HOST);
                int port = data.getInt(DB_PORT);
                String name = data.getString(DB_NAME);
                String user = data.getString(DB_USER);
                String password = data.getString(DB_PASSWORD);

                if (host == null || port == 0 || name == null || user == null || password == null) {
                    instance.getLogger().warning("Missing database credentials, using default FILE database");
                    databaseType = DatabaseType.FILE;
                } else {
                    databaseCredentials = new DatabaseCredentials(host, port, name, user, password);
                }
            }


        } catch (IllegalArgumentException e) {
            instance.getLogger().warning("Invalid database type (FILE,MYSQL), using default FILE");
        }
    }

}
