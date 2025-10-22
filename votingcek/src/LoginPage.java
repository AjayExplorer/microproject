import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginPage extends JFrame {
    private JTextField usernameField, studentField;
    private JPasswordField passwordField;
    private JButton adminLogin, studentLogin;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);    // Steel Blue
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color ACCENT_COLOR = new Color(25, 25, 112);      // Midnight Blue
    private static final Color TEXT_COLOR = new Color(44, 62, 80);         // Dark Gray Blue

    public LoginPage() {
        setTitle("College Election Voting System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout(20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(800, 80));
        headerPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("College Election Voting System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setLayout(new GridLayout(1, 2, 20, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Admin Login Panel
        JPanel adminPanel = createLoginPanel("Admin Login", true);
        mainPanel.add(adminPanel);

        // Student Login Panel
        JPanel studentPanel = createLoginPanel("Student Login", false);
        mainPanel.add(studentPanel);

        add(mainPanel, BorderLayout.CENTER);

        adminLogin.addActionListener(e -> adminLogin());
        studentLogin.addActionListener(e -> studentLogin());

        setVisible(true);
    }

    private JPanel createLoginPanel(String title, boolean isAdmin) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 5, 15, 5);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_COLOR);
        panel.add(titleLabel, gbc);

        // Fields
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        if (isAdmin) {
            // Username
            JLabel userLabel = new JLabel("Username:");
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(userLabel, gbc);

            usernameField = new JTextField(15);
            usernameField.setPreferredSize(new Dimension(200, 30));
            panel.add(usernameField, gbc);

            // Password
            JLabel passLabel = new JLabel("Password:");
            passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(passLabel, gbc);

            passwordField = new JPasswordField(15);
            passwordField.setPreferredSize(new Dimension(200, 30));
            panel.add(passwordField, gbc);

            // Login Button
            adminLogin = createStyledButton("Admin Login");
            panel.add(adminLogin, gbc);
        } else {
            // Student ID
            JLabel studentLabel = new JLabel("Student Admission No:");
            studentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(studentLabel, gbc);

            studentField = new JTextField(15);
            studentField.setPreferredSize(new Dimension(200, 30));
            panel.add(studentField, gbc);

            // Login Button
            studentLogin = createStyledButton("Student Login");
            panel.add(studentLogin, gbc);
        }

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private void adminLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM admin WHERE username=? AND password=?")) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                showSuccess("Welcome Admin!");
                new AdminDashboard().setVisible(true);
                dispose();
            } else {
                showError("Invalid credentials!");
            }
        } catch (Exception e) {
            showError("Database error: " + e.getMessage());
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void studentLogin() {
        String id = studentField.getText().trim();
        
        if (id.isEmpty()) {
            showError("Please enter your admission number");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM student WHERE student_id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                if (status.equals("not_voted")) {
                    new StudentVotingPage(id).setVisible(true);
                    dispose();
                } else {
                    showError("You have already voted!");
                }
            } else {
                showError("Invalid Admission Number!");
            }
        } catch (Exception e) {
            showError("Database error: " + e.getMessage());
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}
