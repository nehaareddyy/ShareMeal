
import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShareMealLogin extends JFrame {

    @FunctionalInterface
    public interface LoginSuccessListener {
        void onLoginSuccess(int userId, String role);
    }

    private LoginSuccessListener loginSuccessListener;

    public ShareMealLogin() {
        this(null);
    }

    public ShareMealLogin(LoginSuccessListener listener) {
        this.loginSuccessListener = listener;

        setTitle("ShareMeal - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        );

        JLabel heading = new JLabel(
            "Login to ShareMeal",
            SwingConstants.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField emailField = new JTextField();
        emailField.setBorder(
            BorderFactory.createTitledBorder("Email")
        );

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBorder(
            BorderFactory.createTitledBorder("Password")
        );

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(46, 139, 87));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        panel.add(heading);
        panel.add(emailField);
        panel.add(passwordField);
        panel.add(loginButton);

        JLabel footer = new JLabel(
            "Welcome back!",
            SwingConstants.CENTER
        );
        panel.add(footer);

        add(panel);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(
                passwordField.getPassword()
            );

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter both email and password."
                );
                return;
            }

            String passwordHash;

            try {
                MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

                byte[] hashBytes = digest.digest(
                    password.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                    )
                );

                StringBuilder hex = new StringBuilder();

                for (byte b : hashBytes) {
                    hex.append(
                        String.format("%02x", b & 0xff)
                    );
                }

                passwordHash = hex.toString();

            } catch (NoSuchAlgorithmException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Password processing failed."
                );
                return;
            }

            int[] userDetails =
                UserService.getUserDetails(email, passwordHash);

            if (userDetails != null) {
                int userId = userDetails[0];

                String role =
                    userDetails[1] == 1 ? "DONOR" : "NGO";

                JOptionPane.showMessageDialog(
                    this,
                    "Login successful!\nRole: " + role
                );

                if (loginSuccessListener != null) {
                    loginSuccessListener.onLoginSuccess(
                        userId,
                        role
                    );
                }

                dispose();

            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid email or password."
                );
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new ShareMealLogin().setVisible(true)
        );
    }
}
