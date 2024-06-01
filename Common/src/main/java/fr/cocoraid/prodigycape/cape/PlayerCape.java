package fr.cocoraid.prodigycape.cape;

import com.github.retrooper.packetevents.manager.player.PlayerManager;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.maximde.passengerapi.PassengerAPI;
import com.maximde.passengerapi.PassengerActions;

import fr.cocoraid.prodigycape.ProdigyCape;

import fr.cocoraid.prodigycape.utils.ItemEditor;

import fr.cocoraid.prodigycape.utils.VersionChecker;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerCape {


    private static ProdigyCape instance = ProdigyCape.getInstance();
    private static PassengerActions passengerActions = instance.getPassengerActions();
    private static PlayerManager playerManager = instance.getPlayerManager();


    private float Y_OFFSET_TRANSLATION = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_20_R1) ? 0.0f : -0.4f;
    private static int INVERT_BACKWARD_FACTOR = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? -1 : 1;
    private float Y_ROTATION_OFFSET = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? 180 : 0f;
    private static final float ROTATION_INTERPOLATION_SPEED = 0.15f;
    private static float DEFAULT_CAPE_X_ROTATION = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? 0F : 10F;

    private BukkitTask task;

    private boolean visible = true;
    private boolean spawned = false;

    private ItemStack capeItem;
    private WrapperEntity capeDisplay;
    private Player player;
    private Cape cape;


    // moving data
    private float currentCapeXRotation = DEFAULT_CAPE_X_ROTATION;
    private float currentSpeed = 0.0f;
    private float sneakOffset = 0.0f;
    private float lastBodyYaw = 0.0f;
    private float currentBodyYaw = 0.0f;
    private float targetCapeXRotation = DEFAULT_CAPE_X_ROTATION;
    private Location lastPosition;


    public PlayerCape(Cape cape) {
        this.cape = cape;
        this.capeItem = new ItemEditor(Material.PLAYER_HEAD).setTexture(cape.getTexture()).getItem();
    }

    public void forceSpawn(Player player) {
        player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) < Bukkit.getViewDistance() * Bukkit.getViewDistance()
                ).forEach(p -> {
                    capeDisplay.addViewer(p.getUniqueId());
                });
        ItemDisplayMeta meta = (ItemDisplayMeta) capeDisplay.getEntityMeta();
        meta.setItem(SpigotConversionUtil.fromBukkitItemStack(capeItem));
        float height = 1.9f;
        meta.setScale(new com.github.retrooper.packetevents.util.Vector3f(1.2f, height, 0.08f));
        capeDisplay.spawn(SpigotConversionUtil.fromBukkitLocation(player.getLocation()));
        passengerActions.addPassenger(player.getEntityId(), capeDisplay.getEntityId());

    }

    public void spawn(Player player) {
        if (spawned) {
            return;
        }

        this.player = player;
        this.lastBodyYaw = player.getLocation().getYaw();
        this.currentBodyYaw = player.getLocation().getYaw();

        capeDisplay = EntityLib.getApi().createEntity(EntityTypes.ITEM_DISPLAY);
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
                targetCapeXRotation = Math.min((DEFAULT_CAPE_X_ROTATION) + (currentSpeed * 100), 100);
                currentCapeXRotation += (targetCapeXRotation - currentCapeXRotation) * ROTATION_INTERPOLATION_SPEED;
                update(currentBodyYaw);


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
        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(
                capeDisplay.getEntityId(),
                capeDisplay.getUuid(),
                capeDisplay.getEntityType(),
                capeDisplay.getLocation(),
                capeDisplay.getYaw(),
                0,
                null);
        playerManager.sendPacket(player, spawnEntity);
        new BukkitRunnable() {
            @Override
            public void run() {
                WrapperPlayServerSetPassengers setPassengers = new WrapperPlayServerSetPassengers(capeDisplay.getEntityId(), new int[]{wearer.getEntityId()});
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
            capeDisplay.despawn();
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

        // Now, apply this rotated offset to the translation vector.
        Vector3f translationVector = new Vector3f(0, Y_OFFSET_TRANSLATION, 0).add(backwardOffset);

        // Update cape's transformation with the new rotation and adjusted translation

        ItemDisplayMeta meta = (ItemDisplayMeta) capeDisplay.getEntityMeta();
        meta.setNotifyAboutChanges(false);
        Quaternion4f quaternion4f = new Quaternion4f(combinedRotation.x, combinedRotation.y, combinedRotation.z, combinedRotation.w);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("yaw: " + bodyYaw + " x: " + translationVector.x + " y: " + translationVector.y + " z: " + translationVector.z));
        meta.setLeftRotation(quaternion4f);
        meta.setTranslation(new com.github.retrooper.packetevents.util.Vector3f(translationVector.x, translationVector.y, translationVector.z));
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(capeDisplay.getEntityId(), meta.entityData());
        player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) < Bukkit.getViewDistance() * Bukkit.getViewDistance()
                ).forEach(p -> {
                    playerManager.sendPacket(p, metadata);
                });
    }


    public void onSneakEvent(boolean isSneaking) {
        if (player.isFlying()) return;
        // Set the X rotation to 20 degrees when sneaking, and reset it when not sneaking
        sneakOffset = isSneaking ? 20.0f : 0F;

        // Call the update method to apply the rotation change immediately
        update(currentBodyYaw);
    }

    public void realigneCapeRotationToPlayerBodyYaw() {
        update(currentBodyYaw);
    }


    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public float getLastBodyYaw() {
        return lastBodyYaw;
    }

    public float getCurrentBodyYaw() {
        return currentBodyYaw;
    }

    public void setCurrentBodyYaw(float currentBodyYaw) {
        this.currentBodyYaw = currentBodyYaw;
    }

    public void setLastBodyYaw(float lastBodyYaw) {
        this.lastBodyYaw = lastBodyYaw;
    }

    public Cape getCape() {
        return cape;
    }

    public WrapperEntity getCapeDisplay() {
        return capeDisplay;
    }

    public void visible(boolean visibility) {
        if (visibility) {
            forceSpawn(player);
        } else {
            capeDisplay.despawn();
        }
        this.visible = visibility;
    }

    public void setLastPosition(Location lastPosition) {
        this.lastPosition = lastPosition;
    }

    public Location getLastPosition() {
        return lastPosition;
    }

    public boolean isVisible() {
        return visible;
    }
}
