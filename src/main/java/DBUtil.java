import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static final String URL = "jdbc:sqlite:museum.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC драйвер не знайдено!");
            System.err.println("Переконайтеся, що sqlite-jdbc-3.45.1.0.jar знаходиться у папці lib/");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            conn.setAutoCommit(false);

            // Таблиця категорій експонатів
            st.executeUpdate("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT)");

            // Таблиця залів музею
            st.executeUpdate("CREATE TABLE IF NOT EXISTS halls (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "floor INTEGER NOT NULL, " +
                    "capacity INTEGER)");

            // Таблиця кураторів
            st.executeUpdate("CREATE TABLE IF NOT EXISTS curators (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE, " +
                    "phone TEXT, " +
                    "specialization TEXT)");

            // Таблиця експонатів
            st.executeUpdate("CREATE TABLE IF NOT EXISTS exhibits (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "creation_period TEXT, " +
                    "arrival_date TEXT NOT NULL, " +
                    "category_id INTEGER, " +
                    "hall_id INTEGER, " +
                    "curator_id INTEGER, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (curator_id) REFERENCES curators(id) ON DELETE SET NULL)");

            // Таблиця подій (виставки, екскурсії)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "event_type TEXT NOT NULL, " +
                    "event_date TEXT NOT NULL, " +
                    "hall_id INTEGER, " +
                    "curator_id INTEGER, " +
                    "max_visitors INTEGER, " +
                    "description TEXT, " +
                    "FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (curator_id) REFERENCES curators(id) ON DELETE SET NULL)");

            // Індекси для прискорення пошуку
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_exhibits_name ON exhibits(name)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_exhibits_arrival ON exhibits(arrival_date)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_events_date ON events(event_date)");

            conn.commit();
            System.out.println("База даних успішно ініціалізована!");
        } catch (SQLException ex) {
            System.err.println("Помилка ініціалізації БД: " + ex.getMessage());
        }
    }
}
