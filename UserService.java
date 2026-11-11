
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // Register a donor or another user
    public static boolean registerUser(
            String fullName,
            String email,
            String passwordHash,
            String role) {

        String sql = "INSERT INTO users (full_name, email, password_hash, role) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.setString(4, role.toUpperCase());

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check login credentials
    public static boolean loginUser(String email, String passwordHash) {

        String sql = "SELECT user_id FROM users "
                   + "WHERE email = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve the user's ID and role after successful login.
    // Returns {userId, 1} for DONOR or {userId, 2} for NGO.
    public static int[] getUserDetails(
            String email,
            String passwordHash) {

        String sql = "SELECT user_id, role FROM users "
                   + "WHERE email = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String role = rs.getString("role");

                    if ("DONOR".equalsIgnoreCase(role)) {
                        return new int[]{userId, 1};
                    } else if ("NGO".equalsIgnoreCase(role)) {
                        return new int[]{userId, 2};
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Register an NGO and its associated user account.
    public static boolean registerNGO(
            String fullName,
            String email,
            String passwordHash,
            String ngoName,
            String location,
            String contactNumber) {

        String userSql = "INSERT INTO users "
                + "(full_name, email, password_hash, role) "
                + "VALUES (?, ?, ?, 'NGO')";

        String ngoSql = "INSERT INTO ngos "
                + "(user_id, ngo_name, location, contact_number) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                int userId;

                try (PreparedStatement userStmt = conn.prepareStatement(
                        userSql, Statement.RETURN_GENERATED_KEYS)) {

                    userStmt.setString(1, fullName);
                    userStmt.setString(2, email);
                    userStmt.setString(3, passwordHash);

                    userStmt.executeUpdate();

                    try (ResultSet keys = userStmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not retrieve the new user ID."
                            );
                        }

                        userId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ngoStmt = conn.prepareStatement(ngoSql)) {
                    ngoStmt.setInt(1, userId);
                    ngoStmt.setString(2, ngoName);
                    ngoStmt.setString(3, location);
                    ngoStmt.setString(4, contactNumber);

                    ngoStmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get the NGO's database ID using the logged-in user's ID.
    // This is important because users.user_id and ngos.ngo_id
    // are separate IDs.
    public static int getNgoIdByUserId(int userId) {

        String sql = "SELECT ngo_id FROM ngos WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ngo_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // Get all registered NGOs for the Nearby NGOs screen.
    public static List<String> getRegisteredNGOs() {

        List<String> ngos = new ArrayList<>();

        String sql = "SELECT ngo_name, location, contact_number, is_online "
                   + "FROM ngos ORDER BY ngo_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String ngoName = rs.getString("ngo_name");
                String location = rs.getString("location");
                String contact = rs.getString("contact_number");
                boolean isOnline = rs.getBoolean("is_online");

                String details =
                        "NGO: " + ngoName
                        + "\nLocation: " + location
                        + "\nContact: " + contact
                        + "\nStatus: " + (isOnline ? "Online" : "Offline");

                ngos.add(details);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ngos;
    }
}
