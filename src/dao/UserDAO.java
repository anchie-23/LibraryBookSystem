package dao;

import config.DBConnection;
import config.Session;
import model.User;
import java.sql.*;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;

public class UserDAO {
    private DBConnection dbConnection;
    
    public UserDAO() {
        this.dbConnection = new DBConnection();
    }
    
    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                Session.setSession(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean registerUser(String username, String password, String role) {
        // Validation
        if (!isValidUsername(username)) {
            return false;
        }
        
        if (!isValidPassword(password)) {
            return false;
        }
        
        String sql = "INSERT INTO users(username, password, role) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Username already exists: " + username);
            } else {
                System.err.println("Registration error: " + e.getMessage());
            }
            return false;
        }
    }
    
    public boolean updateUserProfile(int id, String fullName, String email, String phone) {
    String sql = "UPDATE users SET full_name=?, email=?, phone=? WHERE id=?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, fullName);
        pst.setString(2, email);
        pst.setString(3, phone);
        pst.setInt(4, id);

        return pst.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("Update profile error: " + e.getMessage());
        return false;
    }
}
    
    public boolean updateUserProfileImage(int userId, String profileImageBase64) {
        String sql = "UPDATE users SET profile_image=? WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, profileImageBase64);
            pst.setInt(2, userId);
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating profile image: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id=? AND role != 'admin'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean promoteToAdmin(int userId) {
        String sql = "UPDATE users SET role='admin' WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error promoting user: " + e.getMessage());
            return false;
        }
    }
    
    public void loadUsers(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT id, username, email, full_name, role, total_books_read, " +
                    "CASE WHEN active_borrowings > 0 THEN 'Active' ELSE 'Inactive' END as status " +
                    "FROM users WHERE role != 'admin' ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email") != null ? rs.getString("email") : "-",
                    rs.getString("full_name") != null ? rs.getString("full_name") : "-",
                    rs.getString("role"),
                    rs.getInt("total_books_read"),
                    rs.getString("status")
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    public boolean updateUserStats(int userId, int totalBooksRead, int activeBorrowings) {
        String sql = "UPDATE users SET total_books_read=?, active_borrowings=? WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, totalBooksRead);
            pst.setInt(2, activeBorrowings);
            pst.setInt(3, userId);
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user stats: " + e.getMessage());
            return false;
        }
    }
    
    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) as count FROM users";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            return rs.getInt("count");
            
        } catch (SQLException e) {
            System.err.println("Error getting total users: " + e.getMessage());
            return 0;
        }
    }
    
    public int getActiveUsers() {
        String sql = "SELECT COUNT(*) as count FROM users WHERE active_borrowings > 0";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            return rs.getInt("count");
            
        } catch (SQLException e) {
            System.err.println("Error getting active users: " + e.getMessage());
            return 0;
        }
    }
    
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }
        
        // Alphanumeric and underscore only
        return Pattern.matches("^[a-zA-Z0-9_]+$", username);
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        if (password.length() < 6) {
            return false;
        }
        
        // At least one letter and one number
        return Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d).+$", password);
    }
    
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        
        return null;
    }
    
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching user by username: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // First verify old password
        String verifySql = "SELECT password FROM users WHERE id=? AND password=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
            
            verifyStmt.setInt(1, userId);
            verifyStmt.setString(2, oldPassword);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (!rs.next()) {
                return false; // Old password doesn't match
            }
            
            // Update to new password
            String updateSql = "UPDATE users SET password=? WHERE id=?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, userId);
                return updateStmt.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login=CURRENT_TIMESTAMP WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            return false;
        }
    }
    
}