package fr.cocoraid.prodigycape.listener;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.manager.CapeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private CapeManager capeManager = ProdigyCape.getInstance().getCapeManager();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

      /*  if (capeManager.hasCape(player)) {
            PlayerCape playerCape = capeManager.getCurrentCape(player);
            if (!player.getPassengers().isEmpty()) {
                player.getPassengers().forEach(player::removePassenger);
            }
            playerCape.getCapeDisplay().teleport(event.getTo());
            player.addPassenger(playerCape.getCapeDisplay());
        }*/
    }
}
