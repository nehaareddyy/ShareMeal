
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabase {
    public static void main(String[] args) {
       try (Connection ignored = DatabaseConnection.getConnection()) {
            System.out.println("Connected to ShareMeal database successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            System.out.println(e.getMessage());
        }
    }
}
