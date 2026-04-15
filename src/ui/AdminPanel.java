

package ui;

import config.Session;
import dao.BookDAO;
import dao.UserDAO;
import dao.StatisticsDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class AdminPanel extends javax.swing.JPanel {
    
  private JTabbedPane tabbedPane;
    private BookDAO bookDAO;
    private UserDAO userDAO;
    private StatisticsDAO statsDAO;
    private MainFrame frame;
    private JLabel statusLabel;
    
    // Components
    private JTable booksTable, usersTable, historyTable;
    private DefaultTableModel booksModel, usersModel, historyModel;
    private JTextField titleField, authorField, isbnField, yearField, publisherField;
    private JComboBox<String> categoryBox;
    private JTextArea descriptionArea;
    private JLabel statsLabel, revenueLabel, activeUsersLabel;
    private Timer refreshTimer;
    private JPanel statsPanel;
    private JComboBox<String> reportTypeBox;
    
    public AdminPanel(MainFrame frame) {
        this.frame = frame;
        this.bookDAO = new BookDAO();
        this.userDAO = new UserDAO();
        this.statsDAO = new StatisticsDAO();
        initUI();
        loadAllData();
        startAutoRefresh();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        
        tabbedPane.addTab("📚 Book Management", createBookManagementPanel());
        tabbedPane.addTab("📊 Dashboard & Statistics", createStatisticsPanel());
        tabbedPane.addTab("👥 User Management", createUserManagementPanel());
        tabbedPane.addTab("📜 Borrowing History", createHistoryPanel());
        tabbedPane.addTab("⚙️ System Settings", createSettingsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📖 Library Management System - Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + Session.getUsername() + " (Administrator)");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        welcomeLabel.setForeground(Color.LIGHT_GRAY);
        
        JButton logoutBtn = createStyledButton("Logout", new Color(220, 20, 60), Color.WHITE);
        logoutBtn.addActionListener(e -> logout());
        
        userPanel.add(welcomeLabel);
        userPanel.add(logoutBtn);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Book List Table
        booksModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "ISBN", "Category", "Status", "Times Borrowed"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        booksTable = new JTable(booksModel);
        booksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        booksTable.setRowHeight(25);
        booksTable.getSelectionModel().addListSelectionListener(e -> loadBookDetails());
        
        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Book Inventory"));
        
        // Book Form
        JPanel formPanel = createBookFormPanel();
        
        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, formPanel);
        splitPane.setResizeWeight(0.6);
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBookFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Book Management"));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:*"), gbc);
        titleField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Author:*"), gbc);
        authorField = new JTextField(20);
        gbc.gridx = 3;
        formPanel.add(authorField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("ISBN:"), gbc);
        isbnField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(isbnField, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        categoryBox = new JComboBox<>(new String[]{"Fiction", "Non-Fiction", "Science", "Technology", "History", "Arts"});
        gbc.gridx = 3;
        formPanel.add(categoryBox, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Publication Year:"), gbc);
        yearField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(yearField, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Publisher:"), gbc);
        publisherField = new JTextField(20);
        gbc.gridx = 3;
        formPanel.add(publisherField, gbc);
        
        // Row 4
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        formPanel.add(descScroll, gbc);
        
        // Row 5 - Buttons
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addBtn = createStyledButton("➕ Add Book", new Color(60, 179, 113), Color.WHITE);
        JButton updateBtn = createStyledButton("✏️ Update Book", new Color(255, 140, 0), Color.WHITE);
        JButton deleteBtn = createStyledButton("🗑️ Delete Book", new Color(220, 20, 60), Color.WHITE);
        JButton refreshBtn = createStyledButton("🔄 Refresh", new Color(70, 130, 180), Color.WHITE);
        
        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        refreshBtn.addActionListener(e -> loadBooks());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        
        gbc.gridx = 0;
        formPanel.add(buttonPanel, gbc);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        gbc.gridy = 5;
        formPanel.add(statusLabel, gbc);
        
        return formPanel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Stats Cards
        JPanel statsCardsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsCardsPanel.setOpaque(false);
        
        statsCardsPanel.add(createStatCard("📚 Total Books", String.valueOf(statsDAO.getTotalBooks()), new Color(52, 152, 219)));
        statsCardsPanel.add(createStatCard("📖 Available Books", String.valueOf(statsDAO.getAvailableBooks()), new Color(46, 204, 113)));
        statsCardsPanel.add(createStatCard("🔒 Borrowed Books", String.valueOf(statsDAO.getBorrowedBooks()), new Color(231, 76, 60)));
        statsCardsPanel.add(createStatCard("👥 Total Users", String.valueOf(statsDAO.getTotalUsers()), new Color(155, 89, 182)));
        statsCardsPanel.add(createStatCard("📊 Active Borrowings", String.valueOf(statsDAO.getActiveBorrowings()), new Color(241, 196, 15)));
        statsCardsPanel.add(createStatCard("✅ Books Returned", String.valueOf(statsDAO.getTotalReturns()), new Color(52, 152, 219)));
        statsCardsPanel.add(createStatCard("⭐ Average Rating", String.format("%.1f", statsDAO.getAverageRating()), new Color(230, 126, 34)));
        statsCardsPanel.add(createStatCard("📈 Total Revenue", String.format("$%.2f", statsDAO.getTotalFineCollected()), new Color(39, 174, 96)));
        
        // Category Stats Panel (replacing charts)
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Books by Category"));
        categoryPanel.setBackground(Color.WHITE);
        
        DefaultTableModel categoryModel = new DefaultTableModel(new String[]{"Category", "Number of Books"}, 0);
        JTable categoryTable = new JTable(categoryModel);
        categoryTable.setFont(new Font("Arial", Font.PLAIN, 12));
        categoryTable.setRowHeight(25);
        
        Map<String, Integer> categoryStats = statsDAO.getBooksByCategory();
        for (Map.Entry<String, Integer> entry : categoryStats.entrySet()) {
            categoryModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        categoryPanel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
        
        // Top Books Panel
        JPanel topBooksPanel = createTopBooksPanel();
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsCardsPanel, categoryPanel);
        splitPane.setResizeWeight(0.4);
        
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(topBooksPanel, BorderLayout.SOUTH);
        
        // Update stats periodically
        new Timer(5000, e -> updateStatistics(statsCardsPanel)).start();
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTopBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📈 Most Borrowed Books"));
        
        DefaultTableModel topBooksModel = new DefaultTableModel(new String[]{"Rank", "Title", "Author", "Times Borrowed", "Rating"}, 0);
        JTable topBooksTable = new JTable(topBooksModel);
        topBooksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        topBooksTable.setRowHeight(25);
        
        java.util.List<Object[]> topBooks = statsDAO.getMostBorrowedBooks(10);
        for (int i = 0; i < topBooks.size(); i++) {
            Object[] book = topBooks.get(i);
            topBooksModel.addRow(new Object[]{i + 1, book[0], book[1], book[2], book[3]});
        }
        
        panel.add(new JScrollPane(topBooksTable), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        usersModel = new DefaultTableModel(new String[]{"ID", "Username", "Email", "Full Name", "Role", "Books Read", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(usersModel);
        usersTable.setFont(new Font("Arial", Font.PLAIN, 12));
        usersTable.setRowHeight(25);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton refreshUsersBtn = createStyledButton("🔄 Refresh", new Color(70, 130, 180), Color.WHITE);
        JButton deleteUserBtn = createStyledButton("🗑️ Delete User", new Color(220, 20, 60), Color.WHITE);
        JButton promoteUserBtn = createStyledButton("⭐ Promote to Admin", new Color(255, 140, 0), Color.WHITE);
        
        refreshUsersBtn.addActionListener(e -> loadUsers());
        deleteUserBtn.addActionListener(e -> deleteUser());
        promoteUserBtn.addActionListener(e -> promoteUser());
        
        buttonPanel.add(refreshUsersBtn);
        buttonPanel.add(deleteUserBtn);
        buttonPanel.add(promoteUserBtn);
        
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        historyModel = new DefaultTableModel(new String[]{"ID", "User", "Book", "Borrowed Date", "Due Date", "Returned Date", "Status", "Fine"}, 0);
        historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 12));
        historyTable.setRowHeight(25);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Status:"));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Active", "Returned", "Overdue"});
        statusFilter.addActionListener(e -> loadHistory((String) statusFilter.getSelectedItem()));
        
        filterPanel.add(statusFilter);
        
        JButton refreshHistoryBtn = createStyledButton("🔄 Refresh", new Color(70, 130, 180), Color.WHITE);
        refreshHistoryBtn.addActionListener(e -> loadHistory((String) statusFilter.getSelectedItem()));
        filterPanel.add(refreshHistoryBtn);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Database Settings
        JPanel dbPanel = new JPanel(new GridBagLayout());
        dbPanel.setBorder(BorderFactory.createTitledBorder("Database Settings"));
        dbPanel.setBackground(Color.WHITE);
        
        GridBagConstraints dbGbc = new GridBagConstraints();
        dbGbc.insets = new Insets(5, 10, 5, 10);
        dbGbc.fill = GridBagConstraints.HORIZONTAL;
        
        dbGbc.gridx = 0; dbGbc.gridy = 0;
        dbPanel.add(new JLabel("Auto Backup:"), dbGbc);
        JCheckBox autoBackup = new JCheckBox("Enable Automatic Backup");
        autoBackup.setSelected(true);
        dbGbc.gridx = 1;
        dbPanel.add(autoBackup, dbGbc);
        
        dbGbc.gridx = 0; dbGbc.gridy = 1;
        dbPanel.add(new JLabel("Backup Interval:"), dbGbc);
        JSpinner backupInterval = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
        dbGbc.gridx = 1;
        dbPanel.add(backupInterval, dbGbc);
        dbPanel.add(new JLabel("hours"), dbGbc);
        
        // System Settings
        JPanel sysPanel = new JPanel(new GridBagLayout());
        sysPanel.setBorder(BorderFactory.createTitledBorder("System Settings"));
        sysPanel.setBackground(Color.WHITE);
        
        GridBagConstraints sysGbc = new GridBagConstraints();
        sysGbc.insets = new Insets(5, 10, 5, 10);
        sysGbc.fill = GridBagConstraints.HORIZONTAL;
        
        sysGbc.gridx = 0; sysGbc.gridy = 0;
        sysPanel.add(new JLabel("Loan Duration:"), sysGbc);
        JSpinner loanDuration = new JSpinner(new SpinnerNumberModel(14, 1, 60, 1));
        sysGbc.gridx = 1;
        sysPanel.add(loanDuration, sysGbc);
        sysPanel.add(new JLabel("days"), sysGbc);
        
        sysGbc.gridx = 0; sysGbc.gridy = 1;
        sysPanel.add(new JLabel("Fine per Day:"), sysGbc);
        JSpinner fineAmount = new JSpinner(new SpinnerNumberModel(0.50, 0, 5, 0.25));
        sysGbc.gridx = 1;
        sysPanel.add(fineAmount, sysGbc);
        sysPanel.add(new JLabel("$"), sysGbc);
        
        // Save Button
        JButton saveSettings = createStyledButton("💾 Save Settings", new Color(60, 179, 113), Color.WHITE);
        saveSettings.addActionListener(e -> {
            statsDAO.updateSystemSettings((int) loanDuration.getValue(), (double) fineAmount.getValue());
            JOptionPane.showMessageDialog(this, "Settings saved successfully!");
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(dbPanel, gbc);
        gbc.gridy = 1;
        panel.add(sysPanel, gbc);
        gbc.gridy = 2;
        panel.add(saveSettings, gbc);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(240, 248, 255));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel versionLabel = new JLabel("Version 3.0 | © 2024 Library Management System");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        versionLabel.setForeground(Color.GRAY);
        
        JLabel timeLabel = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        new Timer(1000, e -> timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))).start();
        
        footerPanel.add(versionLabel, BorderLayout.WEST);
        footerPanel.add(timeLabel, BorderLayout.EAST);
        
        return footerPanel;
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
    
    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        
        if (title.isEmpty() || author.isEmpty()) {
            showStatus("Title and Author are required!", Color.RED);
            return;
        }
        
        if (bookDAO.addBook(title, author, (String) categoryBox.getSelectedItem(), 
                           isbnField.getText().trim(), yearField.getText().trim(), 
                           publisherField.getText().trim(), descriptionArea.getText().trim())) {
            showStatus("Book added successfully!", new Color(60, 179, 113));
            clearBookForm();
            loadBooks();
        } else {
            showStatus("Failed to add book!", Color.RED);
        }
    }
    
    private void updateBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            showStatus("Please select a book to update!", Color.RED);
            return;
        }
        
        int bookId = (int) booksModel.getValueAt(selectedRow, 0);
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        
        if (bookDAO.updateBook(bookId, title, author, (String) categoryBox.getSelectedItem(),
                               isbnField.getText().trim(), yearField.getText().trim(),
                               publisherField.getText().trim(), descriptionArea.getText().trim())) {
            showStatus("Book updated successfully!", new Color(60, 179, 113));
            loadBooks();
        } else {
            showStatus("Failed to update book!", Color.RED);
        }
    }
    
    private void deleteBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            showStatus("Please select a book to delete!", Color.RED);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?", 
                                                   "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int bookId = (int) booksModel.getValueAt(selectedRow, 0);
            if (bookDAO.deleteBook(bookId)) {
                showStatus("Book deleted successfully!", new Color(60, 179, 113));
                loadBooks();
                clearBookForm();
            }
        }
    }
    
    private void loadBooks() {
        bookDAO.loadBooks(booksModel);
    }
    
    private void loadUsers() {
        userDAO.loadUsers(usersModel);
    }
    
    private void loadHistory(String status) {
        statsDAO.loadBorrowingHistory(historyModel, status);
    }
    
    private void loadBookDetails() {
        int row = booksTable.getSelectedRow();
        if (row != -1) {
            titleField.setText((String) booksModel.getValueAt(row, 1));
            authorField.setText((String) booksModel.getValueAt(row, 2));
            isbnField.setText((String) booksModel.getValueAt(row, 3));
            categoryBox.setSelectedItem(booksModel.getValueAt(row, 4));
        }
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!");
            return;
        }
        
        int userId = (int) usersModel.getValueAt(selectedRow, 0);
        String username = (String) usersModel.getValueAt(selectedRow, 1);
        
        if (username.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Cannot delete the main admin account!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user '" + username + "'?", 
                                                   "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && userDAO.deleteUser(userId)) {
            loadUsers();
        }
    }
    
    private void promoteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int userId = (int) usersModel.getValueAt(selectedRow, 0);
        if (userDAO.promoteToAdmin(userId)) {
            loadUsers();
            JOptionPane.showMessageDialog(this, "User promoted to Admin successfully!");
        }
    }
    
    private void updateStatistics(JPanel statsCardsPanel) {
        Component[] cards = statsCardsPanel.getComponents();
        if (cards.length >= 8) {
            ((JLabel) ((JPanel) cards[0]).getComponent(1)).setText(String.valueOf(statsDAO.getTotalBooks()));
            ((JLabel) ((JPanel) cards[1]).getComponent(1)).setText(String.valueOf(statsDAO.getAvailableBooks()));
            ((JLabel) ((JPanel) cards[2]).getComponent(1)).setText(String.valueOf(statsDAO.getBorrowedBooks()));
            ((JLabel) ((JPanel) cards[3]).getComponent(1)).setText(String.valueOf(statsDAO.getTotalUsers()));
            ((JLabel) ((JPanel) cards[4]).getComponent(1)).setText(String.valueOf(statsDAO.getActiveBorrowings()));
            ((JLabel) ((JPanel) cards[5]).getComponent(1)).setText(String.valueOf(statsDAO.getTotalReturns()));
            ((JLabel) ((JPanel) cards[6]).getComponent(1)).setText(String.format("%.1f", statsDAO.getAverageRating()));
            ((JLabel) ((JPanel) cards[7]).getComponent(1)).setText(String.format("$%.2f", statsDAO.getTotalFineCollected()));
        }
    }
    
    private void loadAllData() {
        loadBooks();
        loadUsers();
        loadHistory("All");
    }
    
    private void clearBookForm() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        yearField.setText("");
        publisherField.setText("");
        descriptionArea.setText("");
        categoryBox.setSelectedIndex(0);
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        new Timer(3000, e -> statusLabel.setText(" ")).start();
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> {
            loadBooks();
            loadUsers();
        });
        refreshTimer.start();
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", 
                                                   "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (refreshTimer != null) refreshTimer.stop();
            Session.clearSession();
            frame.showPanel("login");
        }
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
