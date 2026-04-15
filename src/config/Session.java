package config;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

public class Session {
    private static int userId;
    private static String username;
    private static String role;
    private static String email;
    private static String fullName;
    private static String phone;
    private static String profileImageBase64;
    private static BufferedImage profileImage;
    private static int totalBooksRead = 0;
    private static int activeBorrowings = 0;

    public static void setSession(int id, String user, String r) {
        userId = id;
        username = user;
        role = r;
        loadUserProfile();
    }
    
    public static void setFullProfile(int id, String user, String r, String email, 
                                      String fullName, String phone, String profileImg) {
        userId = id;
        username = user;
        role = r;
        Session.email = email;
        Session.fullName = fullName;
        Session.phone = phone;
        profileImageBase64 = profileImg;
        if (profileImg != null && !profileImg.isEmpty()) {
            profileImage = decodeBase64ToImage(profileImg);
        }
    }
    
    private static void loadUserProfile() {
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            String sql = "SELECT email, full_name, phone, profile_image, total_books_read, active_borrowings FROM users WHERE id=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            java.sql.ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                email = rs.getString("email");
                fullName = rs.getString("full_name");
                phone = rs.getString("phone");
                profileImageBase64 = rs.getString("profile_image");
                totalBooksRead = rs.getInt("total_books_read");
                activeBorrowings = rs.getInt("active_borrowings");
                if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                    profileImage = decodeBase64ToImage(profileImageBase64);
                }
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
        }
    }

    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    public static String getEmail() { return email; }
    public static String getFullName() { return fullName; }
    public static String getPhone() { return phone; }
    public static BufferedImage getProfileImage() { return profileImage; }
    public static String getProfileImageBase64() { return profileImageBase64; }
    public static int getTotalBooksRead() { return totalBooksRead; }
    public static int getActiveBorrowings() { return activeBorrowings; }
    
    public static void setProfileImage(BufferedImage image) {
        profileImage = image;
        profileImageBase64 = encodeImageToBase64(image);
        updateDatabaseProfileImage();
    }
    
    public static void incrementBooksRead() {
        totalBooksRead++;
        updateUserStats();
    }
    
    public static void setActiveBorrowings(int count) {
        activeBorrowings = count;
        updateUserStats();
    }
    
    private static void updateUserStats() {
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            String sql = "UPDATE users SET total_books_read=?, active_borrowings=? WHERE id=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, totalBooksRead);
            pst.setInt(2, activeBorrowings);
            pst.setInt(3, userId);
            pst.executeUpdate();
            pst.close();
        } catch (Exception e) {
            System.err.println("Error updating stats: " + e.getMessage());
        }
    }
    
    private static void updateDatabaseProfileImage() {
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            String sql = "UPDATE users SET profile_image=? WHERE id=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, profileImageBase64);
            pst.setInt(2, userId);
            pst.executeUpdate();
            pst.close();
        } catch (Exception e) {
            System.err.println("Error updating profile image: " + e.getMessage());
        }
    }
    
    private static String encodeImageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }
    
    private static BufferedImage decodeBase64ToImage(String base64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            InputStream is = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(is);
        } catch (Exception e) {
            return null;
        }
    }

    public static void clearSession() {
        userId = 0;
        username = null;
        role = null;
        email = null;
        fullName = null;
        phone = null;
        profileImageBase64 = null;
        profileImage = null;
        totalBooksRead = 0;
        activeBorrowings = 0;
    }
}