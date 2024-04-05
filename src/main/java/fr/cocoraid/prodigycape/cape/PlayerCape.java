package fr.cocoraid.prodigycape.cape;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.utils.ItemEditor;

import fr.cocoraid.prodigycape.utils.VersionChecker;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerCape {


    private float Y_OFFSET_TRANSLATION = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_20_R1) ? 0.0f : -0.4f;
    private static int INVERT_BACKWARD_FACTOR = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? -1 : 1;
    private float Y_ROTATION_OFFSET = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? 180 : 0f;
    private static final float ROTATION_INTERPOLATION_SPEED = 0.15f;
    private static float DEFAULT_CAPE_X_ROTATION = VersionChecker.isLowerOrEqualThan(VersionChecker.v1_19_R3) ? 0F : 10F;


    private BukkitTask task;

    private boolean visible = true;
    private boolean spawned = false;

    private ItemStack capeItem;
    private ItemDisplay capeDisplay;
    private Player player;
    private Cape cape;



    // moving data
    private float currentCapeXRotation = DEFAULT_CAPE_X_ROTATION;
    private float currentSpeed = 0.0f;
    private float sneakOffset = 0.0f;
    private float lastBodyYaw = 0.0f;
    private float currentBodyYaw = 0.0f;
    private float targetCapeXRotation = DEFAULT_CAPE_X_ROTATION;


    public PlayerCape(Cape cape) {
        this.cape = cape;
        this.capeItem = new ItemEditor(Material.PLAYER_HEAD).setTexture(cape.getTexture()).getItem();
    }

    public void spawn(Player player) {
        if (spawned) {
            return;
        }

        this.player = player;
        this.lastBodyYaw = player.getLocation().getYaw();
        this.currentBodyYaw = player.getLocation().getYaw();

        if (!player.getPassengers().isEmpty()) {
            player.getPassengers().forEach(player::removePassenger);
        }

        capeDisplay = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);
        capeDisplay.setItemStack(capeItem);

        float height = 1.9f;
        Transformation transformation = capeDisplay.getTransformation();
        transformation.getScale().set(1.2f, height, 0.08f);
        capeDisplay.setTransformation(transformation);

        player.addPassenger(capeDisplay);

        task = new BukkitRunnable() {

            @Override
            public void run() {

                if(capeDisplay.getVehicle() == null && spawned) {
                    // look for the player, if still online·
                    capeDisplay.teleport(player.getLocation());
                    player.addPassenger(capeDisplay);
                }

                if(capeDisplay.isVisibleByDefault() && player.isSleeping()) {
                    visible(false);

                    return;
                } else if (!capeDisplay.isVisibleByDefault() && !player.isSleeping()) {
                    realigneCapeRotationToPlayerBodyYaw();
                    visible(true);
                    return;
                }

                if ((player.isInvisible() || hasElytra() || player.isSwimming()  || player.isDead())
                        && capeDisplay.isVisibleByDefault()) {
                    visible(false);
                } else if (!player.isInvisible() && !hasElytra() && !player.isSwimming() && !player.isDead()
                        && !capeDisplay.isVisibleByDefault()) {
                    visible(true);

                }

                if (!visible) {
                    return;
                }
                targetCapeXRotation = Math.min((DEFAULT_CAPE_X_ROTATION) + (currentSpeed * 100), 100); // Adjust formula as needed
                currentCapeXRotation += (targetCapeXRotation - currentCapeXRotation) * ROTATION_INTERPOLATION_SPEED;
                update(currentBodyYaw);


            }
        }.runTaskTimer(ProdigyCape.getInstance(), 0, 0);

        this.spawned = true;
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
            visible(true);
            capeDisplay.remove();
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
        Transformation transformation = capeDisplay.getTransformation();
        transformation.getLeftRotation().set(combinedRotation); // Apply combined rotation
        transformation.getTranslation().set(translationVector); // Set the adjusted translation vector

        capeDisplay.setTransformation(transformation);
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

    public ItemDisplay getCapeDisplay() {
        return capeDisplay;
    }

    public void visible(boolean visibility) {
        capeDisplay.setVisibleByDefault(visibility);
        this.visible = visibility;
    }

    public boolean isVisible() {
        return visible;
    }
}
