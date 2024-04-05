package fr.cocoraid.prodigycape.cape;

public class Cape {

    private String key;
    private boolean enabled;
    private String texture;
    private String name;
    private String description;
    private int price = 0;
    private int limitedEdition = 0;
    private int numberSold = 0;

    public Cape(String key, boolean enabled, String texture, String name, String description, int price, int limitedEdition) {
        this.enabled = enabled;
        this.key = key;
        this.texture = texture;
        this.name = name;
        this.description = description;
        this.price = price;
        this.limitedEdition = limitedEdition;
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTexture() {
        return texture;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getLimitedEdition() {
        return limitedEdition;
    }

    public boolean hasPrice() {
        return price > 0;
    }

    public boolean isLimitedEdition() {
        return limitedEdition > 0;
    }

    public boolean isSoldOut() {
        return isLimitedEdition() && numberSold >= limitedEdition;
    }

    public void incrementSold() {
        numberSold++;
    }

    public int getNumberSold() {
        return numberSold;
    }

    public void setNumberSold(int numberSold) {
        this.numberSold = numberSold;
    }
}
