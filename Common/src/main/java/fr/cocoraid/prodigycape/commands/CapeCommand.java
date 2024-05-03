package fr.cocoraid.prodigycape.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.database.Database;
import fr.cocoraid.prodigycape.database.SyncronizableDatabase;
import fr.cocoraid.prodigycape.inventory.OfficialShopCapeInventory;
import fr.cocoraid.prodigycape.language.LanguageManager;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.inventory.CapeInventory;
import fr.cocoraid.prodigycape.utils.ItemEditor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("%cape")
public class CapeCommand extends BaseCommand {

    private final ProdigyCape instance;
    private final CapeManager capeManager;
    private final LanguageManager languageManager;

    public CapeCommand(ProdigyCape instance) {
        this.instance = instance;
        this.capeManager = instance.getCapeManager();
        this.languageManager = instance.getLanguageManager();
    }

    @Default
    @CommandPermission("prodigycape.help")
    @Subcommand("help")
    public void onCapeHelp(Player player) {

        if(player.hasPermission("prodigycape.admin")) {

        }
    }

    @Syntax("<cape>")
    @CommandCompletion("@capes")
    @Subcommand("apply")
    public void onCapeApply(Player player, Cape cape) {
        if(!capeManager.ownsCape(player, cape)) {
            player.sendMessage(languageManager.getLanguage().no_permission);
            return;
        }
        capeManager.applyCape(player, cape);
    }

    @Syntax("<cape>")
    @CommandPermission("prodigycape.menu")
    @Subcommand("menu")
    public void onCapeMenu(Player player) {
        CapeInventory.getInventory().open(player);
    }


    @Syntax("<cape>")
    @CommandPermission("prodigycape.shop")
    @Subcommand("shop")
    public void onCapeShop(Player player) {
        if(!instance.getEconomyManager().hasEconomySystem()) {
            player.sendMessage(languageManager.getLanguage().no_eco_plugin);
            return;
        }
        OfficialShopCapeInventory.getInventory().open(player);
    }




    @CommandPermission("prodigycape.admin")
    @Subcommand("reload")
    public void onCapeReload(CommandSender sender) {
        instance.reloadCommand();
        sender.sendMessage("§aCapes reloaded.");
    }

    @CommandPermission("prodigycape.admin")
    @Subcommand("sync")
    public void onCapeSync(CommandSender sender) {
        Database db = instance.getDatabaseManager().getDatabase();
        if(db instanceof SyncronizableDatabase) {
            ((SyncronizableDatabase) db).synchronize();
            sender.sendMessage("§aCapes synchronized.");
        } else {
            sender.sendMessage("§cThis database is not syncronizable.");
        }
    }



    @CommandPermission("prodigycape.admin")
    @Subcommand("test")
    public void onCapeTest(Player player) {
        player.sendMessage("§aCape test.");


    }




}
