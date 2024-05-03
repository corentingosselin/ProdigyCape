package fr.cocoraid.prodigycape.support.entities;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class DisplayItemNMS extends DisplayNMS {

    private Display.ItemDisplay display;

    public DisplayItemNMS(World world) {
        super(world, "DISPLAY_ITEM", new Display.ItemDisplay(EntityType.ITEM_DISPLAY, ((CraftWorld) world).getHandle()));
        this.display = (Display.ItemDisplay) entity;
    }
    public void setItemStack(ItemStack itemStack) {
        display.setItemStack(
                CraftItemStack.asNMSCopy(itemStack)
        );
    }

}
