package dao;

import config.DBConnection;
import config.Session;
import java.sql.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class StatisticsDAO {
    
    public int getTotalBooks() {
        return getCount("SELECT COUNT(*) FROM books");
    }
    
    public int getAvailableBooks() {
        return getCount("SELECT COUNT(*) FROM books WHERE status='Available'");
    }
    
    public int getBorrowedBooks() {
        return getCount("SELECT COUNT(*) FROM books WHERE status='Borrowed'");
    }
    
    public int getTotalUsers() {
        return getCount("SELECT COUNT(*) FROM users");
    }
    
    public int getActiveBorrowings() {
        return getCount("SELECT COUNT(*) FROM books WHERE status='Borrowed'");
    }
    
    public int getTotalReturns() {
        return getCount("SELECT COUNT(*) FROM borrowing_history WHERE returned_date IS NOT NULL");
    }
    
    public double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM books WHERE rating > 0";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) {
            return 0.0;
        }
    }
    
    public double getTotalFineCollected() {
        String sql = "SELECT SUM(fine_amount) FROM borrowing_history";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) {
            return 0.0;
        }
    }
    
    public Map<String, Integer> getBooksByCategory() {
        Map<String, Integer> categoryStats = new HashMap<>();
        String sql = "SELECT category, COUNT(*) as count FROM books WHERE category IS NOT NULL GROUP BY category";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                categoryStats.put(rs.getString("category"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryStats;
    }
    
    public List<Object[]> getMostBorrowedBooks(int limit) {
        List<Object[]> books = new ArrayList<>();
        String sql = "SELECT title, author, times_borrowed, rating FROM books ORDER BY times_borrowed DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                books.add(new Object[]{rs.getString("title"), rs.getString("author"), 
                                      rs.getInt("times_borrowed"), rs.getDouble("rating")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
    
    public void loadBorrowingHistory(DefaultTableModel model, String status) {
        model.setRowCount(0);
        String sql = "SELECT bh.id, u.username, b.title, bh.borrowed_date, bh.due_date, " +
                    "bh.returned_date, bh.status, bh.fine_amount " +
                    "FROM borrowing_history bh " +
                    "JOIN users u ON bh.user_id = u.id " +
                    "JOIN books b ON bh.book_id = b.id";
        
        if (!"All".equals(status)) {
            sql += " WHERE bh.status = '" + status + "'";
        }
        sql += " ORDER BY bh.borrowed_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("username"), rs.getString("title"),
                    rs.getString("borrowed_date"), rs.getString("due_date"),
                    rs.getString("returned_date") != null ? rs.getString("returned_date") : "-",
                    rs.getString("status"), String.format("$%.2f", rs.getDouble("fine_amount"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void loadUserReadingHistory(int userId, DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT b.title, b.author, bh.borrowed_date, bh.returned_date, " +
                    "CASE WHEN b.rating > 0 THEN CAST(b.rating AS VARCHAR) ELSE 'Not rated' END as rating " +
                    "FROM borrowing_history bh JOIN books b ON bh.book_id = b.id " +
                    "WHERE bh.user_id = ? AND bh.returned_date IS NOT NULL " +
                    "ORDER BY bh.returned_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("title"), 
                    rs.getString("author"),
                    rs.getString("borrowed_date"), 
                    rs.getString("returned_date"),
                    rs.getString("rating")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updateSystemSettings(int loanDuration, double fineAmount) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                 "INSERT OR REPLACE INTO system_settings (key, value) VALUES (?, ?)")) {
            pst.setString(1, "loan_duration");
            pst.setInt(2, loanDuration);
            pst.executeUpdate();
            
            pst.setString(1, "fine_amount");
            pst.setDouble(2, fineAmount);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean updateBookRating(int bookId, double rating) {
        String sql = "UPDATE books SET rating = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setDouble(1, rating);
            pst.setInt(2, bookId);
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating rating: " + e.getMessage());
            return false;
        }
    }
    
    public List<Object[]> getUnratedBooks(int userId) {
        List<Object[]> unratedBooks = new ArrayList<>();
        String sql = "SELECT bh.book_id, b.title, b.author " +
                     "FROM borrowing_history bh " +
                     "JOIN books b ON bh.book_id = b.id " +
                     "WHERE bh.user_id = ? AND bh.returned_date IS NOT NULL AND (b.rating = 0 OR b.rating IS NULL)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                unratedBooks.add(new Object[]{
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author")
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting unrated books: " + e.getMessage());
        }
        
        return unratedBooks;
    }
    
    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }
}