package fr.cocoraid;

import org.bukkit.entity.Player;

public interface NmsHandler {

    Object clientInfoWithoutCape(Object object);

    void removeCape(Player player);
}
