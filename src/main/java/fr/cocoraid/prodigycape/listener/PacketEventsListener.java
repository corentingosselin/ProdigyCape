package fr.cocoraid.prodigycape.listener;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.cape.PlayerCape;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.manager.ProdigyManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PacketEventsListener extends SimplePacketListenerAbstract {

    private static CapeManager capeManager;
    private final ProdigyManager prodigyManager;

    public PacketEventsListener(ProdigyCape instance) {
        super(PacketListenerPriority.HIGH);
        capeManager = instance.getCapeManager();
        this.prodigyManager = instance.getProdigyManager();
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_SETTINGS -> handleClientSettings(event);
            case PLAYER_POSITION_AND_ROTATION -> handlePlayerPositionAndRotation(event);
            case PLAYER_POSITION -> handlePlayerPosition(event);
            case PLAYER_ROTATION -> handlePlayerRotation(event);
            case ANIMATION -> handlePlayerHandAnimation(event);
        }
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;
        switch (event.getPacketType()) {
            case SPAWN_PLAYER -> handleSpawnPlayer(event, player);
            case SPAWN_ENTITY -> handleSpawnEntity(event, player);
            case DESTROY_ENTITIES -> handleDestroyEntities(event, player);
        }
    }

    private void handleClientSettings(PacketPlayReceiveEvent event) {
        WrapperPlayClientSettings packet = new WrapperPlayClientSettings(event);
        packet.setVisibleSkinSectionMask((byte) 126);
        event.markForReEncode(true);
    }

    private void handlePlayerPositionAndRotation(PacketPlayReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerCape playerCape = getPlayerCape(player);
        if (playerCape == null) return;

        WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
        Location from = playerCape.getLastPosition();
        Location to = new Location(player.getWorld(), packet.getLocation().getX(), packet.getLocation().getY(), packet.getLocation().getZ());

        updatePlayerCape(player, playerCape, from, to, packet.getYaw());
    }

    private void handlePlayerPosition(PacketPlayReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;


        PlayerCape playerCape = getPlayerCape(player);
        if (playerCape == null) return;

        WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);

        //check if y only has changed
        com.github.retrooper.packetevents.protocol.world.Location packetLoc = packet.getLocation();
        if (playerCape.getLastPosition().getX() == packetLoc.getX()
                && playerCape.getLastPosition().getZ() == packetLoc.getZ()) {

            double y = player.getVelocity().getY();
            float calculatedSpeed = (float) -y;
            playerCape.setCurrentSpeed(calculatedSpeed);
            playerCape.update(playerCape.getCurrentBodyYaw());

            return;
        }

        Location from = playerCape.getLastPosition();
        Location to = new Location(player.getWorld(), packet.getLocation().getX(), packet.getLocation().getY(), packet.getLocation().getZ());

        updatePlayerCape(player, playerCape, from, to, null);
    }

    private void handlePlayerRotation(PacketPlayReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerCape playerCape = getPlayerCape(player);
        if (playerCape == null) return;

        WrapperPlayClientPlayerRotation packet = new WrapperPlayClientPlayerRotation(event);

        updatePlayerCapeRotation(player, playerCape, packet.getYaw());
    }

    private void handlePlayerHandAnimation(PacketPlayReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerCape playerCape = getPlayerCape(player);
        if (playerCape == null) return;

        updatePlayerCapeAttackAnimation(player, playerCape);
    }

    private void handleSpawnPlayer(PacketPlaySendEvent event, Player player) {
        WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(event);
        UUID uuid = packet.getUUID();
        Player target = Bukkit.getPlayer(uuid);
        if (target == null) return;

        PlayerCape playerCape = capeManager.getCurrentCape(target);
        if (playerCape == null) return;

        playerCape.spawnForPlayer(player, target);
    }

    private void handleSpawnEntity(PacketPlaySendEvent event, Player player) {
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);
        Optional<UUID> optionalUUID = packet.getUUID();
        if (optionalUUID.isEmpty()) return;

        Player target = Bukkit.getPlayer(optionalUUID.get());
        if (target == null) return;

        PlayerCape playerCape = capeManager.getCurrentCape(target);
        if (playerCape == null) return;

        playerCape.spawnForPlayer(player, target);
    }

    private void handleDestroyEntities(PacketPlaySendEvent event, Player player) {
        User user = event.getUser();
        if (user == null) return;

        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);
        int[] entities = packet.getEntityIds();
        List<Integer> list = Arrays.stream(entities).boxed().toList();

        prodigyManager.getProdigyPlayers().keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(target -> target != null)
                .filter(target -> {
                    PlayerCape playerCape = capeManager.getCurrentCape(target);
                    return playerCape != null && list.contains(target.getEntityId());
                })
                .findFirst()
                .ifPresent(target -> {
                    PlayerCape playerCape = capeManager.getCurrentCape(target);
                    playerCape.despawnForPlayer(player);
                });
    }


    private PlayerCape getPlayerCape(Player player) {
        if (!capeManager.hasCape(player)) {
            return null;
        }

        PlayerCape playerCape = capeManager.getCurrentCape(player);
        if (!playerCape.isVisible() || playerCape.getCapeDisplay() == null) {
            return null;
        }

        if (playerCape.getLastPosition() == null) {
            playerCape.setLastPosition(player.getLocation());
        }

        return playerCape;
    }

    private void updatePlayerCape(Player player, PlayerCape playerCape, Location from, Location to, @Nullable Float yaw) {
        float lastReceivedRawYaw = playerCape.getLastBodyYaw();
        float bodyYaw = playerCape.getCurrentBodyYaw();
        float attackAnim = playerCape.getAttackAnimation();

        float calculatedBodyYaw = calculateBodyYaw(player, from, to, lastReceivedRawYaw, bodyYaw, attackAnim);
        playerCape.setCurrentBodyYaw(calculatedBodyYaw);

        float calculatedSpeed = (float) Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
        playerCape.setCurrentSpeed(calculatedSpeed);
        playerCape.update(calculatedBodyYaw);

        if (yaw != null) {
            playerCape.setLastBodyYaw(yaw);
        }
        playerCape.setLastPosition(to);
    }

    private void updatePlayerCapeRotation(Player player, PlayerCape playerCape, float yaw) {
        float lastReceivedRawYaw = playerCape.getLastBodyYaw();
        float bodyYaw = playerCape.getCurrentBodyYaw();
        float attackAnim = playerCape.getAttackAnimation();

        float calculatedBodyYaw = calculateBodyYaw(player, null, null, lastReceivedRawYaw, bodyYaw, attackAnim);
        playerCape.setCurrentBodyYaw(calculatedBodyYaw);
        playerCape.update(calculatedBodyYaw);

        playerCape.setLastBodyYaw(yaw);
    }

    private void updatePlayerCapeAttackAnimation(Player player, PlayerCape playerCape) {
        playerCape.setAttackAnimation(1f);
        float lastReceivedRawYaw = playerCape.getLastBodyYaw();
        float bodyYaw = playerCape.getCurrentBodyYaw();

        float calculatedBodyYaw = calculateBodyYaw(player, null, null, lastReceivedRawYaw, bodyYaw, 1f);
        playerCape.setCurrentBodyYaw(calculatedBodyYaw);
        playerCape.update(calculatedBodyYaw);
    }

    public static float calculateBodyYaw(Player player, @Nullable Location from, @Nullable Location to, float lastReceivedRawYaw, float bodyYaw, float attackAnim) {
        float yaw = lastReceivedRawYaw;

        if (from != null && to != null) {
            double i = to.getX() - from.getX();
            double d = to.getZ() - from.getZ();
            float distanceSquared = (float) (i * i + d * d);

            if (distanceSquared > 0.0025000002F) {
                float direction = (float) Math.atan2(d, i) * (180F / (float) Math.PI) - 90.0F;
                float yawDifference = Math.abs(wrapDegrees(yaw) - direction);

                if (95.0F < yawDifference && yawDifference < 265.0F) {
                    bodyYaw = direction - 180.0F;
                } else {
                    bodyYaw = direction;
                }
            }
        }
        if (attackAnim > 0.0F) {
            bodyYaw = yaw;
        }

        return turnBody(player, bodyYaw, yaw);
    }

    public static float wrapDegrees(float degrees) {
        degrees %= 360;
        if (degrees >= 180.0F) {
            degrees -= 360.0F;
        } else if (degrees < -180.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }

    public static float turnBody(Player player, float bodyRotation, float yaw) {
        PlayerCape playerCape = capeManager.getCurrentCape(player);
        float currentBodyYaw = playerCape.getCurrentBodyYaw();

        float deltaYaw = wrapDegrees(bodyRotation - currentBodyYaw);
        currentBodyYaw += deltaYaw * 0.3F;

        float yawDifference = wrapDegrees(yaw - currentBodyYaw);
        if (yawDifference < -75.0F) {
            yawDifference = -75.0F;
        } else if (yawDifference > 75.0F) {
            yawDifference = 75.0F;
        }

        currentBodyYaw = yaw - yawDifference;
        if (yawDifference * yawDifference > 2500.0F) {
            currentBodyYaw += yawDifference * 0.2F;
        }

        playerCape.setCurrentBodyYaw(currentBodyYaw);

        return wrapDegrees(currentBodyYaw);
    }
}
