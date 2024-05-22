package fr.cocoraid.prodigycape.support;

import fr.cocoraid.prodigycape.IDisplayItem;
import fr.cocoraid.prodigycape.support.entities_1_20_1.DisplayItemNMS;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

public class DisplayItem_1_20_1 implements IDisplayItem {

    private DisplayItemNMS displayItemNMS;

    @Override
    public void spawn(Location location, ItemStack itemStack) {
        displayItemNMS = new DisplayItemNMS(location.getWorld());
        displayItemNMS.setLocation(location);
        displayItemNMS.setItemStack(itemStack);
        displayItemNMS.spawn();
    }

    @Override
    public void spawn(Player player) {
        displayItemNMS.spawn(player);
    }

    @Override
    public void despawn() {
        displayItemNMS.despawn();
    }

    @Override
    public void despawn(Player player) {
        displayItemNMS.despawn(player);
    }

    @Override
    public void setTransformation(Transformation transformation) {
        displayItemNMS.setTransformation(transformation);
    }

    @Override
    public void mount(Player player) {
        displayItemNMS.mount(player);
    }

    @Override
    public void mount(Player player, Player wearer) {
        displayItemNMS.mount(player, wearer);
    }

    @Override
    public void dismount(Player player) {
        displayItemNMS.dismount(player);
    }

    @Override
    public void dismount(Player player, Player wearer) {
        displayItemNMS.dismount(player, wearer);
    }


    @Override
    public Transformation getTransformation() {
        return displayItemNMS.getTransformation();
    }

    @Override
    public int getId() {
        return displayItemNMS.getId();
    }

    @Override
    public void setLocation(Location location) {
        displayItemNMS.setLocation(location);
        displayItemNMS.setWorld(location.getWorld());
    }
}

