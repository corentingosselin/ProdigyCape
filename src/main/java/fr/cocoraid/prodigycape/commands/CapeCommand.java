package fr.cocoraid.prodigycape.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
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

import java.util.List;

@CommandAlias("%cape")
public class CapeCommand extends BaseCommand {

    private final ProdigyCape instance;
    private final CapeManager capeManager;
    private final LanguageManager languageManager;
    private final PlayerManager playerManager;

    public CapeCommand(ProdigyCape instance) {
        this.instance = instance;
        this.capeManager = instance.getCapeManager();
        this.languageManager = instance.getLanguageManager();
        this.playerManager = instance.getPlayerManager();
    }

    @Default
    @CommandPermission("prodigycape.help")
    @Subcommand("help")
    public void onCapeHelp(Player player) {
        List<String> help = languageManager.getLanguage().help_commands;
        for (String s : help) {
            player.sendMessage(s);
        }

        if(player.hasPermission("prodigycape.admin")) {
            player.sendMessage("§e/cape reload §7- §fReload the capes");
            player.sendMessage("§e/cape sync §7- §fSynchronize the local configuration with the database");
        }

    }

    boolean toggleCape = false;

    @CommandPermission("prodigycape.admin")
    @Subcommand("test")
    public void onCapeTest(Player player) {
        byte isCape = (byte) (toggleCape ? 126 : 127);
        EntityData data = new EntityData(17, EntityDataTypes.BYTE, (byte)isCape);
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(player.getEntityId(), List.of(data));
        playerManager.sendPacket(player, metadata);
        toggleCape = !toggleCape;
        player.sendMessage("§aCape toggled to " + (toggleCape ? "ON" : "OFF"));
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





}
