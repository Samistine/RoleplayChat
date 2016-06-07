package com.gmail.bkunkcu.roleplaychat.Nickname;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gmail.bkunkcu.roleplaychat.RoleplayChat;
import java.sql.PreparedStatement;
import java.util.Optional;

public final class DatabaseManager {

    private final RoleplayChat plugin;
    private Connection connection;
    private Statement statement;
    private ResultSet resultset;

    public DatabaseManager(RoleplayChat plugin) {
        this.plugin = plugin;
    }

    public void open() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath().toString() + "/nicknames.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        createTable();
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }

    private boolean isOpen() {
        return connection != null;
    }

    private void createTable() {
        query("CREATE TABLE IF NOT EXISTS nicknames (username VARCHAR(20) PRIMARY KEY, nickname VARCHAR(20))");
    }

    private ResultSet query(String query) {
        if (isOpen()) {
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (query.startsWith("SELECT")) {
                try {
                    resultset = statement.executeQuery(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    statement.executeUpdate(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return resultset;
        }

        return null;
    }

    ///
    ///Begin Database methods
    ///
    /**
     * Get a given player's nickname.
     *
     * @param username
     * @return optional wrapped in an other optional.
     * <br>
     * The <b>outer optional</b> will be empty if the database was closed or an
     * error occurred while attempting to execute an update.
     * <br>
     * The <b>inner optional</b> will contain the nickname of the the given user
     * contained in the database or empty if there is none.
     */
    Optional<Optional<String>> getNickname(String username) {
        if (isOpen()) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM nicknames WHERE username=?"
                );
                preparedStatement.setString(1, username);

                ResultSet rs = preparedStatement.executeQuery();

                return Optional.of(
                        Optional.ofNullable(
                                rs.next() ? rs.getString("nickname") : null
                        )
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    /**
     * Sets a given player's nickname.
     * <br>
     * This will create an entry in the database if one doesn't exist otherwise
     * it will update the value.
     *
     * @param username username of the player
     * @param nickname nickname to set/update for the player
     */
    void setNickname(String username, String nickname) {
        if (isOpen()) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT OR REPLACE INTO nicknames (username, nickname) VALUES (?, ?)"
                );
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, nickname);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes a given player's nickname.
     * <br>
     * This will remove the entry, for the player, in the database if one
     * exists, otherwise nothing happens
     *
     * @param username username of the player
     * @return <b>TRUE</b> if the nickname was removed, <b>FALSE</b> if there
     * was no nickname to begin with. An  <b>EMPTY OPTIONAL</b> is returned if
     * the database was closed or an error occurred while attempting to execute
     * an update
     */
    Optional<Boolean> removeNickname(String username) {
        if (isOpen()) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "DELETE FROM nicknames WHERE username=?"
                );
                preparedStatement.setString(1, username);

                return Optional.of(preparedStatement.executeUpdate() > 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

}
