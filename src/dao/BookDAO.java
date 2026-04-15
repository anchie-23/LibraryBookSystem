package dao;

import config.DBConnection;
import config.Session;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import model.Book;

public class BookDAO {
    private DBConnection dbConnection;
    
    public BookDAO() {
        this.dbConnection = new DBConnection();
    }
    
    // Original addBook method (kept for compatibility)
    public boolean addBook(String title, String author) {
        // Validation
        if (title == null || title.trim().isEmpty() || title.length() < 2) {
            return false;
        }
        
        if (author == null || author.trim().isEmpty() || author.length() < 2) {
            return false;
        }
        
        String sql = "INSERT INTO books(title, author, status) VALUES (?, ?, 'Available')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, title.trim());
            pst.setString(2, author.trim());
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }
    
    // New addBook method with all fields
    public boolean addBook(String title, String author, String category, String isbn, 
                          String publicationYear, String publisher, String description) {
        // Validation
        if (title == null || title.trim().isEmpty() || title.length() < 2) {
            return false;
        }
        
        if (author == null || author.trim().isEmpty() || author.length() < 2) {
            return false;
        }
        
        String sql = "INSERT INTO books(title, author, category, isbn, publication_year, " +
                    "publisher, description, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Available')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, title.trim());
            pst.setString(2, author.trim());
            pst.setString(3, category != null ? category : "Uncategorized");
            pst.setString(4, isbn != null ? isbn.trim() : "");
            pst.setString(5, publicationYear != null && !publicationYear.isEmpty() ? publicationYear : null);
            pst.setString(6, publisher != null ? publisher.trim() : "");
            pst.setString(7, description != null ? description.trim() : "");
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding book with details: " + e.getMessage());
            return false;
        }
    }
    
    // Update book method
    public boolean updateBook(int id, String title, String author, String category, 
                             String isbn, String publicationYear, String publisher, String description) {
        // Validation
        if (title == null || title.trim().isEmpty() || title.length() < 2) {
            return false;
        }
        
        if (author == null || author.trim().isEmpty() || author.length() < 2) {
            return false;
        }
        
        String sql = "UPDATE books SET title=?, author=?, category=?, isbn=?, " +
                    "publication_year=?, publisher=?, description=? WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, title.trim());
            pst.setString(2, author.trim());
            pst.setString(3, category != null ? category : "Uncategorized");
            pst.setString(4, isbn != null ? isbn.trim() : "");
            pst.setString(5, publicationYear != null && !publicationYear.isEmpty() ? publicationYear : null);
            pst.setString(6, publisher != null ? publisher.trim() : "");
            pst.setString(7, description != null ? description.trim() : "");
            pst.setInt(8, id);
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }
    }
    
    // Load books with all fields for admin panel
    public void loadBooks(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT id, title, author, isbn, category, status, times_borrowed FROM books ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn") != null ? rs.getString("isbn") : "-",
                    rs.getString("category") != null ? rs.getString("category") : "-",
                    rs.getString("status"),
                    rs.getInt("times_borrowed")
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
    }
    
    // Load simple books list for customer panel
    public void loadSimpleBooks(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT id, title, author, status FROM books ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("status")
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
    }
    
    public boolean deleteBook(int id) {
        // First check if book is borrowed
        if (!isBookAvailable(id)) {
            System.err.println("Cannot delete book that is currently borrowed!");
            return false;
        }
        
        String sql = "DELETE FROM books WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, Session.getUserId()); // user ID
pst.setInt(2, id); // book ID
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean borrowBook(int id, int userId, String username) {
        String sql = "UPDATE books SET status='Borrowed', borrowed_by=?, borrowed_date=CURRENT_TIMESTAMP, " +
                    "due_date=date('now', '+14 days'), times_borrowed=times_borrowed+1 WHERE id=? AND status='Available'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            pst.setInt(2, id);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean returnBook(int id) {
        String sql = "UPDATE books SET status='Available', borrowed_by=NULL, borrowed_date=NULL, " +
                    "due_date=NULL WHERE id=? AND status='Borrowed'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, Session.getUserId()); // user ID
pst.setInt(2, id); // book ID
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            return false;
        }
    }
    
    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, Session.getUserId()); // user ID
pst.setInt(2, id); // book ID
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("status")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting book: " + e.getMessage());
        }
        
        return null;
    }
    
    public int getTotalBooks() {
        String sql = "SELECT COUNT(*) as count FROM books";
        return getCount(sql);
    }
    
    public int getAvailableBooks() {
        String sql = "SELECT COUNT(*) as count FROM books WHERE status='Available'";
        return getCount(sql);
    }
    
    public int getBorrowedBooks() {
        String sql = "SELECT COUNT(*) as count FROM books WHERE status='Borrowed'";
        return getCount(sql);
    }
    
    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            return rs.getInt("count");
            
        } catch (SQLException e) {
            System.err.println("Error getting count: " + e.getMessage());
            return 0;
        }
    }
    
    public boolean isBookAvailable(int id) {
        String sql = "SELECT status FROM books WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return "Available".equals(rs.getString("status"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking book availability: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean incrementTimesBorrowed(int bookId) {
        String sql = "UPDATE books SET times_borrowed = times_borrowed + 1 WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, bookId);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error incrementing times borrowed: " + e.getMessage());
            return false;
        }
    }
}