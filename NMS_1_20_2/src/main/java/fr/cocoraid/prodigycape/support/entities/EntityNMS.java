package fr.cocoraid.prodigycape.support.entities;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class EntityNMS {

    private World world;
    protected String ENTITY;
    protected Entity entity;
    private Location location;

    public EntityNMS(World world, String ENTITY) {
        this.ENTITY = ENTITY;
        this.world = world;
    }

    public void spawn(Player player) {

        Packet spawn = null;
        if (entity instanceof LivingEntity)
            spawn = new ClientboundAddEntityPacket(entity);
        else if (entity instanceof LightningBolt) {

            Vec3 vec = new Vec3(0, 0, 0);
            spawn = new ClientboundAddEntityPacket(getId(),
                    getUniqueID(),
                    location.getX(), location.getY(), location.getZ(),
                    0f, 0f,
                    EntityType.LIGHTNING_BOLT, 0, vec, 0);
        } else {
            Vec3 vec = new Vec3(0, 0, 0);
            spawn = new ClientboundAddEntityPacket(getId(),
                    getUniqueID(),
                    location.getX(), location.getY(), location.getZ(),
                    0f, 0f,
                    entity.getType(), 0, vec, 0);
        }

        NMS.sendPacket(player, spawn);
        sendMetaPacket(player);

    }

    public void spawn() {
        Packet spawn = null;
        if (entity instanceof LivingEntity)
            spawn = new ClientboundAddEntityPacket(entity);
        else if (entity instanceof LightningBolt) {

            Vec3 vec = new Vec3(0, 0, 0);
            spawn = new ClientboundAddEntityPacket(getId(),
                    getUniqueID(),
                    location.getX(), location.getY(), location.getZ(),
                    0f, 0f,
                    EntityType.LIGHTNING_BOLT, 0, vec, 0);
        } else {
            Vec3 vec = new Vec3(0, 0, 0);
            spawn = new ClientboundAddEntityPacket(getId(),
                    getUniqueID(),
                    location.getX(), location.getY(), location.getZ(),
                    0f, 0f,
                    entity.getType(), 0, vec, 0);
        }

        NMS.sendPacket(world, spawn);
        sendMetaPacket(world);
    }

    public void despawn(Player player) {
        ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(getId());
        NMS.sendPacket(player, destroy);
    }

    public void despawn() {
        ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(getId());
        NMS.sendPacket(world, destroy);
    }

    public void update(Player player) {
        sendMetaPacket(player);
    }

    public void update() {
        sendMetaPacket(world);
    }

    /**
     * Builder Part
     */

    public void teleport(Player player, Location location) {
        setLocation(location);
        ClientboundTeleportEntityPacket tp = new ClientboundTeleportEntityPacket(entity);
        NMS.sendPacket(player, tp);
    }

    public void teleport(Location location) {
        setLocation(location);
        ClientboundTeleportEntityPacket tp = new ClientboundTeleportEntityPacket(entity);
        NMS.sendPacket(world, tp);
    }

    public EntityNMS setLocation(Location location) {
        //wtf int and boolean not even used in nms code
        //set loc and rot
        if (Double.isNaN(location.getX()) || Double.isNaN(location.getY()) || Double.isNaN(location.getZ()) || Double.isNaN(location.getYaw()) || Double.isNaN(location.getPitch()))
            return this;
        entity.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.location = location;
        return this;
    }

    public SynchedEntityData getDataWatcher() {
        return entity.getEntityData();
    }

    public int getId() {
        return entity.getId();
    }

    public void setId(int id) {
        this.entity.setId(id);
    }


    public UUID getUniqueID() {
        return entity.getUUID();
    }

    public Entity getEntity() {
        return entity;
    }


    public Location getLocation() {
        return location;
    }

    public void sendMetaPacket(Player player) {
        @Nullable List<SynchedEntityData.DataValue<?>> metas = getDataWatcher().getNonDefaultValues();
        if (metas != null && !metas.isEmpty()) {
            ClientboundSetEntityDataPacket meta = new ClientboundSetEntityDataPacket(getId(), metas);
            NMS.sendPacket(player, meta);
        }
    }

    public void sendMetaPacket(World world) {
        @Nullable List<SynchedEntityData.DataValue<?>> metas = getDataWatcher().getNonDefaultValues();
        if (metas != null && !metas.isEmpty()) {
            ClientboundSetEntityDataPacket meta = new ClientboundSetEntityDataPacket(getId(), metas);
            NMS.sendPacket(world, meta);
        }
    }

}
