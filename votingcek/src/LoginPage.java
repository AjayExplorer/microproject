import java.awt.*;
import java.sql.*;
import javax.swing.*;
public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton superAdminLogin; //removed student and admin fields keep it only for super admin
    private JComboBox<String> roleBox; // Role selection dropdown

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(25, 25, 112);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    public LoginPage() {
        setTitle("College Election Voting System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
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
        mainPanel.setLayout(new GridLayout(1, 3, 20, 0)); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Super Admin Login Panel 🔹
        JPanel superAdminPanel = createLoginPanel("Super Admin Login", "super");
        mainPanel.add(superAdminPanel);
    

        add(mainPanel, BorderLayout.CENTER);

        // Button Actions
        superAdminLogin.addActionListener(e -> handleLogin()); // 🔹 Added action
        setVisible(true);
    }

    private JPanel createLoginPanel(String title, String role) {
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

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_COLOR);
        panel.add(titleLabel, gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        if (role.equals("super")) {
            JLabel userLabel = new JLabel("Username:");
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(userLabel, gbc);

            usernameField = new JTextField(15);
            usernameField.setPreferredSize(new Dimension(200, 30));
            panel.add(usernameField, gbc);

            JLabel passLabel = new JLabel("Password:");
            passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(passLabel, gbc);

            passwordField = new JPasswordField(15);
            passwordField.setPreferredSize(new Dimension(200, 30));
            panel.add(passwordField, gbc);
            JLabel roleLabel = new JLabel("Select Role:");
            roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(roleLabel, gbc);

            roleBox = new JComboBox<>(new String[] {"Admin", "Super_Admin"});
            roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(roleBox, gbc);

            superAdminLogin = createStyledButton("Enter");
            panel.add(superAdminLogin, gbc);
            }
            return panel;
    }
// changed button style
    private JButton createStyledButton(String text) {
    JButton button = new JButton(text);

    // Force solid blue background and white text
    button.setBackground(new Color(70, 130, 180)); // Steel blue
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Segoe UI", Font.BOLD, 14));

    // Disable default LAF (Look and Feel) fade / highlight effects
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(true);
    button.setOpaque(true);


    // Make sure it's always visible and enabled
    button.setEnabled(true);
    button.setVisible(true);

    return button;
}
// 🔹 Combined Login Logic with Role Selection
private void handleLogin() {
    String user = usernameField.getText().trim();
    String pass = new String(passwordField.getPassword()).trim();
    String role = roleBox.getSelectedItem().toString();

    if (role.equals("Super_Admin")) {
        if (user.equals("ramu") && pass.equals("ramu")) {
            showSuccess("Welcome Super Admin!");
            new SuperAdminDashboard().setVisible(true);
            dispose();
        } else {
            showError("Invalid Super Admin credentials!");
        }
    } else if (role.equals("Admin")) {
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please enter username and password for admin login.");
            return;
        }

        // Authenticate admin against the database with clearer diagnostics
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT password FROM admin WHERE username = ?")) {

            ps.setString(1, user);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showError("No admin account found for username: '" + user + "'.\n" +
                              "Create one using the SQL in README or via Super_Admin dashboard.");
                    return;
                }

                String dbPass = rs.getString("password");
                if (dbPass == null) dbPass = "";

                if (dbPass.equals(pass)) {
                    showSuccess("Welcome Admin!");
                    new AdminDashboard().setVisible(true);
                    dispose();
                } else {
                    showError("Incorrect password for user '" + user + "'.\n" +
                              "If you forgot the password, reset it from Super_Admin dashboard or update it in the database.");
                }
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage() + "\nPlease ensure MySQL is running and the 'college_voting' database exists.");
        }
    }
}

    // 🔹 Super Admin Login Logic
    private void superAdminLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.equals("ramu") && pass.equals("ramu")) {
            showSuccess("Welcome Super Admin!");
            new SuperAdminDashboard().setVisible(true);
            dispose();
        } else {
            showError("Invalid Super Admin credentials!");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("LookAndFeel error: " + e.getMessage());
        }

        SwingUtilities.invokeLater(LoginPage::new);
    }
}