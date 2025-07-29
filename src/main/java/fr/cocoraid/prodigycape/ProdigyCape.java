package fr.cocoraid.prodigycape;

import co.aikar.commands.CommandReplacements;
import co.aikar.commands.PaperCommandManager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
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
import fr.cocoraid.prodigycape.listener.PacketEventsListener;
import fr.cocoraid.prodigycape.listener.TeleportListener;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.manager.ProdigyManager;

import fr.depends.minuskube.inv.InventoryManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

@Getter
public final class ProdigyCape extends JavaPlugin {

    @Getter
    private static ProdigyCape instance;
    @Getter
    private static InventoryManager invManager;

    private LanguageManager languageManager;
    private EconomyManager economyManager;
    private PaperCommandManager commandManager;
    private ProdigyManager prodigyManager;
    private CapeManager capeManager;
    private DatabaseManager databaseManager;
    private Configuration configuration;

    private PlayerManager playerManager;


    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false) // read only ?
                .checkForUpdates(true)
                .bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;

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

        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener(this));
        PacketEvents.getAPI().init();
        this.playerManager = PacketEvents.getAPI().getPlayerManager();
        loadCommands();

        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);

        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .usePlatformLogger();

        EntityLib.init(platform, settings);

        getServer().getPluginManager().registerEvents(new CapeListener(), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(), this);
        new Metrics(this, 21468);
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

        PacketEvents.getAPI().terminate();
    }

}
