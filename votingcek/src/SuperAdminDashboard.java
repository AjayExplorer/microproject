import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

public class SuperAdminDashboard extends JFrame {
    private JTextField adminUserField, studentIdField;
    private JPasswordField adminPassField;
    private JComboBox<String> adminAssignBox;
    private JButton addAdminBtn, addStudentBtn, viewAdminsBtn, viewStudentsBtn;
    private JTable dataTable;

    // Colors (same as LoginPage)
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(25, 25, 112);

    public SuperAdminDashboard() {
        setTitle("Super Admin Dashboard - College Voting System");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Super Admin Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setOpaque(true);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(900, 70));
        add(header, BorderLayout.NORTH);

        // Left Panel (Admin and Student controls)
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Section Title: Add Admin
        JLabel adminTitle = new JLabel("Add New Admin");
        adminTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        adminTitle.setForeground(ACCENT_COLOR);
        controlPanel.add(adminTitle, gbc);

        // Username
        adminUserField = new JTextField(15);
        controlPanel.add(new JLabel("Username:"), gbc);
        controlPanel.add(adminUserField, gbc);

        // Password
        adminPassField = new JPasswordField(15);
        controlPanel.add(new JLabel("Password:"), gbc);
        controlPanel.add(adminPassField, gbc);

        addAdminBtn = createButton("Add Admin");
        controlPanel.add(addAdminBtn, gbc);

        // Separator
        controlPanel.add(new JSeparator(), gbc);

        // Section Title: Add Student
        JLabel studentTitle = new JLabel("Add New Student");
        studentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        studentTitle.setForeground(ACCENT_COLOR);
        controlPanel.add(studentTitle, gbc);

        // Student ID
        studentIdField = new JTextField(15);
        controlPanel.add(new JLabel("Student Admission No:"), gbc);
        controlPanel.add(studentIdField, gbc);

        // Assign Admin Dropdown
        adminAssignBox = new JComboBox<>();
        controlPanel.add(new JLabel("Assign to Admin:"), gbc);
        controlPanel.add(adminAssignBox, gbc);

        addStudentBtn = createButton("Add Student");
        controlPanel.add(addStudentBtn, gbc);

        add(controlPanel, BorderLayout.WEST);

        // Table Section (Right)
        dataTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(SECONDARY_COLOR);
        viewAdminsBtn = createButton("View Admins");
        viewStudentsBtn = createButton("View Students");
        bottomPanel.add(viewAdminsBtn);
        bottomPanel.add(viewStudentsBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event Handlers
        addAdminBtn.addActionListener(e -> addAdmin());
        addStudentBtn.addActionListener(e -> addStudent());
        viewAdminsBtn.addActionListener(e -> loadTable("admin"));
        viewStudentsBtn.addActionListener(e -> loadTable("student"));

        // Create and add logout button
        JButton logoutBtn = createButton("Logout");
        logoutBtn.setBackground(ACCENT_COLOR);
        logoutBtn.addActionListener(e -> logout());
        bottomPanel.add(logoutBtn);

        loadAdminDropdown();
        loadTable("admin"); // Show admin list by default
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY_COLOR);
            }
        });
        return btn;
    }

    private void addAdmin() {
        String username = adminUserField.getText().trim();
        String password = new String(adminPassField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill all admin fields!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO admin(username, password) VALUES(?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            showSuccess("Admin added successfully!");
            adminUserField.setText("");
            adminPassField.setText("");
            loadAdminDropdown();
            loadTable("admin"); // Refresh the table
        } catch (SQLException e) {
            showError("Error adding admin: " + e.getMessage());
        }
    }

    private void addStudent() {
        String studentId = studentIdField.getText().trim();
        String adminName = (String) adminAssignBox.getSelectedItem();

        if (studentId.isEmpty() || adminName == null) {
            showError("Please fill all student fields!");
            return;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // First check if student already exists
            try (PreparedStatement checkPs = con.prepareStatement("SELECT student_id FROM student WHERE student_id = ?")) {
                checkPs.setString(1, studentId);
                if (checkPs.executeQuery().next()) {
                    showError("Student ID already exists!");
                    return;
                }
            }

            // Get admin ID
            int adminId;
            try (PreparedStatement psAdmin = con.prepareStatement("SELECT admin_id FROM admin WHERE username = ?")) {
                psAdmin.setString(1, adminName);
                ResultSet rs = psAdmin.executeQuery();
                if (!rs.next()) {
                    showError("Selected admin not found!");
                    return;
                }
                adminId = rs.getInt("admin_id");
            }

            // Add student
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO student(student_id, admin_id, status) VALUES (?, ?, 'not_voted')")) {
                ps.setString(1, studentId);
                ps.setInt(2, adminId);
                ps.executeUpdate();
            }

            con.commit();
            showSuccess("Student added successfully!");
            studentIdField.setText("");
            loadTable("student"); // Refresh the table

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    showError("Error rolling back transaction: " + ex.getMessage());
                }
            }
            showError("Error adding student: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    showError("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadAdminDropdown() {
        adminAssignBox.removeAllItems();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement("SELECT username FROM admin ORDER BY username");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                adminAssignBox.addItem(rs.getString("username"));
            }
        } catch (SQLException e) {
            showError("Error loading admin list: " + e.getMessage());
        }
    }

    private void loadTable(String table) {
        // Input validation
        if (!table.matches("^(admin|student)$")) {
            showError("Invalid table name");
            return;
        }

        String query = table.equals("admin") ? 
            "SELECT admin_id AS ID, username AS Username FROM admin" :
            "SELECT student_id AS ID, status AS Status, admin_id AS 'Admin ID' FROM student";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Get column names
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = meta.getColumnName(i);
            }

            // Get data
            java.util.List<String[]> data = new java.util.ArrayList<>();
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                data.add(row);
            }

            // Convert List to array
            // Create and set new table model with non-editable cells
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Add rows to model
            for (String[] rowData : data) {
                model.addRow(rowData);
            }
            
            dataTable.setModel(model);

            // Style the table
            dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
            dataTable.getTableHeader().setBackground(PRIMARY_COLOR);
            dataTable.getTableHeader().setForeground(Color.WHITE);
            dataTable.setRowHeight(30);
            dataTable.setShowGrid(true);
            dataTable.setGridColor(new Color(230, 230, 230));
            
            // Center-align all columns
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < dataTable.getColumnCount(); i++) {
                dataTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

        } catch (SQLException e) {
            showError("Error loading data: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void logout() {
        dispose();
        new LoginPage().setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error setting look and feel: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        SwingUtilities.invokeLater(() -> {
            SuperAdminDashboard dashboard = new SuperAdminDashboard();
            dashboard.setVisible(true);
        });
    }
}
