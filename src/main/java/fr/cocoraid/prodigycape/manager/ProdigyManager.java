package fr.cocoraid.prodigycape.manager;

import fr.cocoraid.prodigycape.ProdigyPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProdigyManager {

    private final Map<UUID, ProdigyPlayer> prodigyPlayers = new HashMap<>();

    public void loadPlayer(UUID uuid) {
        if(prodigyPlayers.containsKey(uuid)) {
            return;
        }
        ProdigyPlayer player = new ProdigyPlayer(uuid);
        prodigyPlayers.put(uuid, player);
    }

    public ProdigyPlayer getProdigyPlayerOrCreate(UUID uuid) {
        if(!prodigyPlayers.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return prodigyPlayers.get(uuid);
    }

    public ProdigyPlayer getProdigyPlayer(UUID uuid) {
        return prodigyPlayers.get(uuid);
    }


    public Map<UUID, ProdigyPlayer> getProdigyPlayers() {
        return prodigyPlayers;
    }
}
