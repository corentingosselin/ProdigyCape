package fr.cocoraid.prodigycape.database;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.ProdigyPlayer;
import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.manager.ProdigyManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileDatabase implements Database {

    private ProdigyCape instance;
    private ProdigyManager manager;
    private CapeManager capeManager;
    public FileDatabase(ProdigyCape instance) {
        this.instance = instance;
        this.manager = instance.getProdigyManager();
        this.capeManager = instance.getCapeManager();
    }

    public void initialize() {
        initPlayersFile();
        initCapeFile();

        Bukkit.getOnlinePlayers().forEach(p -> {
            loadPlayer(p.getUniqueId());
        });
    }

    private File initCapeFile() {
        File file = new File(instance.getDataFolder(), "capes.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                loadDefaultCapes(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadCapes(file);
        }
        return file;
    }

    private void loadCapes(File file) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            String texture = data.getString(key + ".texture");
            String name = data.getString(key + ".name");
            String description = data.getString(key + ".description");
            Integer price = data.getInt(key + ".price");
            Integer limitedEdition = data.getInt(key + ".limited_edition");
            boolean enabled = data.getBoolean(key + ".enabled");
            Integer numberSold = data.getInt(key + ".number_sold");
            Cape cape = new Cape(key, enabled, texture, name, description, price, limitedEdition);
            cape.setNumberSold(numberSold);
            capeManager.registerCape(key, cape);
        }
    }

    private void loadDefaultCapes(File file) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        List<Cape> capes = capeManager.DEFAULT_CAPES;
        for (Cape cape : capes) {
            data.set(cape.getKey() + ".enabled", cape.isEnabled());
            data.set(cape.getKey() + ".texture", cape.getTexture());
            data.set(cape.getKey() + ".name", cape.getName());
            data.set(cape.getKey() + ".description", cape.getDescription());
            data.set(cape.getKey() + ".price", cape.getPrice());
            data.set(cape.getKey() + ".limited_edition", cape.getLimitedEdition());
            data.set(cape.getKey() + ".number_sold", cape.getNumberSold());
            capeManager.registerCape(cape.getKey(), cape);
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private File initPlayersFile() {
        File file = new File(instance.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public void close() {
        //save all prodigy players
        manager.getProdigyPlayers().keySet().forEach(this::savePlayer);
    }

    @Override
    public void loadPlayer(UUID uuid) {
        File file = initPlayersFile();
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        if (data.contains(uuid.toString())) {
            ProdigyPlayer pp = ProdigyPlayer
                    .deserialize(uuid, data.getConfigurationSection(uuid.toString()).getValues(true));
            manager.getProdigyPlayers().put(uuid, pp);
        }

    }

    public void savePlayer(UUID uuid) {
        ProdigyPlayer pp = manager.getProdigyPlayer(uuid);
        if (pp == null) {
            return;
        }

        if (!pp.hasEdition()) {
            return;
        }
        File file = initPlayersFile();
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> map = pp.serialize();
        data.set(uuid.toString(), map);
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveCape(String key) {
        Cape cape = capeManager.getCape(key);
        File file = initCapeFile();
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        data.set(key + ".enabled", cape.isEnabled());
        data.set(key + ".texture", cape.getTexture());
        data.set(key + ".name", cape.getName());
        data.set(key + ".description", cape.getDescription());
        data.set(key + ".price", cape.getPrice());
        data.set(key + ".limited_edition", cape.getLimitedEdition());
        data.set(key + ".number_sold", cape.getNumberSold());
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        initPlayersFile();
        initCapeFile();
    }
}
