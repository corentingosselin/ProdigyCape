package fr.cocoraid.prodigycape.support.entities_1_20_1;

import com.google.common.base.Preconditions;
import fr.cocoraid.prodigycape.Reflection;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;


public class DisplayNMS extends EntityNMS {

    private float radius = 1f;

    private Display display;

    public DisplayNMS(World world, String name, Entity entity) {
        super(world, name);
        this.display = (Display) entity;
        this.entity = entity;

    }

    public void setInterpolationDuration(Integer duration) {
        if(duration == null) {
            display.setInterpolationDuration(0);
            return;
        }
        display.setInterpolationDuration(duration);
    }


    public DisplayNMS setViewRange (int viewDistance) {
        display.setViewRange(viewDistance);
        return this;
    }

    public DisplayNMS setGlow(Color color) {
        display.setGlowingTag(true);
        display.setGlowColorOverride(color.asARGB());
        return this;
    }

    public org.bukkit.util.Transformation getTransformation() {
        com.mojang.math.Transformation nms = Display.createTransformation(display.getEntityData());
        return new org.bukkit.util.Transformation(nms.getTranslation(), nms.getLeftRotation(), nms.getScale(), nms.getRightRotation());
    }

    public void setTransformation(org.bukkit.util.Transformation transformation) {
        Preconditions.checkArgument(transformation != null, "Transformation cannot be null");
        display.setTransformation(new com.mojang.math.Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation()));
        update();

    }

    public DisplayNMS setMaxBrightness() {
        display.setBrightnessOverride(new Brightness(15,15));
        return this;
    }

    private Reflection.FieldAccessor passengersField = Reflection.getField(ClientboundSetPassengersPacket.class, int[].class, 0);
    public void mount(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(serverPlayer);
        passengersField.set(packet, new int[]{display.getId()});
        NMS.sendPacket(player.getWorld(), packet);
    }

    public void dismount(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(serverPlayer);
        passengersField.set(packet, new int[]{});
        NMS.sendPacket(player.getWorld(), packet);
    }


    public void mount(Player viewer, Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(serverPlayer);
        passengersField.set(packet, new int[]{display.getId()});
        NMS.sendPacket(viewer, packet);
    }

    public void dismount(Player viewer, Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(serverPlayer);
        passengersField.set(packet, new int[]{});
        NMS.sendPacket(viewer, packet);
    }



}
