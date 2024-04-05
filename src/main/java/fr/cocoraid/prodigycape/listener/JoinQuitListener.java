package fr.cocoraid.prodigycape.listener;


import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.database.DatabaseManager;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.utils.Reflection;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class JoinQuitListener implements Listener {

    private ProdigyCape instance;
    private CapeManager capeManager;
    private DatabaseManager databaseManager;

    public JoinQuitListener(ProdigyCape instance) {
        this.instance = instance;
        this.capeManager = instance.getCapeManager();
        this.databaseManager = instance.getDatabaseManager();
    }


    private static final Class<?> craftPlayerClass = Reflection.getCraftBukkitClass("entity.CraftPlayer");
    private static final Reflection.MethodInvoker getHandleMethod = Reflection.getMethod(craftPlayerClass, "getHandle");

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();


        ServerPlayer sp = (ServerPlayer) getHandleMethod.invoke(player);

        SynchedEntityData entityData = sp.getEntityData();
        entityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126);
        List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
        eData.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126));


        ClientboundSetEntityDataPacket meta = new ClientboundSetEntityDataPacket(sp.getId(), eData);
        sp.connection.send(meta);


        Cape contributorCape = capeManager.getCapeContributors().getCape(player.getUniqueId());
        if (contributorCape != null) {
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
