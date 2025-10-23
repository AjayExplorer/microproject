import java.sql.*;

public class AddPositionColumn {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            try (Statement st = con.createStatement()) {
                // First check if position column exists
                boolean columnExists = false;
                try (ResultSet rs = con.getMetaData().getColumns(null, null, "candidate", "position")) {
                    columnExists = rs.next();
                }

                if (!columnExists) {
                    // Add position column if it doesn't exist
                    st.executeUpdate("ALTER TABLE candidate ADD COLUMN position VARCHAR(100)");
                    System.out.println("Successfully added position column to candidate table");
                } else {
                    System.out.println("Position column already exists");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}