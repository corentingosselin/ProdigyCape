package fr.cocoraid.prodigycape.database;

import java.util.UUID;

public interface Database {


    public void initialize();
    public void close();

    public void loadPlayer(UUID uuid);
    public void savePlayer(UUID uuid);

    public void saveCape(String key);

    public void reload();





}
