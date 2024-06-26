package fr.cocoraid.prodigycape.inventory;

import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.cape.OwnedCape;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class CapeInventory implements InventoryProvider {

    private static CapeManager capeManager = ProdigyCape.getInstance().getCapeManager();
    private static LanguageManager langManager = ProdigyCape.getInstance().getLanguageManager();

    private final ClickableItem close = ClickableItem.of(new ItemEditor(Material.BARRIER).setDisplayName(langManager.getLanguage().no_capes_close).getItem(), e -> e.getWhoClicked().closeInventory());

    public static SmartInventory getInventory() {
        return SmartInventory.builder()
                .provider(new CapeInventory())
                .size(6, 9)
                .title(langManager.getLanguage().owned_capes_menu_title)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        Language lang = langManager.getLanguage();

        ClickableItem playerHead = ClickableItem.empty(new ItemEditor(Material.PLAYER_HEAD)
                .setHead(player)
                .setDisplayName(langManager.getLanguage().owned_capes_showcase_menu_title).getItem());
        if (capeManager.hasCape(player)) {

            Cape currentCape = capeManager.getCurrentCape(player).getCape();
            playerHead = ClickableItem.empty(new ItemEditor(Material.PLAYER_HEAD)
                    .setTexture(currentCape.getTexture())
                    .setDisplayName(currentCape.getName())
                    .addLoreLine(currentCape.getDescription())
                    .addLoreLine(lang.equipped)
                    .getItem());
        }


        Pagination pagination = contents.pagination();

        Set<Cape> capes = capeManager.getOwnedCapes(player);

        Cape contributorCape = capeManager.getCapeContributors().getCape(player.getUniqueId());
        if (contributorCape != null) {
            capes.add(contributorCape);
        }

        if (capes.isEmpty()) {
            contents.set(3, 4, close);
            return;
        }

        ClickableItem[] items = new ClickableItem[capes.size()];
        int i = 0;
        for (Cape cape : capes) {

            OwnedCape ownedCape = capeManager.getOwnedCape(player, cape.getKey());

            ItemEditor itemEditor = new ItemEditor(Material.PLAYER_HEAD)
                    .setDisplayName(cape.getName())
                    .addLoreLine(cape.getDescription())
                    .setTexture(cape.getTexture());

            if(ownedCape != null && cape.isLimitedEdition()) {

                Date date = new Date(ownedCape.getBoughtTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateFormat.format(date);

                itemEditor.addLoreLine("");
                List<String> bought_info = lang.bought_info;
                bought_info.replaceAll(s -> s.replace("%date", formattedDate)
                        .replace("%price", String.valueOf(ownedCape.getBoughtPrice()) + " " + langManager.getLanguage().currency)
                        .replace("%edition", String.valueOf(ownedCape.getEditionNumber()))
                        .replace("%total", String.valueOf(cape.getLimitedEdition()))
                );
                itemEditor.addLoreLines(bought_info);
            }


            if (capeManager.hasCape(player, cape)) {
                itemEditor.addLoreLine(lang.equipped);
            }
            ItemStack item = itemEditor.getItem();
            items[i] = ClickableItem.of(item, e -> {
                capeManager.applyCape(player, cape);
                player.closeInventory();
            });
            i++;
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(14);

        SlotIterator si = contents.newIterator(SlotIterator.Type.HORIZONTAL, 2, 1);
        si.blacklist(2, 8);
        si.blacklist(3, 0);
        pagination.addToIterator(si);

        contents.set(0, 4, playerHead);


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
    public void update(Player player, InventoryContents inventoryContents) {

    }
}
