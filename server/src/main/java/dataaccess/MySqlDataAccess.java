package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.sql.SQLException;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
                )""";

        String createAuthTable = """
                CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken),
                FOREIGN KEY (username) REFERENCES users(username)
                )""";

        String createGameTable = """
                CREATE TABLE IF NOT EXISTS game (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                game TEXT,
                PRIMARY KEY (gameID)
                )""";

        try (var conn = DatabaseManager.getConnection();
        var statement = conn.createStatement()) {
            statement.executeUpdate(createUsersTable);
            statement.executeUpdate(createAuthTable);
            statement.executeUpdate(createGameTable);
        } catch (SQLException e) {
            throw new DataAccessException("failt to create tables in db", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM auth");
            statement.executeUpdate("DELETE FROM game");
            statement.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear db", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // INSERT INTO users (username, password, email) VALUES (?, ?, ?)

        // String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
        // String hashed= BCrypt.hashpw(user.password(), BCrypt.gensalt());

        //Check if username is takekn
        if (getUser(user.username()) != null) {
            throw new DataAccessException("User already exists or username is taken");
        }

        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to add user to db", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (var returnStatement = ps.executeQuery()) {
                if (!returnStatement.next()) {
                    return null;
                }
                return new UserData(
                        returnStatement.getString("username"),
                        returnStatement.getString("password"),
                        returnStatement.getString("email")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user from db", e);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) Values (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, null);
            ps.setString(2, null);
            ps.setString(3, gameName);
            ps.setString(4, null);

            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("failed to generate or fetch gameID");
                }
                int gameID = keys.getInt(1);
                return new GameData(gameID, null, null, gameName, null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to create", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = java.util.UUID.randomUUID().toString();
        String sql = "Insert INTO auth (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
        var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.setString(2, username);
            ps.executeUpdate();
            return new AuthData(authToken, username);
        } catch (SQLException e) {
            throw new DataAccessException("Create auth failed");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
        var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);

            try (var returnStatement = ps.executeQuery()) {
                if (!returnStatement.next()) {
                    return null;
                }
                return new AuthData(
                        returnStatement.getString("authToken"),
                        returnStatement.getString("username")
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("failed to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
        var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
    } catch (SQLException e) {
            throw new DataAccessException("deleteAuth failed");
        }
    }
}
