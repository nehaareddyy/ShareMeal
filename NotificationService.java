
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    // Stores individual notification details.
    public static class Notification {
        private int notificationId;
        private String message;
        private Timestamp createdAt;

        public Notification(int id, String message, Timestamp time) {
            this.notificationId = id;
            this.message = message;
            this.createdAt = time;
        }

        public int getNotificationId() {
            return notificationId;
        }

        public String getMessage() {
            return message;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }
    }

    // Send notification to one user.
    public static boolean sendNotification(int userId, String message) {

        String sql = "INSERT INTO notifications " +
                     "(user_id, message, is_read) VALUES (?, ?, FALSE)";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.setString(2, message);

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Notify all registered NGO accounts.
    public static void notifyAllNGOs(String message) {

        String sql = "SELECT user_id FROM users WHERE role = 'NGO'";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                sendNotification(rs.getInt("user_id"), message);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve unread notifications with their IDs.
    public static List<Notification> getUnreadNotifications(int userId) {

        List<Notification> notifications = new ArrayList<>();

        String sql = "SELECT notification_id, message, created_at " +
                     "FROM notifications " +
                     "WHERE user_id = ? AND is_read = FALSE " +
                     "ORDER BY created_at DESC";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    notifications.add(new Notification(
                        rs.getInt("notification_id"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    // Mark one notification as read.
    public static boolean markAsRead(int notificationId, int userId) {

        String sql = "UPDATE notifications SET is_read = TRUE " +
                     "WHERE notification_id = ? AND user_id = ?";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mark all notifications as read.
    public static boolean markAllAsRead(int userId) {

        String sql = "UPDATE notifications SET is_read = TRUE " +
                     "WHERE user_id = ? AND is_read = FALSE";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
