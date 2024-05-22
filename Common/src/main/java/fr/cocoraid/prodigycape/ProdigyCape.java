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

import fr.cocoraid.prodigycape.cape.PlayerCape;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

                if (VersionChecker.isLowerOrEqualThan(VersionChecker.v1_20_R1)) {
                    packet.getModifier().write(4, 126);
                    return;
                }
                Object clientInformation = packet.getModifier().read(0);
                Object newClientInfo = nmsHandler.clientInfoWithoutCape(clientInformation);
                packet.getModifier().write(0, newClientInfo);
            }
        };


        manager.addPacketListener(packetAdapter);

        PacketAdapter packetMount = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.MOUNT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                int[] entities = (int[]) packet.getModifier().read(1);
                PlayerCape playerCape = capeManager.getCurrentCape(player);
                if (playerCape == null) return;

                if (entities.length == 0) {
                    packet.getModifier().write(1, new int[]{playerCape.getCapeDisplay().getId()});
                    return;
                }

                int capeId = playerCape.getCapeDisplay().getId();
                if (entities.length == 1) {
                    int potentialCapeId = entities[0];
                    if (potentialCapeId == capeId) return;
                }


                int[] newEntities = new int[entities.length + 1];
                // make the capeID the first entity
                newEntities[0] = capeId;
                for (int i = 0; i < entities.length; i++) {
                    newEntities[i + 1] = entities[i];
                }
                packet.getModifier().write(1, newEntities);

            }
        };

        manager.addPacketListener(packetMount);


        PacketAdapter spawnPacket = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                UUID uuid = packet.getUUIDs().read(0);
                Player target = Bukkit.getPlayer(uuid);
                if (target == null) return;
                PlayerCape playerCape = capeManager.getCurrentCape(target);
                if (playerCape == null) return;
                playerCape.spawnForPlayer(player, target);

            }
        };

        manager.addPacketListener(spawnPacket);

        PacketAdapter despawnPacket = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_DESTROY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                List<Integer> list = packet.getIntLists().read(0);

                prodigyManager.getProdigyPlayers().keySet().forEach(uuid -> {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target == null) return;
                    PlayerCape playerCape = capeManager.getCurrentCape(target);
                    if (playerCape == null) return;
                    int playerId = nmsHandler.getEntityId(target);
                    if (list.contains(playerId)) {
                        playerCape.despawnForPlayer(player);
                    }
                });




            }
        };

        manager.addPacketListener(despawnPacket);

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
