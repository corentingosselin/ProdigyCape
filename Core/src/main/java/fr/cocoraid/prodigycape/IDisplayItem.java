package fr.cocoraid.prodigycape;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

public interface IDisplayItem {

    void spawn(Location location, ItemStack itemStack);
    void spawn(Player player);
    void despawn();
    void despawn(Player player);
    void mount(Player player);
    void mount(Player player, Player wearer);

    void setTransformation(Transformation transformation);

    int getId();

    Transformation getTransformation();
}
