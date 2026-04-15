/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ui;

import config.DBConnection;
import config.Session;
import dao.BookDAO;
import dao.StatisticsDAO;
import dao.UserDAO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class CustomerPanel extends javax.swing.JPanel {
   
   private DefaultTableModel model;
    private JTable table;
    private JLabel statusLabel;
    private JLabel welcomeLabel;
    private JLabel statsLabel;
    private BookDAO bookDAO;
    private MainFrame frame;
    private Timer refreshTimer;
private StatisticsDAO statsDAO = new StatisticsDAO();
private UserDAO userDAO = new UserDAO();

    public CustomerPanel(MainFrame frame) {
        this.frame = frame;
        this.bookDAO = new BookDAO();
        initUI();
        loadBooks();
        startAutoRefresh();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 139, 87));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Customer Dashboard - Browse Books");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        welcomeLabel = new JLabel("Welcome, " + Session.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        welcomeLabel.setForeground(Color.LIGHT_GRAY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(welcomeLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Table panel
        model = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Status", "Due Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(new Color(144, 238, 144));
        table.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(new Color(46, 139, 87));
        header.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "Available Books",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
        ));
        add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(new Color(240, 248, 255));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        JButton borrowBtn = createStyledButton("Borrow Selected Book", new Color(255, 140, 0), Color.WHITE);
        JButton refreshBtn = createStyledButton("Refresh List", new Color(70, 130, 180), Color.WHITE);
        JButton myBooksBtn = createStyledButton("My Borrowed Books", new Color(147, 112, 219), Color.WHITE);
        JButton logoutBtn = createStyledButton("Logout", new Color(169, 169, 169), Color.WHITE);
        
        buttonPanel.add(borrowBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(myBooksBtn);
        buttonPanel.add(logoutBtn);
        
        // Stats label
        statsLabel = new JLabel(" ");
        statsLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statsLabel.setForeground(new Color(46, 139, 87));
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.RED);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statusPanel.setBackground(new Color(240, 248, 255));
        statusPanel.add(statsLabel);
        statusPanel.add(statusLabel);
        
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Action listeners
        borrowBtn.addActionListener(e -> borrowBook());
        refreshBtn.addActionListener(e -> loadBooks());
        myBooksBtn.addActionListener(e -> showMyBooks());
        logoutBtn.addActionListener(e -> logout());
        
        // Update stats
        updateStatistics();
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void borrowBook() {
        int selectedRow = table.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a book to borrow!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String status = (String) model.getValueAt(selectedRow, 3);
        if (!"Available".equals(status)) {
            JOptionPane.showMessageDialog(this,
                "This book is already borrowed!\nPlease select another book.",
                "Not Available",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookId = (int) model.getValueAt(selectedRow, 0);
        String bookTitle = (String) model.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Do you want to borrow:\n'" + bookTitle + "'?\n\nYou have 14 days to return it.",
            "Confirm Borrow",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (bookDAO.borrowBook(bookId, Session.getUserId(), Session.getUsername())) {
                statusLabel.setText("Book borrowed successfully! Due date: 14 days from now");
                statusLabel.setForeground(new Color(60, 179, 113));
                loadBooks();
                updateStatistics();
                
                new Timer(5000, e -> statusLabel.setText(" ")).start();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to borrow book!\nIt might have been borrowed by someone else.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadBooks() {
        model.setRowCount(0);
        String sql = "SELECT id, title, author, status, due_date FROM books ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                String dueDate = rs.getString("due_date");
                if (dueDate == null) dueDate = "-";
                else if ("Borrowed".equals(rs.getString("status"))) {
                    // Highlight overdue books
                    if (isOverdue(dueDate)) {
                        dueDate = "OVERDUE! " + dueDate;
                    }
                }
                
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("status"),
                    dueDate
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
    }
    
    private boolean isOverdue(String dueDate) {
        try {
            Date due = Date.valueOf(dueDate);
            Date today = new Date(System.currentTimeMillis());
            return today.after(due);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void showMyBooks() {
        JDialog myBooksDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "My Borrowed Books", true);
        myBooksDialog.setSize(600, 400);
        myBooksDialog.setLocationRelativeTo(this);
        
        DefaultTableModel myBooksModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Borrowed Date", "Due Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable myBooksTable = new JTable(myBooksModel);
        myBooksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        myBooksTable.setRowHeight(25);
        
        String sql = "SELECT id, title, author, borrowed_date, due_date, status FROM books WHERE borrowed_by=? AND status='Borrowed'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, Session.getUserId());
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String status = "Active";
                String dueDate = rs.getString("due_date");
                if (isOverdue(dueDate)) {
                    status = "OVERDUE!";
                }
                
                myBooksModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("borrowed_date"),
                    dueDate,
                    status
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading borrowed books: " + e.getMessage());
        }
        
        JPanel buttonPanel = new JPanel();
        JButton returnBtn = new JButton("Return Selected Book");
        JButton closeBtn = new JButton("Close");
        
        returnBtn.addActionListener(e -> {
            int row = myBooksTable.getSelectedRow();
            if (row != -1) {
                int bookId = (int) myBooksModel.getValueAt(row, 0);
                String bookTitle = (String) myBooksModel.getValueAt(row, 1);
                
                int confirm = JOptionPane.showConfirmDialog(myBooksDialog,
                    "Return book:\n'" + bookTitle + "'?",
                    "Confirm Return",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (bookDAO.returnBook(bookId)) {
                        JOptionPane.showMessageDialog(myBooksDialog,
                            "Book returned successfully!");
                        myBooksDialog.dispose();
                        loadBooks();
                        updateStatistics();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(myBooksDialog,
                    "Please select a book to return!");
            }
        });
        
        buttonPanel.add(returnBtn);
        buttonPanel.add(closeBtn);
        closeBtn.addActionListener(e -> myBooksDialog.dispose());
        
        myBooksDialog.setLayout(new BorderLayout());
        myBooksDialog.add(new JScrollPane(myBooksTable), BorderLayout.CENTER);
        myBooksDialog.add(buttonPanel, BorderLayout.SOUTH);
        myBooksDialog.setVisible(true);
    }
    
    private void updateStatistics() {
        int available = bookDAO.getAvailableBooks();
        int borrowed = bookDAO.getBorrowedBooks();
        statsLabel.setText(String.format("📚 Available: %d  |  📖 Borrowed: %d", available, borrowed));
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> loadBooks()); // Refresh every 30 seconds
        refreshTimer.start();
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            Session.clearSession();
            frame.showPanel("login");
        }
    }
// Add these methods to CustomerPanel.java

private void showProfileDialog() {
    JDialog profileDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "My Profile", true);
    profileDialog.setSize(500, 600);
    profileDialog.setLocationRelativeTo(this);
    profileDialog.setLayout(new BorderLayout());
    
    JTabbedPane profileTabs = new JTabbedPane();
    profileTabs.addTab("👤 Profile Info", createProfileInfoPanel(profileDialog));
    profileTabs.addTab("🖼️ Change Avatar", createAvatarSelectionPanel(profileDialog));
    profileTabs.addTab("📊 My Statistics", createUserStatisticsPanel());
    profileTabs.addTab("📚 Reading History", createReadingHistoryPanel());
    
    profileDialog.add(profileTabs, BorderLayout.CENTER);
    
    JButton closeBtn = new JButton("Close");
    closeBtn.addActionListener(e -> profileDialog.dispose());
    JPanel btnPanel = new JPanel();
    btnPanel.add(closeBtn);
    profileDialog.add(btnPanel, BorderLayout.SOUTH);
    
    profileDialog.setVisible(true);
}

private JPanel createProfileInfoPanel(JDialog dialog) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    // Profile Image
    JLabel profileImageLabel = new JLabel();
    profileImageLabel.setPreferredSize(new Dimension(100, 100));
    profileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    if (Session.getProfileImage() != null) {
        ImageIcon icon = new ImageIcon(Session.getProfileImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        profileImageLabel.setIcon(icon);
    } else {
        profileImageLabel.setText("No Image");
    }
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    panel.add(profileImageLabel, gbc);
    
    // Form Fields
    String[][] fields = {
        {"Username:", Session.getUsername()},
        {"Full Name:", Session.getFullName() != null ? Session.getFullName() : ""},
        {"Email:", Session.getEmail() != null ? Session.getEmail() : ""},
        {"Phone:", Session.getPhone() != null ? Session.getPhone() : ""},
        {"Role:", Session.getRole()},
        {"Member Since:", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())}
    };
    
    JTextField fullNameField = new JTextField(20);
    JTextField emailField = new JTextField(20);
    JTextField phoneField = new JTextField(20);
    
    for (int i = 0; i < fields.length; i++) {
        gbc.gridy = i + 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel(fields[i][0]), gbc);
        gbc.gridx = 1;
        
        if (fields[i][0].equals("Full Name:")) {
            fullNameField.setText(fields[i][1]);
            panel.add(fullNameField, gbc);
        } else if (fields[i][0].equals("Email:")) {
            emailField.setText(fields[i][1]);
            panel.add(emailField, gbc);
        } else if (fields[i][0].equals("Phone:")) {
            phoneField.setText(fields[i][1]);
            panel.add(phoneField, gbc);
        } else {
            JLabel valueLabel = new JLabel(fields[i][1]);
            valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
            panel.add(valueLabel, gbc);
        }
    }
    
    // Save Button
JButton saveBtn = new JButton("Save Changes");
saveBtn.setBackground(new Color(60, 179, 113));
saveBtn.setForeground(Color.WHITE); // Fixed: Changed from "new WHITE()" to "Color.WHITE"
saveBtn.setFocusPainted(false);
saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

saveBtn.addActionListener(e -> {
    if (userDAO.updateUserProfile(Session.getUserId(), fullNameField.getText(),
        emailField.getText(), phoneField.getText())) {
        JOptionPane.showMessageDialog(dialog, "Profile updated successfully!");
        Session.setFullProfile(Session.getUserId(), Session.getUsername(), Session.getRole(),
            emailField.getText(), fullNameField.getText(),
            phoneField.getText(), Session.getProfileImageBase64());
        dialog.dispose();
    } else {
        JOptionPane.showMessageDialog(dialog, "Failed to update profile!", "Error", JOptionPane.ERROR_MESSAGE);
    }
});
    
    gbc.gridy = fields.length + 1;
    panel.add(saveBtn, gbc);
    
    return panel;
}


private JPanel createAvatarSelectionPanel(JDialog dialog) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Predefined avatars
    String[] avatarColors = {"FF6B6B", "4ECDC4", "45B7D1", "96CEB4", "FFEAA7", "DDA0DD", "98D8C8", "F7DC6F"};
    String[] avatarNames = {"Red", "Teal", "Blue", "Green", "Yellow", "Purple", "Mint", "Gold"};
    
    JPanel avatarsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
    
    for (int i = 0; i < avatarColors.length; i++) {
        JPanel avatarCard = createAvatarCard(avatarColors[i], avatarNames[i], dialog);
        avatarsPanel.add(avatarCard);
    }
    
    // Upload custom image
    JPanel uploadPanel = new JPanel();
    JButton uploadBtn = new JButton("📁 Upload Custom Image");
    uploadBtn.addActionListener(e -> uploadCustomImage(dialog));
    uploadPanel.add(uploadBtn);
    
    panel.add(avatarsPanel, BorderLayout.CENTER);
    panel.add(uploadPanel, BorderLayout.SOUTH);
    
    return panel;
}

private JPanel createAvatarCard(String colorHex, String name, JDialog dialog) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    card.setBackground(Color.WHITE);
    
    JLabel avatarLabel = new JLabel();
    avatarLabel.setPreferredSize(new Dimension(80, 80));
    avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
    avatarLabel.setOpaque(true);
    avatarLabel.setBackground(Color.decode("#" + colorHex));
    avatarLabel.setText(name.substring(0, 1));
    avatarLabel.setFont(new Font("Arial", Font.BOLD, 36));
    avatarLabel.setForeground(Color.WHITE);
    
    JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
    
    card.add(avatarLabel, BorderLayout.CENTER);
    card.add(nameLabel, BorderLayout.SOUTH);
    
    card.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            // Create colored avatar image
            BufferedImage avatar = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = avatar.createGraphics();
            g2d.setColor(Color.decode("#" + colorHex));
            g2d.fillOval(0, 0, 200, 200);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 100));
            FontMetrics fm = g2d.getFontMetrics();
            String letter = name.substring(0, 1);
            int x = (200 - fm.stringWidth(letter)) / 2;
            int y = ((200 - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(letter, x, y);
            g2d.dispose();
            
            Session.setProfileImage(avatar);
            JOptionPane.showMessageDialog(dialog, "Avatar updated successfully!");
            dialog.dispose();
            refreshProfileDisplay();
        }
    });
    
    return card;
}

private void uploadCustomImage(JDialog dialog) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
        "Image Files", "jpg", "jpeg", "png", "gif"));
    
    if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
        try {
            BufferedImage image = ImageIO.read(fileChooser.getSelectedFile());
            // Resize image
            Image scaledImage = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            
            Session.setProfileImage(resizedImage);
            JOptionPane.showMessageDialog(dialog, "Profile image updated!");
            dialog.dispose();
            refreshProfileDisplay();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading image!");
        }
    }
}

private JPanel createUserStatisticsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    String[][] stats = {
        {"📚 Total Books Read", String.valueOf(Session.getTotalBooksRead())},
        {"📖 Currently Borrowed", String.valueOf(Session.getActiveBorrowings())},
        {"⭐ Average Rating", "4.5/5"},
        {"🏆 Reading Streak", "15 days"},
        {"📅 Member Since", "2024"},
        {"🎯 Completion Rate", "85%"}
    };
    
    for (int i = 0; i < stats.length; i++) {
        gbc.gridy = i;
        gbc.gridx = 0;
        JLabel label = new JLabel(stats[i][0]);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        JLabel value = new JLabel(stats[i][1]);
        value.setFont(new Font("Arial", Font.PLAIN, 14));
        value.setForeground(new Color(46, 139, 87));
        panel.add(value, gbc);
    }
    
    // Progress Bar
    gbc.gridy = stats.length;
    gbc.gridwidth = 2;
    panel.add(new JLabel("Reading Progress:"), gbc);
    
    gbc.gridy = stats.length + 1;
    JProgressBar progressBar = new JProgressBar(0, 100);
    progressBar.setValue(65);
    progressBar.setStringPainted(true);
    progressBar.setForeground(new Color(46, 139, 87));
    panel.add(progressBar, gbc);
    
    return panel;
}

private JPanel createReadingHistoryPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    DefaultTableModel historyModel = new DefaultTableModel(
        new String[]{"Book Title", "Author", "Borrowed Date", "Returned Date", "Rating"}, 0);
    JTable historyTable = new JTable(historyModel);
    
    // Load reading history from database
    statsDAO.loadUserReadingHistory(Session.getUserId(), historyModel);
    
    panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
    
    // Rating panel for returned books
    JPanel ratingPanel = new JPanel();
    JButton rateBtn = new JButton("⭐ Rate Books");
    rateBtn.addActionListener(e -> showRatingDialog());
    ratingPanel.add(rateBtn);
    panel.add(ratingPanel, BorderLayout.SOUTH);
    
    return panel;
}


private void showRatingDialog() {
    // Implementation for rating books
    JOptionPane.showMessageDialog(this, "Rating feature coming soon!");
}

private void refreshProfileDisplay() {
    welcomeLabel.setText("Welcome, " + Session.getUsername());
    // Update any other profile displays
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
