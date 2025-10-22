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

    public AdminDashboard() {
        setTitle("Admin Dashboard - College Election System");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout(15, 15));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

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
        int totalVotes = 0;

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, votes FROM candidate ORDER BY votes DESC")) {
            
            while (rs.next()) {
                String name = rs.getString("name");
                int votes = rs.getInt("votes");
                totalVotes += votes;
                addResultCard(name, votes);
            }
            
            totalVotesLabel.setText("Total Votes: " + totalVotes);
            
        } catch (SQLException e) {
            showError("Database error while loading results: " + e.getMessage());
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void addResultCard(String name, int votes) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);

        JLabel votesLabel = new JLabel("Votes: " + votes);
        votesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        votesLabel.setForeground(TEXT_COLOR);

        card.add(nameLabel, BorderLayout.CENTER);
        card.add(votesLabel, BorderLayout.EAST);

        resultsPanel.add(card);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 1)));
    }

    private void logout() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        dispose();
        new LoginPage().setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
