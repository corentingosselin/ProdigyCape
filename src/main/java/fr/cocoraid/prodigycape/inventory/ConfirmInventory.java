package fr.cocoraid.prodigycape.inventory;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.language.LanguageManager;
import fr.cocoraid.prodigycape.utils.ItemEditor;
import fr.depends.minuskube.inv.ClickableItem;
import fr.depends.minuskube.inv.SmartInventory;
import fr.depends.minuskube.inv.content.InventoryContents;
import fr.depends.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ConfirmInventory implements InventoryProvider {

    private static final LanguageManager lm = ProdigyCape.getInstance().getLanguageManager();

    public static SmartInventory getInventory(ItemStack item, Consumer<InventoryClickEvent> confirm, Consumer<InventoryClickEvent> decline) {
        return SmartInventory.builder()
                .provider(new ConfirmInventory(item, confirm, decline))
                .size(6, 9)
                .title(lm.getLanguage().confirmation_menu_title)
                .build();
    }

    private ItemStack item;
    private Consumer<InventoryClickEvent> confirmConsumer;
    private Consumer<InventoryClickEvent> declineConsumer;

    public ConfirmInventory(ItemStack item, Consumer<InventoryClickEvent> confirm, Consumer<InventoryClickEvent> decline) {
        this.item = item;
        this.confirmConsumer = confirm;
        this.declineConsumer = decline;
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        ClickableItem confirm = ClickableItem.of(
                new ItemEditor(Material.GREEN_CONCRETE)
                        .setDisplayName(lm.getLanguage().confirm).getItem(), confirmConsumer);
        ClickableItem decline = ClickableItem.of(new ItemEditor(Material.RED_CONCRETE).setDisplayName(lm.getLanguage().cancel).getItem(), declineConsumer);


        contents.set(3, 1, confirm);
        contents.set(3, 2, confirm);
        contents.set(4, 1, confirm);
        contents.set(4, 2, confirm);

        contents.set(3, 6, decline);
        contents.set(3, 7, decline);
        contents.set(4, 6, decline);
        contents.set(4, 7, decline);


        contents.set(1, 4, ClickableItem.empty(item));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
