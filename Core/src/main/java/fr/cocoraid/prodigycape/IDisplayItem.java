package fr.cocoraid.prodigycape;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

import javax.annotation.Nullable;

public interface IDisplayItem {

    void spawn(Location location, ItemStack itemStack, @Nullable Integer interpolationDuration);
    void spawn(Player player);
    void despawn();
    void despawn(Player player);
    void mount(Player player);
    void mount(Player player, Player wearer);
    void dismount(Player player);
    void dismount(Player player, Player wearer);

    void setTransformation(Transformation transformation);

    int getId();

    Transformation getTransformation();

    void setLocation(Location location);
}
