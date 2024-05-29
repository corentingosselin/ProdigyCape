package fr.cocoraid.prodigycape.listener;

import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.cape.PlayerCape;
import fr.cocoraid.prodigycape.ProdigyCape;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

public class CapeListener implements Listener {

    private CapeManager capeManager = ProdigyCape.getInstance().getCapeManager();

    @EventHandler
    public void sneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!capeManager.hasCape(player)) {
            return;
        }
        PlayerCape playerCape = capeManager.getCurrentCape(player);
        if (playerCape.getCapeDisplay() == null) return;
        playerCape.onSneakEvent(e.isSneaking());
    }


    public float calculateBodyYaw(Player player, Location from, Location to, float lastReceivedRawYaw, float bodyYaw) {
        float yaw = lastReceivedRawYaw; // Use the last raw yaw
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

        return turnBody(player, bodyYaw, yaw); // Implement this based on the Minecraft source logic
    }

    // Placeholder for the MathHelper.wrapDegrees method
    public static float wrapDegrees(float degrees) {
        degrees %= 360;
        if (degrees >= 180.0F) {
            degrees -= 360.0F;
        } else if (degrees < -180.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }


    // Placeholder for the turnBody logic, adapt this method based on Minecraft's source code
    // Adjusted function to include Player UUID parameter
    public float turnBody(Player player, float bodyRotation, float yaw) {
        PlayerCape playerCape = capeManager.getCurrentCape(player);
        // Retrieve the current body yaw for the player
        float currentBodyYaw = playerCape.getCurrentBodyYaw();

        float deltaYaw = wrapDegrees(bodyRotation - currentBodyYaw);
        currentBodyYaw += deltaYaw * 0.3F; // Adjust this formula as necessary

        // Limit the change in body yaw to avoid snapping
        float yawDifference = wrapDegrees(yaw - currentBodyYaw);
        if (yawDifference < -75.0F) {
            yawDifference = -75.0F;
        } else if (yawDifference > 75.0F) {
            yawDifference = 75.0F;
        }

        // Update the body yaw to the new calculated value
        currentBodyYaw = yaw - yawDifference;
        if (yawDifference * yawDifference > 2500.0F) {
            currentBodyYaw += yawDifference * 0.2F;
        }

        // Update the map with the new body yaw
        playerCape.setCurrentBodyYaw(currentBodyYaw);

        return wrapDegrees(currentBodyYaw);
    }
}
