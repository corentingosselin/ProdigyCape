package fr.cocoraid.prodigycape.cape;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OwnedCape implements ConfigurationSerializable {

    private String key;
    @Setter
    private double boughtPrice;
    @Setter
    private long boughtTime;
    @Setter
    private int editionNumber;

    public OwnedCape(String key) {
        this.key = key;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("boughtPrice", boughtPrice);
        data.put("boughtTime", boughtTime);
        data.put("edition", editionNumber);
        return data;
    }

    public static OwnedCape deserialize(Map<String, Object> data) {
        OwnedCape cape = new OwnedCape((String) data.get("key"));
        cape.key = (String) data.get("key");
        cape.boughtPrice = (double) data.get("boughtPrice");
        cape.boughtTime = (long) data.get("boughtTime");
        cape.editionNumber = (int) data.get("edition");
        return cape;
    }

    @Override
    public String toString() {
        return "OwnedCape{" +
                "key='" + key + '\'' +
                ", boughtPrice=" + boughtPrice +
                ", boughtTime=" + boughtTime +
                ", editionNumber=" + editionNumber +
                '}';
    }
}
