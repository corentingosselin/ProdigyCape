package fr.cocoraid.prodigycape;

import co.aikar.commands.CommandReplacements;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import fr.cocoraid.prodigycape.commands.CapeCommand;
import fr.cocoraid.prodigycape.commands.CapeCompletion;
import fr.cocoraid.prodigycape.commands.CapeContext;
import fr.cocoraid.prodigycape.configs.Configuration;
import fr.cocoraid.prodigycape.database.DatabaseManager;
import fr.cocoraid.prodigycape.hook.HookRegister;
import fr.cocoraid.prodigycape.hook.vault.EconomyManager;
import fr.cocoraid.prodigycape.language.LanguageManager;
import fr.cocoraid.prodigycape.listener.CapeListener;
import fr.cocoraid.prodigycape.listener.JoinQuitListener;
import fr.cocoraid.prodigycape.listener.TeleportListener;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.manager.ProdigyManager;

import fr.cocoraid.prodigycape.nms.NmsHandlerFactory;
import fr.cocoraid.prodigycape.utils.VersionChecker;
import fr.depends.minuskube.inv.InventoryManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class ProdigyCape extends JavaPlugin {

    private NmsHandler nmsHandler;

    private static InventoryManager invManager;

    private LanguageManager languageManager;
    private EconomyManager economyManager;

    private PaperCommandManager commandManager;
    private static ProdigyCape instance;
    private ProdigyManager prodigyManager;
    private CapeManager capeManager;
    private DatabaseManager databaseManager;
    private Configuration configuration;

    @Override
    public void onEnable() {
        instance = this;

        this.nmsHandler = NmsHandlerFactory.getHandler();

        this.configuration = new Configuration(this);
        configuration.load();

        this.languageManager = new LanguageManager(this);
        languageManager.load();


        HookRegister hookRegister = new HookRegister(this);
        this.economyManager = hookRegister.loadEconomyManager();


        invManager = new InventoryManager(this);
        invManager.init();


        this.prodigyManager = new ProdigyManager();

        //circular dependency here, capeManager needs databaseManager and databaseManager needs capeManager
        capeManager = new CapeManager(this);

        this.databaseManager = new DatabaseManager(this, configuration.getDatabaseType());
        databaseManager.initialize();
        capeManager.setDatabaseManager(databaseManager);

        capeManager.applyAllCapes();

        loadCommands();

        getServer().getPluginManager().registerEvents(new CapeListener(), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(), this);
        new Metrics(this, 21468);

        registerProtocolLib();

    }


    private void registerProtocolLib() {
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib is not installed, players with official capes would have duplicated capes. Please install ProtocolLib to fix this issue.");
            return;
        }
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        PacketAdapter packetAdapter = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.SETTINGS) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();

                if(VersionChecker.isLowerOrEqualThan(VersionChecker.v1_20_R1)) {
                    packet.getModifier().write(4,126);
                    return;
                }
                Object clientInformation = packet.getModifier().read(0);
                Object newClientInfo = nmsHandler.clientInfoWithoutCape(clientInformation);
                packet.getModifier().write(0, newClientInfo);
            }
        };


        manager.addPacketListener(packetAdapter);


    }


    public void reloadCommand() {
        this.prodigyManager.getProdigyPlayers().keySet().forEach(uuid -> {
            this.databaseManager.getDatabase().savePlayer(uuid);
        });

        configuration.load();
        languageManager.load();
        databaseManager.close();
        capeManager.removeAllCapes();
        databaseManager.getDatabase().reload();
        capeManager.clearCapes();
        capeManager.getCapesFile().initCapeFile();
        capeManager.applyAllCapes();
        loadCommands();
    }


    private void loadCommands() {
        this.commandManager = new PaperCommandManager(this);
        commandManager.getLocales().setDefaultLocale(Locale.ENGLISH);
        new CapeCompletion(this).register();
        new CapeContext(this).register();

        CommandReplacements replacements = commandManager.getCommandReplacements();
        replacements.addReplacement("cape", configuration.getCustomCommand());
        commandManager.registerCommand(new CapeCommand(this));
    }


    @Override
    public void onDisable() {

        this.prodigyManager.getProdigyPlayers().keySet().forEach(uuid -> {
            this.databaseManager.getDatabase().savePlayer(uuid);
        });
        this.databaseManager.close();
        capeManager.removeAllCapes();

    }

    public static ProdigyCape getInstance() {
        return instance;
    }

    public CapeManager getCapeManager() {
        return capeManager;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static InventoryManager manager() {
        return invManager;
    }

    public ProdigyManager getProdigyManager() {
        return prodigyManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public NmsHandler getNmsHandler() {
        return nmsHandler;
    }
}
