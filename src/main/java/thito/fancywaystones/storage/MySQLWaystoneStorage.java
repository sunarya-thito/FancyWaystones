package thito.fancywaystones.storage;

import com.zaxxer.hikari.*;
import org.yaml.snakeyaml.tokens.*;
import thito.fancywaystones.*;

import java.io.*;
import java.sql.*;
import java.util.*;

public class MySQLWaystoneStorage implements WaystoneStorage {
    private static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len;
        byte[] buff = new byte[1024 * 8];
        while ((len = inputStream.read(buff, 0, buff.length)) != -1) {
            outputStream.write(buff, 0, len);
        }
        return outputStream.toByteArray();
    }

    private HikariDataSource dataSource;
    private String playersTable, waystonesTable, namesTable;

    public MySQLWaystoneStorage(HikariDataSource dataSource, String playersTable, String waystonesTable, String namesTable) {
        this.dataSource = dataSource;
        this.playersTable = playersTable;
        this.waystonesTable = waystonesTable;
        this.namesTable = namesTable;
    }

    @Override
    public void migrateTo(WaystoneStorage storage) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet set = statement.executeQuery("SELECT `id`, `type`, `data` FROM `"+waystonesTable+"`")) {
                while (set.next()) {
                    try {
                        WaystoneType type = WaystoneManager.getManager().getType(set.getString(2));
                        if (type == null) continue;
                        UUID uuid = UUID.fromString(set.getString(1));
                        byte[] data = readAll(set.getBinaryStream(3));
                        storage.writeWaystoneData(type, uuid, data);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try (ResultSet set = statement.executeQuery("SELECT `id`, `data` FROM `"+playersTable+"`")) {
                while (set.next()) {
                    try {
                        UUID id = UUID.fromString(set.getString(1));
                        byte[] data = readAll(set.getBinaryStream(2));
                        storage.writePlayerData(id, data);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } catch (Throwable x) {
                x.printStackTrace();
            }
            try (ResultSet set = statement.executeQuery("SELECT `name`, `context` FROM `"+namesTable+"`")) {
                while (set.next()) {
                    try {
                        String name = set.getString(1);
                        String context = set.getString(2);
                        storage.putName(context, name);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

    public void connect() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS `" + playersTable + "`(`id` VARCHAR(36), `data` BLOB, PRIMARY KEY (`id`))");
            statement.execute("CREATE TABLE IF NOT EXISTS `" + waystonesTable + "`(`id` VARCHAR(36), `type` VARCHAR(48), `data` BLOB, PRIMARY KEY (`id`))");
            statement.execute("CREATE TABLE IF NOT EXISTS `" + namesTable + "`(`context` VARCHAR(48), `name` VARCHAR(64), UNIQUE KEY `unique`(`context`, `name`))");
        }
    }

    @Override
    public void removePlayerData(UUID id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + playersTable + "` WHERE `id` = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeWaystoneData(UUID id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE  FROM `" + waystonesTable + "` WHERE `id` = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] readWaystoneData(UUID id) throws IOException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `data` FROM `" + waystonesTable + "` WHERE `id` = ?")) {
            statement.setString(1, id.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readAll(resultSet.getBinaryStream(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public byte[] readPlayerData(UUID id) throws IOException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `data` FROM `" + playersTable + "` WHERE `id` = ?")) {
            statement.setString(1, id.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readAll(resultSet.getBinaryStream(1));
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return null;
    }

    @Override
    public void writeWaystoneData(WaystoneType type, UUID id, byte[] data) throws IOException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + waystonesTable + "` (`id`, `type`, `data`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `type`=VALUES(`type`), `data`=VALUES(`data`)")) {
            statement.setString(1, id.toString());
            statement.setString(2, type.name());
            statement.setBinaryStream(3, new ByteArrayInputStream(data));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writePlayerData(UUID id, byte[] data) throws IOException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + playersTable + "` (`id`, `data`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `data`=VALUES(`data`)")) {
            statement.setString(1, id.toString());
            statement.setBinaryStream(2, new ByteArrayInputStream(data));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<byte[]> readWaystones(WaystoneType type) {
        List<byte[]> data = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `data` FROM `" + waystonesTable + "` WHERE `type` = ?")) {
            statement.setString(1, type.name());
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    data.add(readAll(set.getBinaryStream(1)));
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public boolean containsName(String contextName, String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(`name`) FROM `"+namesTable+"` WHERE `name` = ? AND `context` = ?")) {
            statement.setString(1, name);
            statement.setString(2, contextName);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void putName(String contextName, String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT IGNORE INTO `"+namesTable+"`(`context`, `name`) VALUES (?, ?)")) {
            statement.setString(1, contextName);
            statement.setString(2, name.toUpperCase());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeName(String contextName, String name) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM `"+namesTable+"` WHERE `name` = ? AND `context` = ?")) {
                statement.setString(1, name.toUpperCase());
                statement.setString(2, contextName);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
