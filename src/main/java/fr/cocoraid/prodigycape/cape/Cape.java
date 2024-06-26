package fr.cocoraid.prodigycape.cape;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter //Can't use @ all args constructor here 'cause numberSold has to be excluded ... :/
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
        this.key = key;
        this.enabled = enabled;
        this.texture = texture;
        this.name = name;
        this.description = description;
        this.price = price;
        this.limitedEdition = limitedEdition;
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

}
