package fr.cocoraid.prodigycape.listener;


import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.database.DatabaseManager;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class JoinQuitListener implements Listener {

    private ProdigyCape instance;
    private CapeManager capeManager;
    private DatabaseManager databaseManager;

    public JoinQuitListener(ProdigyCape instance) {
        this.instance = instance;
        this.capeManager = instance.getCapeManager();
        this.databaseManager = instance.getDatabaseManager();
    }



    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        instance.getNmsHandler().removeCape(player);

        Cape contributorCape = capeManager.getCapeContributors().getCape(player.getUniqueId());
        if (contributorCape != null && !capeManager.hasCape(player)) {
            capeManager.applyCape(player, contributorCape);
            return;
        }
        if (!capeManager.hasCape(player)) {
            return;
        }
        if (!capeManager.ownsCape(player, capeManager.getCurrentCape(player).getCape())) {
            return;
        }
        capeManager.getCurrentCape(player).spawn(player);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (capeManager.hasCape(player)) {
            capeManager.getCurrentCape(player).despawn();
        }
        databaseManager.getDatabase().savePlayer(player.getUniqueId());
    }

    @EventHandler
    public void asyncJoin(AsyncPlayerPreLoginEvent event) {
        databaseManager.getDatabase().loadPlayer(event.getUniqueId());
    }

}
