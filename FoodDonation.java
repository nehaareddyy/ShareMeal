
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FoodDonation {

    // Save a new food donation and notify all registered NGOs.
    public static boolean saveDonation(
            int donorId,
            String foodName,
            int quantity,
            String pickupLocation,
            LocalDateTime expiryTime) {

        String sql =
            "INSERT INTO food_donations " +
            "(donor_id, food_name, quantity, pickup_location, expiry_time) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, donorId);
            stmt.setString(2, foodName);
            stmt.setInt(3, quantity);
            stmt.setString(4, pickupLocation);
            stmt.setTimestamp(5, Timestamp.valueOf(expiryTime));

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted == 1) {
                NotificationService.notifyAllNGOs(
                    "New food donation: " + foodName
                    + ", quantity: " + quantity
                    + ", pickup location: " + pickupLocation
                    + ", expiry: " + expiryTime
                );

                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get available, unexpired food donations for display.
    public static List<String> getAvailableDonations() {
        List<String> donations = new ArrayList<>();

        String sql =
            "SELECT food_name, quantity, pickup_location, expiry_time " +
            "FROM food_donations " +
            "WHERE status = 'AVAILABLE' " +
            "AND expiry_time > NOW() " +
            "ORDER BY created_at DESC";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                String donation =
                    "Food: " + rs.getString("food_name")
                    + "\nQuantity: " + rs.getInt("quantity")
                    + "\nPickup location: "
                    + rs.getString("pickup_location")
                    + "\nExpiry: " + rs.getTimestamp("expiry_time");

                donations.add(donation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return donations;
    }

    // Get available donations with IDs for NGO acceptance.
    // Each array contains:
    // [0] food_id, [1] food_name, [2] quantity,
    // [3] pickup_location, [4] expiry_time.
    public static List<String[]> getAvailableDonationsWithIds() {
        List<String[]> donations = new ArrayList<>();

        String sql =
            "SELECT food_id, food_name, quantity, " +
            "pickup_location, expiry_time " +
            "FROM food_donations " +
            "WHERE status = 'AVAILABLE' " +
            "AND expiry_time > NOW() " +
            "ORDER BY created_at DESC";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                String[] donation = {
                    String.valueOf(rs.getInt("food_id")),
                    rs.getString("food_name"),
                    String.valueOf(rs.getInt("quantity")),
                    rs.getString("pickup_location"),
                    String.valueOf(rs.getTimestamp("expiry_time"))
                };

                donations.add(donation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return donations;
    }

    // Reserve a donation for an NGO.
    // ngoId must be ngos.ngo_id, not users.user_id.
    // Only one NGO can successfully reserve an available donation.
    public static boolean acceptDonation(int foodId, int ngoId) {
        String sql =
            "UPDATE food_donations " +
            "SET status = 'RESERVED', " +
            "accepted_by_ngo = ?, reserved_at = NOW() " +
            "WHERE food_id = ? " +
            "AND status = 'AVAILABLE' " +
            "AND expiry_time > NOW()";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, ngoId);
            stmt.setInt(2, foodId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 1) {
                // Notify the donor after the reservation succeeds.
                notifyDonorFoodAccepted(foodId, ngoId);
                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Notify the donor when an NGO accepts their donation.
    private static void notifyDonorFoodAccepted(int foodId, int ngoId) {
        String sql =
            "SELECT fd.donor_id, fd.food_name, n.ngo_name " +
            "FROM food_donations fd " +
            "JOIN ngos n ON fd.accepted_by_ngo = n.ngo_id " +
            "WHERE fd.food_id = ? AND n.ngo_id = ?";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, foodId);
            stmt.setInt(2, ngoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int donorId = rs.getInt("donor_id");
                    String foodName = rs.getString("food_name");
                    String ngoName = rs.getString("ngo_name");

                    NotificationService.sendNotification(
                        donorId,
                        "Your food donation '" + foodName
                        + "' has been accepted by NGO: " + ngoName
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get food donations reserved by the currently logged-in NGO.
    public static List<String[]> getReservedDonationsByNgo(int ngoId) {
        List<String[]> donations = new ArrayList<>();

        String sql =
            "SELECT food_id, food_name, quantity, pickup_location, expiry_time " +
            "FROM food_donations " +
            "WHERE status = 'RESERVED' AND accepted_by_ngo = ? " +
            "ORDER BY reserved_at DESC";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, ngoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    donations.add(new String[] {
                        String.valueOf(rs.getInt("food_id")),
                        rs.getString("food_name"),
                        String.valueOf(rs.getInt("quantity")),
                        rs.getString("pickup_location"),
                        String.valueOf(rs.getTimestamp("expiry_time"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return donations;
    }

    // Mark a reserved donation as collected by the NGO that accepted it.
    public static boolean markDonationCollected(int foodId, int ngoId) {
        String sql =
            "UPDATE food_donations " +
            "SET status = 'COLLECTED' " +
            "WHERE food_id = ? AND accepted_by_ngo = ? AND status = 'RESERVED'";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, foodId);
            stmt.setInt(2, ngoId);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
