/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ui;

import config.DBConnection;
import config.Session;
import dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LoginPanel extends javax.swing.JPanel {
    
private JTextField userField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JButton clearBtn;
    private JLabel statusLabel;
    private UserDAO userDAO;
    private MainFrame frame;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        this.userDAO = new UserDAO();
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));
        
        // Create main panel with GridBagLayout for better control
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Login subtitle
        JLabel subtitleLabel = new JLabel("Login to Your Account");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);
        
        // Spacing
        gbc.gridy = 2;
        mainPanel.add(Box.createVerticalStrut(20), gbc);
        
        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(userLabel, gbc);
        
        userField = new JTextField(20);
        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridx = 1;
        mainPanel.add(userField, gbc);
        
        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(passLabel, gbc);
        
        passField = new JPasswordField(20);
        passField.setFont(new Font("Arial", Font.PLAIN, 14));
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridx = 1;
        mainPanel.add(passField, gbc);
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(statusLabel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        loginBtn = createStyledButton("Login", new Color(70, 130, 180), Color.WHITE);
        registerBtn = createStyledButton("Register", new Color(60, 179, 113), Color.WHITE);
        clearBtn = createStyledButton("Clear", new Color(169, 169, 169), Color.WHITE);
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(clearBtn);
        
        gbc.gridy = 6;
        mainPanel.add(buttonPanel, gbc);
        
        // Demo credentials panel
        JPanel demoPanel = new JPanel();
        demoPanel.setBackground(new Color(255, 255, 224));
        demoPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
        
        JLabel demoLabel = new JLabel("Demo Credentials:");
        demoLabel.setFont(new Font("Arial", Font.BOLD, 11));
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel adminLabel = new JLabel("Admin: admin / admin123");
        adminLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel customerLabel = new JLabel("Customer: john / john123");
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        customerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        demoPanel.add(Box.createVerticalStrut(5));
        demoPanel.add(demoLabel);
        demoPanel.add(adminLabel);
        demoPanel.add(customerLabel);
        demoPanel.add(Box.createVerticalStrut(5));
        
        gbc.gridy = 7;
        mainPanel.add(demoPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Add action listeners
        addActionListeners();
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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
    
    private void addActionListeners() {
        loginBtn.addActionListener(e -> performLogin());
        registerBtn.addActionListener(e -> showRegistrationDialog());
        clearBtn.addActionListener(e -> clearFields());
        
        // Enter key to login
        userField.addActionListener(e -> performLogin());
        passField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();
        
        // Validations
        if (!validateLoginInput(username, password)) {
            return;
        }
        
        // Disable login button during processing
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");
        
        // Perform login in background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return userDAO.authenticateUser(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Login successful
                        if (Session.getRole() != null && Session.getRole().equals("admin")) {
                            frame.showPanel("admin");
                        } else {
                            frame.showPanel("customer");
                        }
                    } else {
                        statusLabel.setText("Invalid username or password!");
                        passField.setText("");
                        JOptionPane.showMessageDialog(LoginPanel.this, 
                            "Invalid username or password!\nPlease try again.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Login error: " + ex.getMessage());
                } finally {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                }
            }
        };
        worker.execute();
    }
    
    private boolean validateLoginInput(String username, String password) {
        if (username.isEmpty()) {
            statusLabel.setText("Username is required!");
            userField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            statusLabel.setText("Password is required!");
            passField.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            statusLabel.setText("Username must be at least 3 characters!");
            return false;
        }
        
        statusLabel.setText(" ");
        return true;
    }
    
    private void showRegistrationDialog() {
        JDialog regDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Account", true);
        regDialog.setLayout(new BorderLayout());
        regDialog.setSize(400, 350);
        regDialog.setLocationRelativeTo(this);
        
        JPanel regPanel = new JPanel(new GridBagLayout());
        regPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        regPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        regPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        regPanel.add(new JLabel("Username:*"), gbc);
        JTextField regUser = new JTextField(15);
        gbc.gridx = 1;
        regPanel.add(regUser, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        regPanel.add(new JLabel("Password:*"), gbc);
        JPasswordField regPass = new JPasswordField(15);
        gbc.gridx = 1;
        regPanel.add(regPass, gbc);
        
        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        regPanel.add(new JLabel("Confirm Password:*"), gbc);
        JPasswordField confirmPass = new JPasswordField(15);
        gbc.gridx = 1;
        regPanel.add(confirmPass, gbc);
        
        // Password strength indicator
        JLabel strengthLabel = new JLabel(" ");
        strengthLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        gbc.gridx = 1;
        gbc.gridy = 4;
        regPanel.add(strengthLabel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton regBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(regBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        regPanel.add(buttonPanel, gbc);
        
        // Password strength checker
        regPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String password = new String(regPass.getPassword());
                int strength = getPasswordStrength(password);
                if (strength <= 2) {
                    strengthLabel.setText("Weak password");
                    strengthLabel.setForeground(Color.RED);
                } else if (strength <= 4) {
                    strengthLabel.setText("Medium password");
                    strengthLabel.setForeground(Color.ORANGE);
                } else {
                    strengthLabel.setText("Strong password");
                    strengthLabel.setForeground(Color.GREEN);
                }
            }
        });
        
        regBtn.addActionListener(e -> {
            String username = regUser.getText().trim();
            String password = new String(regPass.getPassword());
            String confirm = new String(confirmPass.getPassword());
            
            if (validateRegistration(username, password, confirm)) {
                if (userDAO.registerUser(username, password, "customer")) {
                    JOptionPane.showMessageDialog(regDialog, 
                        "Registration successful!\nYou can now login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    regDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(regDialog,
                        "Username already exists!\nPlease choose another username.",
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelBtn.addActionListener(e -> regDialog.dispose());
        
        regDialog.add(regPanel);
        regDialog.setVisible(true);
    }
    
    private boolean validateRegistration(String username, String password, String confirm) {
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username is required!");
            return false;
        }
        
        if (username.length() < 3) {
            JOptionPane.showMessageDialog(null, "Username must be at least 3 characters!");
            return false;
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            JOptionPane.showMessageDialog(null, "Username can only contain letters, numbers, and underscore!");
            return false;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Password is required!");
            return false;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(null, "Password must be at least 6 characters!");
            return false;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match!");
            return false;
        }
        
        return true;
    }
    
    private int getPasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()].*")) strength++;
        return strength;
    }
    
    private void clearFields() {
        userField.setText("");
        passField.setText("");
        statusLabel.setText(" ");
        userField.requestFocus();
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
