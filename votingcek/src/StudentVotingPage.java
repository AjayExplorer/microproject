import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;

public class StudentVotingPage extends JFrame {
    // Modern Color Palette
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);    // Blue
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);  // Lighter Blue
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);     // Green
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // Light Gray
    private static final Color TEXT_COLOR = new Color(44, 62, 80);         // Dark Blue Gray
    private static final Color WARNING_COLOR = new Color(231, 76, 60);     // Red
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);     // Dark Green
    
    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    private final String studentId;
    private final JPanel candidatesPanel;
    private final JLabel timerLabel;
    private final JProgressBar timeProgressBar;
    private Timer timer;
    private int timeLeft;
    private final List<Integer> selectedCandidates;
    private final Map<JButton, Integer> buttonToCandidateMap;

    public StudentVotingPage(String studentId) {
        this.studentId = studentId;
        this.candidatesPanel = new JPanel();
        this.timerLabel = new JLabel();
        this.timeProgressBar = new JProgressBar(0, 60);
        this.timeLeft = 60;
        this.selectedCandidates = new ArrayList<>();
        this.buttonToCandidateMap = new HashMap<>();
        
        setupWindow();
        setupUI();
        loadCandidates();
        startTimer();
    }

    private void setupWindow() {
        setTitle("Student Voting Portal - CEK");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(15, 15));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showWarning("Please complete your vote or wait for the timer to expire.");
            }
        });
    }

    private void setupUI() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Cast Your Vote", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        
        JLabel studentInfoLabel = new JLabel("Student ID: " + studentId);
        studentInfoLabel.setFont(HEADER_FONT);
        studentInfoLabel.setForeground(Color.WHITE);
        
        JPanel timerPanel = new JPanel(new BorderLayout(10, 0));
        timerPanel.setOpaque(false);
        
        timerLabel.setText("Time Left: " + timeLeft + "s");
        timerLabel.setFont(HEADER_FONT);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        timeProgressBar.setValue(timeLeft);
        timeProgressBar.setStringPainted(true);
        timeProgressBar.setForeground(ACCENT_COLOR);
        timeProgressBar.setBackground(SECONDARY_COLOR);
        
        timerPanel.add(timerLabel, BorderLayout.NORTH);
        timerPanel.add(timeProgressBar, BorderLayout.SOUTH);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(studentInfoLabel, BorderLayout.WEST);
        headerPanel.add(timerPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setBackground(Color.WHITE);
        instructionsPanel.setBorder(createRoundedBorder());
        instructionsPanel.setLayout(new BorderLayout());
        
        JLabel instructionsLabel = new JLabel(
            "<html><div style='padding: 10px;'>" +
            "• Please select your preferred candidates (maximum 2)<br>" +
            "• You can only vote once<br>" +
            "• Your vote is confidential<br>" +
            "• Click on 'Vote' button next to your chosen candidates" +
            "</div></html>"
        );
        instructionsLabel.setFont(NORMAL_FONT);
        instructionsLabel.setForeground(TEXT_COLOR);
        instructionsPanel.add(instructionsLabel, BorderLayout.CENTER);

        candidatesPanel.setLayout(new BoxLayout(candidatesPanel, BoxLayout.Y_AXIS));
        candidatesPanel.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(candidatesPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        mainPanel.add(instructionsPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JLabel noteLabel = new JLabel("Your vote is secure and confidential");
        noteLabel.setFont(NORMAL_FONT);
        noteLabel.setForeground(TEXT_COLOR);
        footerPanel.add(noteLabel);

        return footerPanel;
    }

    private void loadCandidates() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT candidate_id, name FROM candidate")) {
            
            while (rs.next()) {
                int candidateId = rs.getInt("candidate_id");
                String name = rs.getString("name");
                addCandidateCard(candidateId, name);
            }
        } catch (Exception e) {
            showError("Error loading candidates: " + e.getMessage());
        }
    }

    private void addCandidateCard(int candidateId, String name) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(createRoundedBorder());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(HEADER_FONT);
        nameLabel.setForeground(TEXT_COLOR);
        
        JLabel idLabel = new JLabel("ID: " + candidateId);
        idLabel.setFont(NORMAL_FONT);
        idLabel.setForeground(TEXT_COLOR);

        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);

        JButton voteButton = createVoteButton(candidateId);
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(voteButton, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.add(card, BorderLayout.CENTER);

        candidatesPanel.add(wrapper);
        candidatesPanel.revalidate();
        candidatesPanel.repaint();
    }

    private JButton createVoteButton(int candidateId) {
        JButton voteButton = new JButton("Vote");
        voteButton.setFont(HEADER_FONT);
        voteButton.setForeground(Color.WHITE);
        voteButton.setBackground(ACCENT_COLOR);
        voteButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        voteButton.setFocusPainted(false);
        voteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonToCandidateMap.put(voteButton, candidateId);

        voteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                voteButton.setBackground(ACCENT_COLOR.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                voteButton.setBackground(ACCENT_COLOR);
            }
        });

        voteButton.addActionListener(e -> handleVoteButtonClick(voteButton, candidateId));
        return voteButton;
    }

    private void handleVoteButtonClick(JButton button, int candidateId) {
        if (selectedCandidates.contains(candidateId)) {
            selectedCandidates.remove(Integer.valueOf(candidateId));
            button.setBackground(ACCENT_COLOR);
        } else {
            if (selectedCandidates.size() >= 2) {
                showWarning("You can only select 2 candidates!");
                return;
            }
            selectedCandidates.add(candidateId);
            button.setBackground(SUCCESS_COLOR);
        }

        if (selectedCandidates.size() == 2) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cast your votes?",
                "Confirm Vote",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                castVotes();
            }
        }
    }

    private void castVotes() {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Update votes for selected candidates
                for (int candidateId : selectedCandidates) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE candidate SET votes = votes + 1 WHERE candidate_id = ?")) {
                        ps.setInt(1, candidateId);
                        ps.executeUpdate();
                    }
                }

                // Update student status
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE student SET status = 'voted' WHERE student_id = ?")) {
                    ps.setString(1, studentId);
                    ps.executeUpdate();
                }

                // Update total votes
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE super_admin SET total_votes = total_votes + 1 WHERE super_id = 1")) {
                    ps.executeUpdate();
                }

                con.commit();
                showSuccess("Your votes have been cast successfully!");
                stopTimer();
                dispose();
                // Return to Admin Dashboard after voting completes
                new AdminDashboard().setVisible(true);

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            showError("Error casting votes: " + e.getMessage());
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    timeLeft--;
                    timerLabel.setText("Time Left: " + timeLeft + "s");
                    timeProgressBar.setValue(timeLeft);

                    if (timeLeft <= 10) {
                        timerLabel.setForeground(WARNING_COLOR);
                        timeProgressBar.setForeground(WARNING_COLOR);
                    }

                    if (timeLeft <= 0) {
                        stopTimer();
                        showTimeout();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private Border createRoundedBorder() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0, 0, 0, 30), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
    }

    private void showTimeout() {
        JOptionPane.showMessageDialog(this,
            "Time's up! Voting session has expired.",
            "Session Expired",
            JOptionPane.WARNING_MESSAGE);
        dispose();
        new LoginPage().setVisible(true);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Warning",
            JOptionPane.WARNING_MESSAGE);
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new StudentVotingPage("STU001").setVisible(true);
        });
    }
}