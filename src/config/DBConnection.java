package config;

import static java.nio.file.StandardOpenOption.CREATE;
import java.sql.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DBConnection {
    
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection pooledConnection = null;
    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        if (pooledConnection == null || pooledConnection.isClosed()) {
            pooledConnection = DriverManager.getConnection(DB_URL);
            
            if (!initialized) {
                initializeDatabase(pooledConnection);
                initialized = true;
            }
        }
        return pooledConnection;
    }
    
   // Add this to initializeDatabase method in DBConnection.java
private static void initializeDatabase(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
        stmt.execute("PRAGMA foreign_keys = ON;");
        stmt.execute("PRAGMA journal_mode=WAL;");
        stmt.execute("PRAGMA synchronous=NORMAL;");
        
        String createUsersTable =
    "CREATE TABLE IF NOT EXISTS users (" +
    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
    "username TEXT UNIQUE NOT NULL," +
    "password TEXT NOT NULL," +
    "role TEXT CHECK(role IN ('admin','customer')) DEFAULT 'customer'," +
    "full_name TEXT," +
    "email TEXT," +
    "phone TEXT," +
    "profile_image TEXT," +
    "total_books_read INTEGER DEFAULT 0," +
    "active_borrowings INTEGER DEFAULT 0," +
    "last_login TIMESTAMP," +
    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
    ")";
        
        String createBooksTable = 
            "CREATE TABLE IF NOT EXISTS books (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT NOT NULL, " +
            "author TEXT NOT NULL, " +
            "isbn TEXT UNIQUE, " +
            "category TEXT, " +
            "publication_year INTEGER, " +
            "publisher TEXT, " +
            "description TEXT, " +
            "cover_image TEXT, " +
            "status TEXT CHECK(status IN ('Available', 'Borrowed', 'Reserved')) DEFAULT 'Available', " +
            "borrowed_by INTEGER, " +
            "borrowed_date TIMESTAMP, " +
            "due_date TIMESTAMP, " +
            "returned_date TIMESTAMP, " +
            "rating REAL DEFAULT 0, " +
            "times_borrowed INTEGER DEFAULT 0, " +
            "FOREIGN KEY (borrowed_by) REFERENCES users(id)" +
            ")";
        
        String createBorrowingHistory =
    "CREATE TABLE IF NOT EXISTS borrowing_history (" +
    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
    "user_id INTEGER," +
    "book_id INTEGER," +
    "borrowed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
    "due_date TIMESTAMP," +
    "returned_date TIMESTAMP," +
    "status TEXT," +
    "fine_amount REAL DEFAULT 0," +
    "FOREIGN KEY(user_id) REFERENCES users(id)," +
    "FOREIGN KEY(book_id) REFERENCES books(id)" +
    ")";

String createSystemSettings =
    "CREATE TABLE IF NOT EXISTS system_settings (" +
    "key TEXT PRIMARY KEY," +
    "value TEXT" +
    ")";
        
        String createCategories = 
            "CREATE TABLE IF NOT EXISTS categories (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT UNIQUE NOT NULL, " +
            "description TEXT" +
            ")";
        
        stmt.execute(createUsersTable);
        stmt.execute(createBooksTable);
        stmt.execute(createBorrowingHistory);
        stmt.execute(createCategories);
        
        // Insert default categories
        String insertCategories = 
            "INSERT OR IGNORE INTO categories(name, description) VALUES " +
            "('Fiction', 'Fictional books including novels and stories'), " +
            "('Non-Fiction', 'Educational and informational books'), " +
            "('Science', 'Scientific books and research materials'), " +
            "('Technology', 'Computer science and technology books'), " +
            "('History', 'Historical books and biographies'), " +
            "('Arts', 'Art, music, and design books')";
        stmt.execute(insertCategories);
        
        String insertAdmin = 
            "INSERT OR IGNORE INTO users(username, password, role, email, full_name) " +
            "VALUES('admin','admin123','admin','admin@library.com','System Administrator')";
        stmt.execute(insertAdmin);
        
        // Insert sample books
        String insertSampleBooks = 
            "INSERT OR IGNORE INTO books(title, author, category, isbn, status) VALUES " +
            "('The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', '978-0-7432-7356-5', 'Available'), " +
            "('To Kill a Mockingbird', 'Harper Lee', 'Fiction', '978-0-06-112008-4', 'Available'), " +
            "('1984', 'George Orwell', 'Fiction', '978-0-452-28423-4', 'Available'), " +
            "('Pride and Prejudice', 'Jane Austen', 'Fiction', '978-0-14-143951-8', 'Available'), " +
            "('The Catcher in the Rye', 'J.D. Salinger', 'Fiction', '978-0-316-76948-0', 'Available'), " +
            "('Clean Code', 'Robert C. Martin', 'Technology', '978-0-13-235088-4', 'Available'), " +
            "('Design Patterns', 'Erich Gamma', 'Technology', '978-0-201-63361-0', 'Available'), " +
            "('Sapiens', 'Yuval Noah Harari', 'History', '978-0-06-231609-7', 'Available')";
        stmt.execute(insertSampleBooks);
        
    } catch (SQLException e) {
        System.err.println("Database initialization error: " + e.getMessage());
    }
}

    public boolean executeUpdate(String sql, Object... values) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("DB Update Error: " + e.getMessage());
            return false;
        }
    }

    public ResultSet getData(String sql, Object... values) {
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            return pstmt.executeQuery();
            
        } catch (SQLException e) {
            System.err.println("Fetch Error: " + e.getMessage());
            return null;
        }
    }
    
    public void displayData(String sql, JTable table, Object... values) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            
            ResultSet rs = pstmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i-1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            
        } catch (SQLException e) {
            System.err.println("Display Error: " + e.getMessage());
        }
    }
    
    public static void closeConnection() {
        if (pooledConnection != null) {
            try {
                pooledConnection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
}