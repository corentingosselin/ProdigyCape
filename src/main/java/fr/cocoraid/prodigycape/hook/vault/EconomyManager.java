package fr.cocoraid.prodigycape.hook.vault;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.language.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private ConsoleCommandSender c = Bukkit.getConsoleSender();
    private Economy economy = null;

    public EconomyManager(ProdigyCape instance) {
        this.lm = instance.getLanguageManager();
        if (instance.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                c.sendMessage("§3Info: Vault economy is loaded ! cape purchase system is now enabled");
                this.economy = rsp.getProvider();
            } else {
                c.sendMessage("§6[ProdigyCape] Warning: Vault detected without economy plugin, if you want to enable the cape purchase system please install an economy plugin.");
            }
        }
    }


    private LanguageManager lm;

    public boolean purchase(Player buyer, OfflinePlayer seller, int price) {
        if (!hasEconomySystem()) {
            buyer.sendMessage(lm.getLanguage().cape_resell_not_allowed);
            return false;
        }
        if (!economy.has(buyer, price)) {
            buyer.sendMessage(lm.getLanguage().no_enough_money);
            return false;
        }
        boolean pay = economy.withdrawPlayer(buyer, price).transactionSuccess();
        boolean receive = economy.depositPlayer(seller, price).transactionSuccess();
        if (!pay) {
            buyer.sendMessage(lm.getLanguage().something_wrong);
            return false;
        }
        if (!receive) {
            buyer.sendMessage(lm.getLanguage().something_wrong);
            if(pay) {
                economy.depositPlayer(buyer, price);
                //send refund message
                //buyer.sendMessage(lang.something_wrong);
            }
            return false;
        }
        return true;
    }

    public boolean purchase(OfflinePlayer buyer, int price) {
        if (!hasEconomySystem() || price <= 0) {
            return true;
        }
        if (!economy.has(buyer, price)) {
            if (buyer.isOnline())
                buyer.getPlayer().sendMessage(lm.getLanguage().no_enough_money);
            return false;
        }
        boolean pay = economy.withdrawPlayer(buyer, price).transactionSuccess();
        if (!pay) {
            if (buyer.isOnline())
                buyer.getPlayer().sendMessage(lm.getLanguage().something_wrong);
            return false;
        }
        return true;
    }

    public boolean hasEconomySystem() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }
}
