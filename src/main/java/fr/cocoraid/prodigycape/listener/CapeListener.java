package fr.cocoraid.prodigycape.listener;

import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.cape.PlayerCape;
import fr.cocoraid.prodigycape.ProdigyCape;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class CapeListener implements Listener {

    private final CapeManager capeManager = ProdigyCape.getInstance().getCapeManager();

    @EventHandler
    public void sneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();

        if (!capeManager.hasCape(player)) {
            return;
        }
        PlayerCape playerCape = capeManager.getCurrentCape(player);
        if (playerCape.getCapeDisplay() == null) return;
        playerCape.onSneakEvent(e.isSneaking());
    }

}
