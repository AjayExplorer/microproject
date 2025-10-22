import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class SuperAdminPage extends JFrame {
    JTextField adminUsernameField, adminPasswordField;
    JTextField studentIdField, assignAdminIdField;
    JTextArea outputArea;
    JButton addAdminBtn, addStudentBtn, viewAdminsBtn, viewStudentsBtn;

    public SuperAdminPage() {
        setTitle("Super Admin Dashboard");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Super Admin Control Panel", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Admin section
        centerPanel.add(new JLabel("Admin Username:"));
        adminUsernameField = new JTextField();
        centerPanel.add(adminUsernameField);

        centerPanel.add(new JLabel("Admin Password:"));
        adminPasswordField = new JTextField();
        centerPanel.add(adminPasswordField);

        addAdminBtn = new JButton("Add Admin");
        centerPanel.add(addAdminBtn);
        addAdminBtn.addActionListener(e -> addAdmin());

        // Student section
        centerPanel.add(new JLabel("Student ID:"));
        studentIdField = new JTextField();
        centerPanel.add(studentIdField);

        centerPanel.add(new JLabel("Assign to Admin ID:"));
        assignAdminIdField = new JTextField();
        centerPanel.add(assignAdminIdField);

        addStudentBtn = new JButton("Add Student");
        centerPanel.add(addStudentBtn);
        addStudentBtn.addActionListener(e -> addStudent());

        add(centerPanel, BorderLayout.CENTER);

        // View Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        viewAdminsBtn = new JButton("View All Admins");
        viewStudentsBtn = new JButton("View All Students");
        bottomPanel.add(viewAdminsBtn);
        bottomPanel.add(viewStudentsBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.EAST);

        viewAdminsBtn.addActionListener(e -> viewAdmins());
        viewStudentsBtn.addActionListener(e -> viewStudents());

        setVisible(true);
    }

    private void addAdmin() {
        String username = adminUsernameField.getText().trim();
        String password = adminPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill both fields!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO admin(username, password) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Admin added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addStudent() {
        String studentId = studentIdField.getText().trim();
        String adminIdText = assignAdminIdField.getText().trim();

        if (studentId.isEmpty() || adminIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill both fields!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO student(student_id, status, admin_id) VALUES (?, 'not_voted', ?)")) {
            ps.setString(1, studentId);
            ps.setInt(2, Integer.parseInt(adminIdText));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added and assigned to Admin ID " + adminIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void viewAdmins() {
        outputArea.setText("List of Admins:\n-----------------\n");
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT admin_id, username FROM admin")) {
            while (rs.next()) {
                outputArea.append("Admin ID: " + rs.getInt("admin_id") + 
                                  " | Username: " + rs.getString("username") + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void viewStudents() {
        outputArea.setText("List of Students:\n-----------------\n");
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT student_id, status, admin_id FROM student")) {
            while (rs.next()) {
                outputArea.append("Student ID: " + rs.getString("student_id") + 
                                  " | Status: " + rs.getString("status") +
                                  " | Admin ID: " + rs.getInt("admin_id") + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SuperAdminPage();
    }
}
