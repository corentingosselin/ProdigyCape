package fr.cocoraid.prodigycape;

import org.bukkit.entity.Player;

public interface NmsHandler {

    Object clientInfoWithoutCape(Object object);

    void removeCape(Player player);

}
