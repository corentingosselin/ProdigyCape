package fr.cocoraid.prodigycape.support;


import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMS {

    private static final int viewDistance = Bukkit.getServer().getViewDistance();

    public static void sendPacket(World w, Packet<?> packet) {
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(cur -> cur.getWorld().equals(w))
                .forEach(cur -> {
                    sendPacket(cur, packet);
                });
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

}
