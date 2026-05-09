package arpharmacy.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection – singleton JDBC helper.
 * Edit DB_URL / USER / PASS to match your MySQL setup.
 */
public class DBConnection {

    // ── Configuration ────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/ar_pharmacy?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "shaikh123";          // ← change to your MySQL password
    // ─────────────────────────────────────────────────────────────────────────

    private static Connection connection = null;

    /** Returns a live (or re-opened) connection. */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found: " + e.getMessage());
            System.err.println("Add mysql-connector-j-*.jar to your project classpath.");
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
        }
        return connection;
    }

    /** Closes the shared connection (call on app exit). */
    public static void close() {
        if (connection != null) {
            try { connection.close(); }
            catch (SQLException ignored) {}
        }
    }

    private DBConnection() {}   // prevent instantiation
}
