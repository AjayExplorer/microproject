import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class AdminDashboard extends JFrame {
    JTextArea resultArea;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Election Results", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        resultArea = new JTextArea();
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Results");
        refreshBtn.addActionListener(e -> loadResults());
        add(refreshBtn, BorderLayout.SOUTH);

        loadResults();
        setVisible(true);
    }

    private void loadResults() {
        resultArea.setText("");
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, votes FROM candidate ORDER BY votes DESC")) {
            while (rs.next()) {
                resultArea.append(rs.getString("name") + " - Votes: " + rs.getInt("votes") + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
