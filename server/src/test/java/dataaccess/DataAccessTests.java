package dataaccess;

import org.junit.jupiter.api.*;

import java.sql.SQLDataException;
import java.sql.SQLException;

public class DataAccessTests {

    @Test
    void smokeTest() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection();
        var ps = conn.prepareStatement("SELECT 1");
        var rs = ps.executeQuery()) {
            Assertions.assertTrue(rs.next());
            Assertions.assertEquals(1, rs.getInt(1));
        }
    }
}
