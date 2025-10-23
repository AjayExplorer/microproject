import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class AdminDashboard extends JFrame {
    // Modern Color Scheme (matching LoginPage)
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);    // Steel Blue
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color ACCENT_COLOR = new Color(25, 25, 112);      // Midnight Blue
    private static final Color TEXT_COLOR = new Color(44, 62, 80);         // Dark Gray Blue
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);    // Green

    private JPanel resultsPanel;
    private JLabel totalVotesLabel;
    private Timer refreshTimer;
    private JTextField studentIdEntry;
    private JButton startVotingBtn;
    private JTextField candidateStudentId;
    private JTextField candidatePosition;
    private JTextField candidateNameField;
    private JLabel candidateNameLabel;
    private JButton lookupStudentBtn;
    private JButton addCandidateBtn;

    public AdminDashboard() {
        setTitle("Admin Dashboard - College Election System");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout(15, 15));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Split the main area into candidate management and results
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(SECONDARY_COLOR);
        
        // Left side: Candidate Management
        JPanel candidatePanel = createCandidateManagementPanel();
        splitPane.setLeftComponent(candidatePanel);
        
        // Right side: Results
        JPanel mainPanel = createMainPanel();
        splitPane.setRightComponent(mainPanel);
        
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setupAutoRefresh();
        loadResults();
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(800, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Election Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        totalVotesLabel = new JLabel("Total Votes: 0");
        totalVotesLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalVotesLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(totalVotesLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 1, 15, 15));
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane);

        return mainPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(SECONDARY_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JButton refreshBtn = createStyledButton("Refresh Results");
        refreshBtn.addActionListener(e -> loadResults());
        footerPanel.add(refreshBtn);

        JButton logoutBtn = createStyledButton("Logout");
        logoutBtn.setBackground(ACCENT_COLOR);
        logoutBtn.addActionListener(e -> logout());
        footerPanel.add(logoutBtn);

        // Student voting starter
        JPanel voteStart = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voteStart.setBackground(SECONDARY_COLOR);
        voteStart.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        voteStart.add(new JLabel("Student ID:"));
        studentIdEntry = new JTextField(12);
        voteStart.add(studentIdEntry);
        startVotingBtn = createStyledButton("Start Voting");
        voteStart.add(startVotingBtn);
        footerPanel.add(voteStart);

        startVotingBtn.addActionListener(e -> startVotingForStudent());

        return footerPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getText().equals("Logout")) {
                    button.setBackground(button.getBackground().darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.getText().equals("Logout")) {
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });

        return button;
    }

    private void setupAutoRefresh() {
        refreshTimer = new Timer(10000, e -> loadResults()); // Refresh every 10 seconds
        refreshTimer.start();
    }

    private void loadResults() {
    resultsPanel.removeAll();

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT candidate_id, name, position, votes, " +
                 "(SELECT COUNT(*) FROM student WHERE status='voted') as total_voters " +
                 "FROM candidate ORDER BY votes DESC")) {
            
            if (rs.next()) {
                int totalVoters = rs.getInt("total_voters");
                totalVotesLabel.setText(String.format("Total Voters: %d", totalVoters));
                
                do {
                    int candidateId = rs.getInt("candidate_id");
                    String name = rs.getString("name");
                    String position = rs.getString("position");
                    int votes = rs.getInt("votes");
                    addResultCard(candidateId, name, position, votes, totalVoters);
                } while (rs.next());
            }
            
        } catch (SQLException e) {
            showError("Database error while loading results: " + e.getMessage());
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void addResultCard(int candidateId, String name, String position, int votes, int totalVoters) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Top row: Name
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        
        topRow.add(nameLabel, BorderLayout.WEST);
        
        // Middle row: Position
        JLabel positionLabel = new JLabel("Position: " + position);
        positionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        positionLabel.setForeground(TEXT_COLOR);
        
    // Bottom row: Votes, percentage and Delete button for admins
        JPanel bottomRow = new JPanel(new BorderLayout(10, 0));
        bottomRow.setOpaque(false);
        
        double percentage = totalVoters > 0 ? (votes * 100.0) / totalVoters : 0;
        JLabel votesLabel = new JLabel(String.format("Votes: %d (%.1f%%)", votes, percentage));
        votesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        votesLabel.setForeground(votes > 0 ? SUCCESS_COLOR : TEXT_COLOR);
        
        bottomRow.add(votesLabel, BorderLayout.WEST);
        
        // Progress bar for votes
        JProgressBar progress = new JProgressBar(0, Math.max(1, totalVoters));
        progress.setValue(votes);
        progress.setStringPainted(true);
        progress.setString(String.format("%.1f%%", percentage));
        progress.setForeground(SUCCESS_COLOR);
        progress.setPreferredSize(new Dimension(150, 20));
        bottomRow.add(progress, BorderLayout.EAST);

        // Delete button
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete candidate '" + name + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteCandidate(candidateId);
            }
        });
        bottomRow.add(deleteBtn, BorderLayout.CENTER);

        card.add(topRow);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(positionLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(bottomRow);

        resultsPanel.add(card);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void deleteCandidate(int candidateId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM candidate WHERE candidate_id = ?")) {
            ps.setInt(1, candidateId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                showSuccess("Candidate deleted successfully.");
                loadResults();
            } else {
                showError("Candidate not found or could not be deleted.");
            }
        } catch (SQLException e) {
            showError("Error deleting candidate: " + e.getMessage());
        }
    }

    private void logout() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        dispose();
        new LoginPage().setVisible(true);
    }

    private void startVotingForStudent() {
        String sid = studentIdEntry.getText().trim();
        if (sid.isEmpty()) {
            showError("Please enter a student ID to begin voting.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Check election active
            try (PreparedStatement ps = con.prepareStatement("SELECT active FROM election_status WHERE id = 1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int active = rs.getInt("active");
                        if (active != 1) {
                            showError("Election is not active. Please ask Super Admin to start the election.");
                            return;
                        }
                    } else {
                        showError("Election status not configured. Contact Super Admin.");
                        return;
                    }
                }
            }

            // Check student exists and not voted
            try (PreparedStatement ps = con.prepareStatement("SELECT status FROM student WHERE student_id = ?")) {
                ps.setString(1, sid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        showError("Student ID not found: " + sid);
                        return;
                    }
                    String status = rs.getString("status");
                    if ("voted".equalsIgnoreCase(status)) {
                        showError("Student has already voted.");
                        return;
                    }
                }
            }

            // All good — open StudentVotingPage
            new StudentVotingPage(sid).setVisible(true);
            dispose();

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createCandidateManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel title = new JLabel("Add New Candidate", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Student ID lookup
        JPanel lookupPanel = new JPanel(new BorderLayout(5, 0));
        lookupPanel.setBackground(Color.WHITE);
        candidateStudentId = new JTextField(15);
        lookupStudentBtn = createStyledButton("Lookup");
        lookupPanel.add(candidateStudentId, BorderLayout.CENTER);
        lookupPanel.add(lookupStudentBtn, BorderLayout.EAST);

        formPanel.add(new JLabel("Student ID:"), gbc);
        formPanel.add(lookupPanel, gbc);

    // Student Name (editable) - allow admin to set display name
    candidateNameLabel = new JLabel("Name:");
    candidateNameField = new JTextField(20);
    JPanel namePanel = new JPanel(new BorderLayout());
    namePanel.setBackground(Color.WHITE);
    namePanel.add(candidateNameField, BorderLayout.CENTER);
    formPanel.add(candidateNameLabel, gbc);
    formPanel.add(namePanel, gbc);

        // Position
        formPanel.add(new JLabel("Position:"), gbc);
        candidatePosition = new JTextField(20);
        formPanel.add(candidatePosition, gbc);

        // Add button
        addCandidateBtn = createStyledButton("Register as Candidate");
        addCandidateBtn.setEnabled(false); // Enable after successful lookup
        
        // Add form to panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(addCandidateBtn, BorderLayout.SOUTH);

        // Event handlers
        lookupStudentBtn.addActionListener(e -> lookupStudent());
        addCandidateBtn.addActionListener(e -> addCandidate());

        return panel;
    }

    private void lookupStudent() {
        String studentId = candidateStudentId.getText().trim();
        if (studentId.isEmpty()) {
            showError("Please enter a student ID");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT student_id FROM student WHERE student_id = ?")) {
            
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showError("Student not found");
                    addCandidateBtn.setEnabled(false);
                    candidateNameLabel.setText("Name: Not found");
                    return;
                }

                // Check if already a candidate
                try (PreparedStatement checkPs = con.prepareStatement(
                        "SELECT 1 FROM candidate WHERE name = ?")) {
                    checkPs.setString(1, studentId);
                    if (checkPs.executeQuery().next()) {
                        showError("This student is already registered as a candidate");
                        addCandidateBtn.setEnabled(false);
                        return;
                    }
                }

                // Populate the editable name field (prefill with student ID)
                candidateNameField.setText(studentId);
                candidateNameLabel.setText("Name: ");
                addCandidateBtn.setEnabled(true);
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void addCandidate() {
        String studentId = candidateStudentId.getText().trim();
        String position = candidatePosition.getText().trim();

        if (studentId.isEmpty() || position.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO candidate(name, position, votes) VALUES(?, ?, 0)")) {
            
            String displayName = candidateNameField.getText().trim();
            if (displayName.isEmpty()) {
                showError("Please enter a name for the candidate");
                return;
            }

            ps.setString(1, displayName);
            ps.setString(2, position);
            ps.executeUpdate();

            showSuccess("Candidate registered successfully!");
            // Clear form
            candidateStudentId.setText("");
            candidatePosition.setText("");
            candidateNameLabel.setText("Name: ");
            addCandidateBtn.setEnabled(false);
            // Refresh results
            loadResults();

        } catch (SQLException e) {
            showError("Error registering candidate: " + e.getMessage());
        }
    }
}
