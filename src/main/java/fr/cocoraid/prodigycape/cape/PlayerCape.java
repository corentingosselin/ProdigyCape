package fr.cocoraid.prodigycape.cape;


import com.github.retrooper.packetevents.manager.player.PlayerManager;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;


import fr.cocoraid.prodigycape.ProdigyCape;

import fr.cocoraid.prodigycape.utils.ItemEditor;

import fr.cocoraid.prodigycape.utils.ProtocolVersionOffset;
import fr.cocoraid.prodigycape.utils.VersionChecker;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Collectors;

import static fr.cocoraid.prodigycape.listener.PacketEventsListener.calculateBodyYaw;

public class PlayerCape {


    private static ProdigyCape instance = ProdigyCape.getInstance();
    private static PlayerManager playerManager = instance.getPlayerManager();


    private float Y_OFFSET_TRANSLATION = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_20_R1) ? 0.0f : -0.4f;
    private static float INVERT_BACKWARD_FACTOR = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? -1F : 1F;
    private float Y_ROTATION_OFFSET = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? 180 : 0f;
    private static final float ROTATION_INTERPOLATION_SPEED = 0.15f;
    private static float DEFAULT_CAPE_X_ROTATION = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? 0F : 3F;

    private BukkitTask task;

    @Getter
    private boolean visible = true;
    private boolean spawned = false;

    private ItemStack capeItem;
    @Getter
    private WrapperEntity capeDisplay;
    private Player player;
    @Getter
    private Cape cape;


    // moving data
    private float currentCapeXRotation = DEFAULT_CAPE_X_ROTATION;
    @Setter
    private float currentSpeed = 0.0f;
    private boolean isSneaking = false;
    private float sneakOffset = 0.0f;
    @Setter
    @Getter
    private float lastBodyYaw = 0.0f;
    @Setter
    @Getter
    private float currentBodyYaw = 0.0f;
    @Setter
    @Getter
    private float attackAnimation = 0.0f;
    private float targetCapeXRotation = DEFAULT_CAPE_X_ROTATION;
    @Getter
    @Setter
    private Location lastPosition;
    private Location lastLocation;
    private boolean isMoving = false;


    public PlayerCape(Cape cape) {
        this.cape = cape;
        this.capeItem = new ItemEditor(Material.PLAYER_HEAD).setTexture(cape.getTexture()).getItem();
    }

    public void forceSpawn(Player player) {

        ItemDisplayMeta meta = (ItemDisplayMeta) capeDisplay.getEntityMeta();
        meta.setItem(SpigotConversionUtil.fromBukkitItemStack(capeItem));
        float height = 1.8f;
        meta.setScale(new com.github.retrooper.packetevents.util.Vector3f(1.1f, height, 0.12F));
        Location l = player.getLocation();
        l.setYaw(0);
        l.setPitch(0);
        player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) < Bukkit.getViewDistance() * Bukkit.getViewDistance()
                ).forEach(p -> {
                    WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(
                            capeDisplay.getEntityId(),
                            capeDisplay.getUuid(),
                            capeDisplay.getEntityType(),
                            SpigotConversionUtil.fromBukkitLocation(l),
                            0,
                            0,
                            null);
                    playerManager.sendPacket(p, spawnEntity);

                    //working as well
                    WrapperPlayServerSetPassengers setPassengers = new WrapperPlayServerSetPassengers(player.getEntityId(), new int[]{capeDisplay.getEntityId()});
                    playerManager.sendPacket(p, setPassengers);
                    //passengerActions.addPassenger(player.getEntityId(), capeDisplay.getEntityId());
                });

        capeDisplay.setLocation(SpigotConversionUtil.fromBukkitLocation(l));


    }

    public void spawn(Player player) {
        if (spawned) {
            return;
        }

        this.player = player;
        this.lastBodyYaw = player.getLocation().getYaw();
        this.currentBodyYaw = lastBodyYaw;

        capeDisplay = new WrapperEntity(EntityTypes.ITEM_DISPLAY);
        forceSpawn(player);

        task = new BukkitRunnable() {

            @Override
            public void run() {

                if (!spawned) return;


                if (visible) {
                    if (player.isSleeping() || player.isInvisible() || player.isSwimming() || player.isDead() || hasElytra()) {
                        visible(false);
                        return;
                    }
                } else {
                    if (!player.isSleeping() && !player.isInvisible() && !player.isSwimming() && !player.isDead() && !hasElytra()) {
                        visible(true);
                        realigneCapeRotationToPlayerBodyYaw();
                        return;
                    }
                }

                if (!visible) {
                    return;
                }

                Location currentLocation = player.getLocation();
                if (lastLocation == null) {
                    lastLocation = currentLocation;
                }

                boolean hasMoved = lastLocation.getX() != currentLocation.getX() || lastLocation.getZ() != currentLocation.getZ();
                if (hasMoved) {
                    if (!isMoving) {
                        isMoving = true;
                    }
                } else {
                    if (isMoving) {
                        isMoving = false;
                    }
                }

                if (attackAnimation > 0.0f) {
                    attackAnimation -= 0.1f;
                    currentBodyYaw = calculateBodyYaw(player, null, null, lastBodyYaw, currentBodyYaw, attackAnimation);
                    isMoving = true;
                }
                if (attackAnimation < 0.0f) attackAnimation = 0.0f;

                sneakOffset = (isSneaking && player.isFlying()) ? 20.0f : 0F;

                if (isMoving) {
                    targetCapeXRotation = Math.min((DEFAULT_CAPE_X_ROTATION) + (currentSpeed * 100), 100);
                    currentCapeXRotation += (targetCapeXRotation - currentCapeXRotation) * ROTATION_INTERPOLATION_SPEED;
                    update(currentBodyYaw);
                }
                lastLocation = currentLocation;


            }
        }.runTaskTimerAsynchronously(ProdigyCape.getInstance(), 0, 0);

        this.spawned = true;
    }

    public void spawnForPlayer(Player player, Player wearer) {
        if (!spawned) {
            return;
        }

        respawn(player, wearer);
    }

    public void despawnForPlayer(Player player) {
        if (!spawned) {
            return;
        }
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(new int[]{capeDisplay.getEntityId()});
        playerManager.sendPacket(player, destroyEntities);
    }

    public void respawn(Player player, Player wearer) {
        com.github.retrooper.packetevents.protocol.world.Location l = capeDisplay.getLocation();
        l.setYaw(0);
        l.setPitch(0);
        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(
                capeDisplay.getEntityId(),
                capeDisplay.getUuid(),
                capeDisplay.getEntityType(),
                l,
                0,
                0,
                null);
        playerManager.sendPacket(player, spawnEntity);
        new BukkitRunnable() {
            @Override
            public void run() {
                WrapperPlayServerSetPassengers setPassengers = new WrapperPlayServerSetPassengers(wearer.getEntityId(), new int[]{capeDisplay.getEntityId()});
                playerManager.sendPacket(player, setPassengers);
            }
        }.runTaskLater(ProdigyCape.getInstance(), 1);
    }

    public void spawnForOthers(Player player) {
        if (!spawned) {
            return;
        }
        player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) < 100)
                .forEach(p -> {
                    respawn(p, player);
                });

    }

    private boolean hasElytra() {
        return player.getInventory().getChestplate() != null
                && player.getInventory().getChestplate().getType() == Material.ELYTRA;
    }


    public void despawn() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (capeDisplay != null) {

            WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(new int[]{capeDisplay.getEntityId()});
            getViewerPlayers(player).forEach(p -> {
                playerManager.sendPacket(p, destroyEntities);
            });

            capeDisplay = null;
        }

        // reset all moving data
        currentCapeXRotation = DEFAULT_CAPE_X_ROTATION;
        currentSpeed = 0.0f;
        sneakOffset = 0.0f;
        lastBodyYaw = 0.0f;
        currentBodyYaw = 0.0f;
        targetCapeXRotation = DEFAULT_CAPE_X_ROTATION;

        this.spawned = false;

    }


    public void update(float bodyYaw) {
        // Convert yaw to radians for rotation
        this.currentBodyYaw = bodyYaw;
        float yawRadians = (float) Math.toRadians(-bodyYaw + Y_ROTATION_OFFSET);

        // Create a rotation quaternion for Y-axis based on player's yaw
        Quaternionf yRotationQuaternion = new Quaternionf().rotateY(yawRadians);

        // Define a small X-axis rotation (e.g., 5 degrees downward)
        float xRotationDegrees = (currentCapeXRotation + sneakOffset) * INVERT_BACKWARD_FACTOR;
        float xRotationRadians = (float) Math.toRadians(xRotationDegrees);
        Quaternionf xRotationQuaternion = new Quaternionf().rotateX(xRotationRadians);

        // Combine the rotations, applying X-axis rotation after Y-axis rotation
        Quaternionf combinedRotation = new Quaternionf(yRotationQuaternion).mul(xRotationQuaternion);

        // Adjust initial translation vector to ensure the cape starts behind the player
        // The backward offset here is critical; ensure it's negative to move the cape behind the player.
        // This is where you adjust based on the player's body rotation.
        Vector3f backwardOffset = new Vector3f(0, 0, -0.15f * INVERT_BACKWARD_FACTOR); // Adjust -0.5f as needed to place the cape correctly behind the player

        // Since we want the backwardOffset to apply correctly relative to the player's rotation,
        // we first rotate this offset by the player's current yaw (yRotationQuaternion) only.
        // This ensures the offset moves directly backwards from the player's current facing direction.
        backwardOffset.rotateY(yawRadians);


        // Update cape's transformation with the new rotation and adjusted translation

        ItemDisplayMeta meta = (ItemDisplayMeta) capeDisplay.getEntityMeta();
        meta.setNotifyAboutChanges(false);
        Quaternion4f quaternion4f = new Quaternion4f(combinedRotation.x, combinedRotation.y, combinedRotation.z, combinedRotation.w);

        meta.setLeftRotation(quaternion4f);
        meta.setTransformationInterpolationDuration(1);
        meta.setInterpolationDelay(0);


        getViewerPlayers(player).forEach(p -> {
            // Now, apply this rotated offset to the translation vector.
            float y_offset = Y_OFFSET_TRANSLATION;
            if (ViaVersionUtil.isAvailable()) {
                int version = ViaVersionUtil.getProtocolVersion(p);
                y_offset = ProtocolVersionOffset.getOffsetByVersion(version);
            }
            Vector3f translationVector = new Vector3f(0, y_offset, 0).add(backwardOffset);

            meta.setTranslation(new com.github.retrooper.packetevents.util.Vector3f(translationVector.x, translationVector.y, translationVector.z));
            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(capeDisplay.getEntityId(), meta.entityData());

            playerManager.sendPacket(p, metadata);

        });

    }


    public void onSneakEvent(boolean isSneaking) {
        this.isSneaking = isSneaking;

        if (player.isFlying()) return;
        // Set the X rotation to 20 degrees when sneaking, and reset it when not sneaking
        sneakOffset = isSneaking ? 20.0f : 0F;

        // Call the update method to apply the rotation change immediately
        update(currentBodyYaw);
    }

    public void realigneCapeRotationToPlayerBodyYaw() {
        update(currentBodyYaw);
    }


    public void visible(boolean visibility) {
        if (visibility) {
            forceSpawn(player);
        } else {
            WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(new int[]{capeDisplay.getEntityId()});
            getViewerPlayers(player).forEach(p -> {
                playerManager.sendPacket(p, destroyEntities);
            });

        }
        this.visible = visibility;
    }


    public int getViewDistance(World world) {
        int viewDistanceChunks = world.getViewDistance();
        int viewDistanceBlocks = viewDistanceChunks * 16;
        int distanceSquared = viewDistanceBlocks * viewDistanceBlocks;
        return distanceSquared;
    }

    public List<Player> getViewerPlayers(Player player) {
        int viewDistance = getViewDistance(player.getWorld());
        return player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) < viewDistance)
                .collect(Collectors.toList());
    }

}
