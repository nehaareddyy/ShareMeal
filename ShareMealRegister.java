
import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShareMealRegister extends JFrame {

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JComboBox<String> roleBox =
            new JComboBox<>(new String[]{"DONOR", "NGO"});

    private final JTextField ngoNameField = new JTextField(20);
    private final JTextField locationField = new JTextField(20);
    private final JTextField contactField = new JTextField(20);

    private final JLabel ngoNameLabel = new JLabel("NGO Name:");
    private final JLabel locationLabel = new JLabel("NGO Location:");
    private final JLabel contactLabel = new JLabel("Contact Number:");

    public ShareMealRegister() {
        setTitle("ShareMeal - Register");
        setSize(450, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        panel.add(new JLabel("Register As:"));
        panel.add(roleBox);

        panel.add(ngoNameLabel);
        panel.add(ngoNameField);

        panel.add(locationLabel);
        panel.add(locationField);

        panel.add(contactLabel);
        panel.add(contactField);

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        panel.add(registerButton);
        panel.add(cancelButton);

        add(panel);

        roleBox.addActionListener(e -> updateNGOFields());
        updateNGOFields();

        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> dispose());
    }

    private void updateNGOFields() {
        boolean isNGO = "NGO".equals(roleBox.getSelectedItem());

        ngoNameLabel.setVisible(isNGO);
        ngoNameField.setVisible(isNGO);
        locationLabel.setVisible(isNGO);
        locationField.setVisible(isNGO);
        contactLabel.setVisible(isNGO);
        contactField.setVisible(isNGO);

        pack();
        setSize(450, isNGO ? 480 : 350);
        setLocationRelativeTo(null);
    }

    private void register() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please fill in your name, email and password.");
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a valid email address.");
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(
                    this, "Password must contain at least 8 characters.");
            return;
        }

        if ("NGO".equals(role)) {
            if (ngoNameField.getText().trim().isEmpty()
                    || locationField.getText().trim().isEmpty()
                    || contactField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "Please fill in all NGO details.");
                return;
            }
        }

        try {
            String passwordHash = hashPassword(password);
            boolean success;

            if ("NGO".equals(role)) {
                success = UserService.registerNGO(
                        name,
                        email,
                        passwordHash,
                        ngoNameField.getText().trim(),
                        locationField.getText().trim(),
                        contactField.getText().trim()
                );
            } else {
                success = UserService.registerUser(
                        name, email, passwordHash, role);
            }

            if (success) {
                JOptionPane.showMessageDialog(
                        this, "Registration successful! Welcome to ShareMeal.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Registration failed. Check the email or database.");
            }

        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(
                    this, "Password processing failed.");
        }
    }

    private String hashPassword(String password)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(
                password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new ShareMealRegister().setVisible(true));
    }
}
