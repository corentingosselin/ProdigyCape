package fr.cocoraid.prodigycape.listener;


import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.cape.PlayerCape;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.manager.ProdigyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PacketEventsListener extends SimplePacketListenerAbstract {

    private final CapeManager capeManager;
    private final ProdigyManager prodigyManager;
    public PacketEventsListener(ProdigyCape instance) {
        super(PacketListenerPriority.HIGH);
        this.capeManager = instance.getCapeManager();
        this.prodigyManager = instance.getProdigyManager();
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_SETTINGS -> {
                WrapperPlayClientSettings packet = new WrapperPlayClientSettings(event);
                packet.setVisibleSkinSectionMask((byte) 126);
            }
        }
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        Player player = (Player) event.getPlayer();
        if(player == null) return;
        switch (event.getPacketType()) {
            case SPAWN_PLAYER, SPAWN_ENTITY -> {
                WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(event);
                UUID uuid = packet.getUUID();
                Player target = Bukkit.getPlayer(uuid);
                if (target == null) return;
                PlayerCape playerCape = capeManager.getCurrentCape(target);
                if (playerCape == null) return;
                playerCape.spawnForPlayer(player, target);
            }

            case DESTROY_ENTITIES -> {
                User user = event.getUser();
                if (user == null) return;
                WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);
                int[] entities = packet.getEntityIds();
                List<Integer> list =  Arrays.stream(entities).boxed().collect(Collectors.toList());
                prodigyManager.getProdigyPlayers().keySet().forEach(uuid -> {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target == null) return;
                    PlayerCape playerCape = capeManager.getCurrentCape(target);
                    if (playerCape == null) return;
                    int playerId = user.getEntityId();
                    if (list.contains(playerId)) {
                        playerCape.despawnForPlayer(player);
                    }
                });

            }

            case SET_PASSENGERS -> {
                WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);

                int[] entities =  packet.getPassengers();
                PlayerCape playerCape = capeManager.getCurrentCape(player);
                if (playerCape == null) return;

                if (entities.length == 0) {
                    packet.setPassengers(new int[]{playerCape.getCapeDisplay().getId()});
                    return;
                }

                int capeId = playerCape.getCapeDisplay().getId();
                if (entities.length == 1) {
                    int potentialCapeId = entities[0];
                    if (potentialCapeId == capeId) return;
                }


                int[] newEntities = new int[entities.length + 1];
                // make the capeID the first entity
                newEntities[0] = capeId;
                for (int i = 0; i < entities.length; i++) {
                    newEntities[i + 1] = entities[i];
                }
                packet.setPassengers(newEntities);
            }
        }

    }
}