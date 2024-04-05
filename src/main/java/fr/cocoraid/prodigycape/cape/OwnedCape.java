package fr.cocoraid.prodigycape.cape;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class OwnedCape implements ConfigurationSerializable {

    private String key;
    private double boughtPrice;
    private long boughtTime;
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

    public String getKey() {
        return key;
    }

    public void setBoughtPrice(double boughtPrice) {
        this.boughtPrice = boughtPrice;
    }

    public void setBoughtTime(long boughtTime) {
        this.boughtTime = boughtTime;
    }

    public void setEditionNumber(int editionNumber) {
        this.editionNumber = editionNumber;
    }

    public double getBoughtPrice() {
        return boughtPrice;
    }

    public int getEditionNumber() {
        return editionNumber;
    }

    public long getBoughtTime() {
        return boughtTime;
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
