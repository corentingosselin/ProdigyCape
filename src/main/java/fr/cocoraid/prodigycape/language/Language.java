package fr.cocoraid.prodigycape.language;

import fr.cocoraid.prodigycape.language.abstraction.LanguageAbstract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Language extends LanguageAbstract {

    public String next_button = "§7Next";
    public String previous_button = "§7Previous";

    public String owned_capes_menu_title = "My capes";
    public String owned_capes_showcase_menu_title = "My capes";

    public String no_capes_close = "§cSorry there is no capes here \nClose the inventory";
    public String shop_capes_menu_title = "Official cape shop";

    public String equipped = "\n§a§lEquipped";

    public String no_enough_money = "§cYou don't have enough money";
    public String something_wrong = "§cSomething went wrong, please contact an admin";
    public String cape_resell_not_allowed = "§cCape resell is not allowed";

    public String confirm = "§aConfirm";
    public String cancel = "§cCancel";

    public String confirmation_menu_title = "Purchase confirmation";

    public String confirm_purchase = "\n§aConfirm purchase";
    public String click_to_purchase = "\n§aClick to purchase";
    public String price = "§7Price: §6%price";
    public String currency = "$";

    public String no_eco_plugin = "§cNo cape shop available, please contact an administrator";
    public String cape_purchased_and_equipped = "§aCape purchased and now equipped ! You can find all your capes using /cape menu";

    public String sold_out = "§cSold out ! Sorry there is no cape left for this article";
    public String no_permission = "§cYou don't have the permission to do this";
    public String limited_edition = "\n§cLimited edition: %cape_left_number left";

    public List<String> bought_info = new ArrayList<>(Arrays.asList(
            "§5Edition number: #§d%edition §5/ %total",
            "§6Bought for: %price",
            "§7Buy date: %date"
    ));

    //inventory

    public String event_menu_title = "Available events";
    public List<String> event_menu_lore = new ArrayList<>(Arrays.asList(
            "§3Tickets: %ticket_ratio",
            "§3Start: " + "%start",
            "§3End: " + "%end",
            "§3Place: §b" + "%nightclub"));


}
