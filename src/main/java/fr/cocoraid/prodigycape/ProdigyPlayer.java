package fr.cocoraid.prodigycape;

import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.cape.OwnedCape;
import fr.cocoraid.prodigycape.cape.PlayerCape;
import fr.cocoraid.prodigycape.manager.CapeManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;


@Getter
@RequiredArgsConstructor
public class ProdigyPlayer implements ConfigurationSerializable {

    private static final CapeManager capeManager = ProdigyCape.getInstance().getCapeManager();

    @Setter
    private boolean hasEdition = false;

    private final UUID uuid;
    private PlayerCape cape;
    private final Set<OwnedCape> ownedCapes = new HashSet<>();


    public void setCape(PlayerCape cape) {
        this.cape = cape;
        this.hasEdition = true;
    }

    public void setCapeWithoutEdition(PlayerCape cape) {
        this.cape = cape;
    }

    public boolean hasCape() {
        return cape != null;
    }

    public boolean hasEdition() {
        return hasEdition;
    }

    public void addOwnedCape(OwnedCape cape) {
        ownedCapes.add(cape);
        hasEdition = true;
    }

    public void removeOwnedCape(OwnedCape cape) {
        ownedCapes.remove(cape);
        hasEdition = true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", uuid.toString());
        if (cape != null) {
            data.put("cape", cape.getCape().getKey());
        } else {
            data.put("cape", null);
        }
        List<Map<String, Object>> ownedCapes = new ArrayList<>();
        for (OwnedCape ownedCape : this.ownedCapes) {
            ownedCapes.add(ownedCape.serialize());
        }
        data.put("ownedCapes", ownedCapes);
        return data;
    }


    public static ProdigyPlayer deserialize(UUID uuid, Map<String, Object> data) {
        ProdigyPlayer pp = new ProdigyPlayer(uuid);

        List<Map<String, Object>> ownedCapes = (List<Map<String, Object>>) data.get("ownedCapes");
        if (ownedCapes == null) {
            ownedCapes = new ArrayList<>();
        }
        for (Map<String, Object> ownedCape : ownedCapes) {
            pp.addOwnedCape(OwnedCape.deserialize(ownedCape));
        }

        String capeKey = (String) data.get("cape");
        if (capeKey != null) {
            Cape cape = capeManager.getCape(capeKey);
            if (cape == null) {
                //this means the cape is not available anymore
                pp.setCape(null);
                //we remove the cape from the player owned capes
                pp.ownedCapes.removeIf(ownedCape -> ownedCape.getKey().equals(capeKey));
            } else {
                pp.setCape(new PlayerCape(cape));
            }
        }

        return pp;
    }

    @Override
    public String toString() {
        return "ProdigyPlayer{" +
                "hasEdition=" + hasEdition +
                ", uuid=" + uuid +
                ", cape=" + cape +
                ", ownedCapes=" + ownedCapes +
                '}';
    }

}
