package fr.cocoraid.prodigycape.inventory;

import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.language.Language;
import fr.cocoraid.prodigycape.language.LanguageManager;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.utils.Heads;
import fr.cocoraid.prodigycape.utils.ItemEditor;
import fr.depends.minuskube.inv.ClickableItem;
import fr.depends.minuskube.inv.SmartInventory;
import fr.depends.minuskube.inv.content.InventoryContents;
import fr.depends.minuskube.inv.content.InventoryProvider;
import fr.depends.minuskube.inv.content.Pagination;
import fr.depends.minuskube.inv.content.SlotIterator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class OfficialShopCapeInventory implements InventoryProvider {

    private static CapeManager capeManager = ProdigyCape.getInstance().getCapeManager();
    private static LanguageManager langManager = ProdigyCape.getInstance().getLanguageManager();

    public static SmartInventory getInventory() {
        return SmartInventory.builder()
                .provider(new OfficialShopCapeInventory())
                .size(6, 9)
                .title(langManager.getLanguage().shop_capes_menu_title)
                .build();
    }


    private final ClickableItem close = ClickableItem.of(new ItemEditor(Material.BARRIER).setDisplayName(langManager.getLanguage().no_capes_close).getItem(), e -> e.getWhoClicked().closeInventory());


    @Override
    public void init(Player player, InventoryContents contents) {

        Language lang = langManager.getLanguage();

        Pagination pagination = contents.pagination();

        Set<Cape> capes = capeManager.getPurchasableCapes(player);

        if (capes.isEmpty()) {
            contents.set(3, 4, close);
            return;
        }

        ClickableItem[] items = new ClickableItem[capes.size()];
        int i = 0;
        for(Cape cape : capes) {
            ItemEditor itemEditor = new ItemEditor(Material.PLAYER_HEAD)
                    .setDisplayName(cape.getName())
                    .addLoreLine(cape.getDescription())
                    .setTexture(cape.getTexture());

            if(cape.isLimitedEdition()) {
                int capeLeft = cape.getLimitedEdition() - cape.getNumberSold();
                itemEditor.addLoreLine(lang.limited_edition.replace("%cape_left_number", String.valueOf(capeLeft)));

            }

            itemEditor.addLoreLine(lang.price.replace("%price", cape.getPrice() + " " + lang.currency));

            //prepare confirm item
            ItemEditor itemClone = new ItemEditor(itemEditor.getItem());
            itemClone.addLoreLine(lang.confirm_purchase);


            itemEditor.addLoreLine(lang.click_to_purchase);

            ItemStack item = itemEditor.getItem();

            items[i] = ClickableItem.of(item, e -> {

                ConfirmInventory.getInventory(itemClone.getItem(),
                        c -> {
                            boolean purchased = capeManager.purchaseCape(player, cape);
                            player.closeInventory();
                            if (!purchased) {
                                return;
                            }
                            player.sendMessage(lang.cape_purchased_and_equipped);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        }, d -> {
                            OfficialShopCapeInventory.getInventory().open(player);
                        }
                ).open(player);
            });
            i++;
        }


        pagination.setItems(items);
        pagination.setItemsPerPage(21);

        SlotIterator si = contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1);
        si.blacklist(1, 8);
        si.blacklist(2, 0);
        si.blacklist(3, 8);
        si.blacklist(4, 0);
        pagination.addToIterator(si);



        if (!pagination.isFirst()) {
            contents.set(5, 1, ClickableItem.of(Heads.ARROW_LEFT,
                    e -> getInventory().open(player, pagination.previous().getPage())));
        }

        if (!pagination.isLast()) {
            contents.set(5, 7, ClickableItem.of(Heads.ARROW_RIGHT,
                    e -> getInventory().open(player, pagination.next().getPage())));
        }

    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {}
}
