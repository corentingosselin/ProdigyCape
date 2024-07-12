package fr.cocoraid.prodigycape.listener;


import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityMetadataProvider;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.database.DatabaseManager;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;


public class JoinQuitListener implements Listener {

    private ProdigyCape instance;
    private CapeManager capeManager;
    private DatabaseManager databaseManager;
    private PlayerManager playerManager;

    public JoinQuitListener(ProdigyCape instance) {
        this.instance = instance;
        this.capeManager = instance.getCapeManager();
        this.databaseManager = instance.getDatabaseManager();
        this.playerManager = instance.getPlayerManager();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                EntityData data = new EntityData(17, EntityDataTypes.BYTE, (byte) 126);
                WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(player.getEntityId(), List.of(data));
                Bukkit.getOnlinePlayers().forEach(cur -> {
                    playerManager.sendPacket(cur, metadata);
                });

            }
        }.runTaskLaterAsynchronously(instance, 10L);


        new BukkitRunnable() {
            @Override
            public void run() {
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
        }.runTaskLaterAsynchronously(instance, 10L);

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

    @EventHandler
    public void WorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                EntityData data = new EntityData(17, EntityDataTypes.BYTE, (byte) 126);
                WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(player.getEntityId(), List.of(data));
                Bukkit.getOnlinePlayers().forEach(cur -> {
                    playerManager.sendPacket(cur, metadata);
                });

            }
        }.runTaskLaterAsynchronously(instance, 10L);


    }

}
