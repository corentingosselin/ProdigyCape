package fr.cocoraid.prodigycape.database;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.manager.CapeManager;

public class DatabaseManager {



    private Database database;

    public DatabaseManager(ProdigyCape instance, DatabaseType type) {
        switch (type) {
            case FILE:
                database = new FileDatabase(instance);
                break;

        }
    }

    public void initialize() {
        database.initialize();
    }

    public void close() {
        database.close();
    }

    public Database getDatabase() {
        return database;
    }

}
