package fr.cocoraid.prodigycape.support;


import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NMS_1_19_4 implements fr.cocoraid.prodigycape.NmsHandler {


    @Override
    public void removeCape(Player player) {

        ServerPlayer sp = ((CraftPlayer) player).getHandle();

        SynchedEntityData entityData = sp.getEntityData();
        entityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126);
        List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
        eData.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126));

        ClientboundSetEntityDataPacket meta = new ClientboundSetEntityDataPacket(sp.getId(), eData);
        sp.connection.send(meta);
    }


}
