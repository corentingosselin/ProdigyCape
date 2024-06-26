package fr.cocoraid.prodigycape.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.ProdigyPlayer;
import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.cape.OwnedCape;
import fr.cocoraid.prodigycape.cape.PlayerCape;
import fr.cocoraid.prodigycape.configs.Configuration;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.manager.ProdigyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MysqlDatabase implements Database, SyncronizableDatabase {


    private Configuration configuration;
    private ProdigyCape instance;
    private ProdigyManager manager;
    private CapeManager capeManager;
    public boolean mysqlDisconnected = false;

    public MysqlDatabase(ProdigyCape instance) {
        this.instance = instance;
        this.manager = instance.getProdigyManager();
        this.capeManager = instance.getCapeManager();
        this.configuration = instance.getConfiguration();
    }

    private HikariDataSource hikari;

    public void initialize() {
        try {

            Configuration.DatabaseCredentials credentials = configuration.getDatabaseCredentials();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + credentials.host() + ":" + credentials.port() + "/" + credentials.name());
            config.setUsername(credentials.user());
            config.setPassword(credentials.password());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCachSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setDriverClassName("com.mysql.jdbc.Driver");
            hikari = new HikariDataSource(config);

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ProdigyCape] MySQL connected successfully !");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ProdigyCape] MySQL connection failed !");
            e.printStackTrace();
        }
        startConnection();
        generateDefaultCapes();
        createTable();
    }

    public void createTable() {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {


            // Create the players table
            String sqlPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "current_cape VARCHAR(255)" +
                    ")";
            statement.executeUpdate(sqlPlayers);

            // Create the owned_capes table
            String sqlOwnedCapes = "CREATE TABLE IF NOT EXISTS owned_capes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "bought_price DECIMAL(10, 2) NOT NULL," +
                    "edition INT NOT NULL," +
                    "`key` VARCHAR(255) NOT NULL," +
                    "bought_time BIGINT NOT NULL," +
                    "FOREIGN KEY (player_uuid) REFERENCES players(uuid)" +
                    ")";
            statement.executeUpdate(sqlOwnedCapes);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void generateDefaultCapes() {

        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            String checkTableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = '" + configuration.getDatabaseCredentials().name() + "' " +
                    "AND TABLE_NAME = 'capes'";

            ResultSet rs = statement.executeQuery(checkTableExistsSql);
            if (rs.next() && rs.getInt(1) > 0) {
                // TODO: syncronize existing table to capes config file
                return;
            }

            String sqlCapes = "CREATE TABLE capes (" +
                    "keyName VARCHAR(255) PRIMARY KEY," +
                    "enabled BOOLEAN NOT NULL," +
                    "texture TEXT NOT NULL," +
                    "name VARCHAR(255) NOT NULL," +
                    "description TEXT NOT NULL," +
                    "price DECIMAL(10, 2) NOT NULL," +
                    "limited_edition INT NOT NULL," +
                    "number_sold INT NOT NULL" +
                    ")";
            statement.executeUpdate(sqlCapes);

            // Insert the default capes
            Map<String, Cape> capes = capeManager.getCapes();
            // Prepare the SQL statement
            String insertSql = "INSERT INTO capes (keyName, enabled, texture, name, description, price, limited_edition, number_sold) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                for (Cape cape : capes.values()) {
                    ps.setString(1, cape.getKey());
                    ps.setInt(2, 1); // enabled
                    ps.setString(3, cape.getTexture());
                    ps.setString(4, cape.getName());
                    ps.setString(5, cape.getDescription());
                    ps.setDouble(6, cape.getPrice());
                    ps.setInt(7, cape.getLimitedEdition());
                    ps.setInt(8, 0); // number_sold

                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @Override
    public void loadPlayer(UUID uuid) {
        try (Connection connection = hikari.getConnection();
             //get player and owned capes with a join
             PreparedStatement ps = connection.prepareStatement("SELECT players.uuid, players.current_cape, owned_capes.id," +
                     " owned_capes.player_uuid, owned_capes.bought_price, owned_capes.edition, owned_capes.key, " +
                     "owned_capes.bought_time FROM players" +
                     " LEFT JOIN owned_capes ON players.uuid = owned_capes.player_uuid WHERE players.uuid = ?")) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                ProdigyPlayer pp = manager.getProdigyPlayer(uuid);
                if (pp == null) {
                    return;
                }

                while (rs.next()) {
                    String currentCape = rs.getString("current_cape");
                    if (currentCape != null) {
                        Cape cape = capeManager.getCape(currentCape);
                        if (cape != null) {
                            pp.setCape(new PlayerCape(cape));
                        }
                    }

                    int id = rs.getInt("id");
                    if (id != 0) {
                        double boughtPrice = rs.getDouble("bought_price");
                        int edition = rs.getInt("edition");
                        String key = rs.getString("key");
                        long boughtTime = rs.getLong("bought_time");

                        Cape cape = capeManager.getCape(key);
                        if (cape != null) {
                            OwnedCape ownedCape = new OwnedCape(key);
                            ownedCape.setEditionNumber(edition);
                            ownedCape.setBoughtTime(boughtTime);
                            ownedCape.setBoughtPrice(boughtPrice);
                            pp.addOwnedCape(ownedCape);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void savePlayer(UUID uuid) {
        ProdigyPlayer pp = manager.getProdigyPlayer(uuid);
        if (pp == null) {
            return;
        }

        if (!pp.isHasEdition()) {
            return;
        }

        // update or insert player
        try (Connection connection = hikari.getConnection()) {
            String sql = "INSERT INTO players (uuid, current_cape) VALUES (?, ?) ON DUPLICATE KEY UPDATE current_cape = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, pp.getCape().getCape().getKey());
                ps.setString(3, pp.getCape().getCape().getKey());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        saveOwnedCapes(uuid);

        pp.setHasEdition(false);


    }

    @Override
    public void saveCape(String key) {
        Cape cape = capeManager.getCape(key);
        if (cape == null) {
            return;
        }
        //insert or update cape
        try (Connection connection = hikari.getConnection()) {
            String sql = "INSERT INTO capes (keyName, enabled, texture, name, description, price, limited_edition, number_sold) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE enabled = ?, texture = ?, name = ?, description = ?, price = ?, limited_edition = ?, number_sold = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cape.getKey());
                ps.setInt(2, cape.isEnabled() ? 1 : 0);
                ps.setString(3, cape.getTexture());
                ps.setString(4, cape.getName());
                ps.setString(5, cape.getDescription());
                ps.setDouble(6, cape.getPrice());
                ps.setInt(7, cape.getLimitedEdition());
                ps.setInt(8, cape.getNumberSold());
                ps.setInt(9, cape.isEnabled() ? 1 : 0);
                ps.setString(10, cape.getTexture());
                ps.setString(11, cape.getName());
                ps.setString(12, cape.getDescription());
                ps.setDouble(13, cape.getPrice());
                ps.setInt(14, cape.getLimitedEdition());
                ps.setInt(15, cape.getNumberSold());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveOwnedCapes(UUID uuid) {
        ProdigyPlayer pp = manager.getProdigyPlayer(uuid);
        if (pp == null) {
            return;
        }

        Set<OwnedCape> ownedCapes = pp.getOwnedCapes();
        // update or insert owned_capes
        try (Connection connection = hikari.getConnection()) {
            String sql = "INSERT INTO owned_capes (player_uuid, bought_price, edition, `key`, bought_time) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE bought_price = ?, edition = ?, `key` = ?, bought_time = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (OwnedCape ownedCape : ownedCapes) {
                    ps.setString(1, uuid.toString());
                    ps.setDouble(2, ownedCape.getBoughtPrice());
                    ps.setInt(3, ownedCape.getEditionNumber());
                    ps.setString(4, ownedCape.getKey());
                    ps.setLong(5, ownedCape.getBoughtTime());
                    ps.setDouble(6, ownedCape.getBoughtPrice());
                    ps.setInt(7, ownedCape.getEditionNumber());
                    ps.setString(8, ownedCape.getKey());
                    ps.setLong(9, ownedCape.getBoughtTime());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {


        initialize();

    }

    private void attemptReconnect(int retries) {
        if (retries == 0) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: MySQL connection failed after retries.");
            mysqlDisconnected = true;
            return;
        }

        // Schedule the reconnect task asynchronously
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            try {
                Connection connection = hikari.getConnection();
                if (connection != null) {
                    mysqlDisconnected = false;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "MySQL reconnected successfully!");
                    return;
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: Attempting to reconnect to MySQL. Remaining tries: " + retries);
                e.printStackTrace();
            }
            attemptReconnect(retries - 1); // Recursive call to try again
        }, 100L); // 100L = 100 ticks, equivalent to 5 seconds
    }

    public Connection startConnection() {
        try {
            Connection connection = hikari.getConnection();
            if (connection != null) {
                mysqlDisconnected = false;
                return connection;
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: MySQL disconnected. Attempting to reconnect...");
            e.printStackTrace();
            attemptReconnect(3); // Start retries with a specified count, e.g., 3
        }
        return null;
    }

    @Override
    public void synchronize() {
        instance.reloadCommand();
        // synchronize capes with table capes
        Map<String, Cape> capes = capeManager.getCapes();
        // update or insert capes
        try (Connection connection = startConnection()) {
            for (Cape cape : capes.values()) {
                // Check if the cape exists
                String checkCapeExistsSql = "SELECT COUNT(*) FROM capes WHERE keyName = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkCapeExistsSql)) {
                    checkStmt.setString(1, cape.getKey());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {

                        // Cape exists, update it
                        String updateCapeSql = "UPDATE capes SET enabled = ?, name = ?, description = ?, price = ?, limited_edition = ?, texture = ? WHERE keyName = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateCapeSql)) {
                            updateStmt.setInt(1, cape.isEnabled() ? 1 : 0);
                            updateStmt.setString(2, cape.getName());
                            updateStmt.setString(3, cape.getDescription());
                            updateStmt.setDouble(4, cape.getPrice());
                            updateStmt.setInt(5, cape.getLimitedEdition());
                            updateStmt.setString(6, cape.getTexture());

                            updateStmt.setString(7, cape.getKey());
                            updateStmt.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    } else {
                        // Cape does not exist, insert it
                        String insertCapeSql = "INSERT INTO capes (keyName, enabled, texture, name, description, price, limited_edition, number_sold) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertCapeSql)) {
                            insertStmt.setString(1, cape.getKey());
                            insertStmt.setInt(2, cape.isEnabled() ? 1 : 0);
                            insertStmt.setString(3, cape.getTexture());
                            insertStmt.setString(4, cape.getName());
                            insertStmt.setString(5, cape.getDescription());
                            insertStmt.setDouble(6, cape.getPrice());
                            insertStmt.setInt(7, cape.getLimitedEdition());
                            insertStmt.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
