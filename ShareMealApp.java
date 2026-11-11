
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ShareMealApp {

    private static int loggedInUserId = -1;
    private static String loggedInRole = null;
    private static int loggedInNgoId = -1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShareMealApp::createHomeScreen);
    }

    // ================= HOME SCREEN =================

    private static void createHomeScreen() {

        JFrame frame = new JFrame("ShareMeal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 680);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(10, 1, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(20, 35, 20, 35)
        );
        panel.setBackground(new Color(235, 248, 235));

        JLabel heading = new JLabel(
            "ShareMeal", SwingConstants.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 32));
        heading.setForeground(new Color(30, 110, 55));

        JLabel subtitle = new JLabel(
            "Share food. Reduce waste. Help people.",
            SwingConstants.CENTER
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton availableButton = createButton("Available Food");
        JButton ngosButton = createButton("Nearby NGOs");
        JButton donateButton = createButton("Donate Food");
        JButton acceptButton = createButton("Accept Food");
        JButton acceptedFoodButton = createButton("Accepted Food");
        JButton addNgoButton = createButton("Add an NGO");

        JButton notificationButton =
            createButton("Notifications");

        availableButton.addActionListener(
            e -> showAvailableFood(frame)
        );

        ngosButton.addActionListener(
            e -> showNearbyNGOs(frame)
        );

        donateButton.addActionListener(
            e -> showDonateFoodScreen(frame)
        );

        acceptButton.addActionListener(
            e -> showAcceptFoodScreen(frame)
        );

        acceptedFoodButton.addActionListener(
            e -> showAcceptedFoodScreen(frame)
        );

        notificationButton.addActionListener(
            e -> showNotifications(frame)
        );

        addNgoButton.addActionListener(e -> {

            if (!isLoggedIn()) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Please log in with your NGO account first."
                );

            } else if (!"NGO".equals(loggedInRole)) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Only NGO accounts can manage NGO details."
                );

            } else {

                JOptionPane.showMessageDialog(
                    frame,
                    "Your NGO details are managed through NGO registration."
                );
            }
        });

        JPanel accountPanel = new JPanel(
            new GridLayout(1, 2, 10, 0)
        );
        accountPanel.setOpaque(false);

        JButton loginButton = createButton("Login");
        JButton registerButton = createButton("Register");

        loginButton.addActionListener(
            e -> openLogin(frame)
        );

        registerButton.addActionListener(e -> {
            ShareMealRegister register = new ShareMealRegister();
            register.setVisible(true);
        });

        accountPanel.add(loginButton);
        accountPanel.add(registerButton);

        panel.add(heading);
        panel.add(subtitle);
        panel.add(availableButton);
        panel.add(ngosButton);
        panel.add(donateButton);
        panel.add(acceptButton);
        panel.add(acceptedFoodButton);
        panel.add(addNgoButton);
        panel.add(notificationButton);
        panel.add(accountPanel);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    // ================= BUTTON DESIGN =================

    private static JButton createButton(String text) {

        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setBackground(new Color(50, 145, 75));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        return button;
    }

    // ================= LOGIN CHECK =================

    private static boolean isLoggedIn() {

        return loggedInUserId != -1
            && loggedInRole != null;
    }

    // ================= LOGIN =================

    public static void openLogin(JFrame frame) {

        ShareMealLogin login = new ShareMealLogin((userId, role) -> {

            loggedInUserId = userId;
            loggedInRole = role;

            if ("NGO".equals(role)) {

                loggedInNgoId =
                    UserService.getNgoIdByUserId(userId);

                if (loggedInNgoId == -1) {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Login succeeded, but no NGO record was found. " +
                        "Please check your NGO registration."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Logged in successfully as NGO."
                    );
                }

            } else {

                loggedInNgoId = -1;

                JOptionPane.showMessageDialog(
                    frame,
                    "Logged in successfully as DONOR."
                );
            }
        });

        login.setVisible(true);
    }

    // ================= AVAILABLE FOOD =================

    private static void showAvailableFood(JFrame frame) {

        try {

            List<String> donations =
                FoodDonation.getAvailableDonations();

            if (donations == null || donations.isEmpty()) {

                JOptionPane.showMessageDialog(
                    frame,
                    "No available food donations at the moment."
                );
                return;
            }

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(
                new Font("Arial", Font.PLAIN, 14)
            );

            textArea.setText(
                String.join("\n\n", donations)
            );

            JOptionPane.showMessageDialog(
                frame,
                new JScrollPane(textArea),
                "Available Food",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            showError(
                frame,
                "Could not load available food.",
                ex
            );
        }
    }

    // ================= NEARBY NGOS =================

    private static void showNearbyNGOs(JFrame frame) {

        try {

            List<String> ngos =
                UserService.getRegisteredNGOs();

            if (ngos == null || ngos.isEmpty()) {

                JOptionPane.showMessageDialog(
                    frame,
                    "No registered NGOs found."
                );
                return;
            }

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(
                new Font("Arial", Font.PLAIN, 14)
            );

            textArea.setText(
                String.join("\n\n", ngos)
            );

            JOptionPane.showMessageDialog(
                frame,
                new JScrollPane(textArea),
                "Nearby NGOs",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            showError(
                frame,
                "Could not load NGOs.",
                ex
            );
        }
    }

    // ================= DONATE FOOD =================

    private static void showDonateFoodScreen(JFrame frame) {

        if (!isLoggedIn()) {

            JOptionPane.showMessageDialog(
                frame,
                "Please log in before donating food."
            );
            return;
        }

        if (!"DONOR".equals(loggedInRole)) {

            JOptionPane.showMessageDialog(
                frame,
                "Only donor accounts can donate food."
            );
            return;
        }

        JTextField foodNameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField expiryField = new JTextField();

        JPanel form = new JPanel(
            new GridLayout(4, 2, 8, 8)
        );

        form.add(new JLabel("Food name:"));
        form.add(foodNameField);

        form.add(new JLabel("Quantity:"));
        form.add(quantityField);

        form.add(new JLabel("Pickup location:"));
        form.add(locationField);

        form.add(new JLabel("Expiry (yyyy-MM-dd HH:mm):"));
        form.add(expiryField);

        int result = JOptionPane.showConfirmDialog(
            frame,
            form,
            "Donate Food",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String foodName = foodNameField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String location = locationField.getText().trim();
        String expiryText = expiryField.getText().trim();

        if (foodName.isEmpty()
                || quantityText.isEmpty()
                || location.isEmpty()
                || expiryText.isEmpty()) {

            JOptionPane.showMessageDialog(
                frame,
                "Please fill in all fields."
            );
            return;
        }

        try {

            int quantity = Integer.parseInt(quantityText);

            LocalDateTime expiry = LocalDateTime.parse(
                expiryText.replace(" ", "T")
            );

            if (quantity <= 0) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Quantity must be greater than zero."
                );
                return;
            }

            if (!expiry.isAfter(LocalDateTime.now())) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Expiry time must be in the future."
                );
                return;
            }

            boolean saved = FoodDonation.saveDonation(
                loggedInUserId,
                foodName,
                quantity,
                location,
                expiry
            );

            if (saved) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Food donation added successfully!"
                );

            } else {

                JOptionPane.showMessageDialog(
                    frame,
                    "Could not save the donation. " +
                    "Please check the console for errors."
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                frame,
                "Enter a valid number for quantity."
            );

        } catch (DateTimeParseException ex) {

            JOptionPane.showMessageDialog(
                frame,
                "Enter expiry in this format: 2026-10-28 12:30"
            );

        } catch (Exception ex) {

            showError(
                frame,
                "Could not save the donation.",
                ex
            );
        }
    }

    // ================= ACCEPT FOOD =================

    private static void showAcceptFoodScreen(JFrame frame) {

        if (!isLoggedIn()) {

            JOptionPane.showMessageDialog(
                frame,
                "Please log in before accepting food."
            );
            return;
        }

        if (!"NGO".equals(loggedInRole)) {

            JOptionPane.showMessageDialog(
                frame,
                "Only NGO accounts can accept food."
            );
            return;
        }

        // accepted_by_ngo stores ngos.ngo_id,
        // not users.user_id.

        if (loggedInNgoId == -1) {

            loggedInNgoId =
                UserService.getNgoIdByUserId(loggedInUserId);
        }

        if (loggedInNgoId == -1) {

            JOptionPane.showMessageDialog(
                frame,
                "No NGO record is linked to this account. " +
                "Please check your NGO registration."
            );
            return;
        }

        try {

            List<String[]> donations =
                FoodDonation.getAvailableDonationsWithIds();

            if (donations == null || donations.isEmpty()) {

                JOptionPane.showMessageDialog(
                    frame,
                    "No available food donations at the moment."
                );
                return;
            }

            String[] options = new String[donations.size()];

            for (int i = 0; i < donations.size(); i++) {

                String[] donation = donations.get(i);

                options[i] =
                    "ID: " + donation[0]
                    + " | Food: " + donation[1]
                    + " | Quantity: " + donation[2]
                    + " | Location: " + donation[3]
                    + " | Expiry: " + donation[4];
            }

            String selected = (String) JOptionPane.showInputDialog(
                frame,
                "Select a donation to accept:",
                "Accept Food",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (selected == null) {
                return;
            }

            int selectedIndex = -1;

            for (int i = 0; i < options.length; i++) {

                if (options[i].equals(selected)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex == -1) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Could not identify the selected donation."
                );
                return;
            }

            int foodId = Integer.parseInt(
                donations.get(selectedIndex)[0]
            );

            boolean accepted = FoodDonation.acceptDonation(
                foodId,
                loggedInNgoId
            );

            if (accepted) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Food accepted successfully! Please arrange collection."
                );

            } else {

                JOptionPane.showMessageDialog(
                    frame,
                    "This donation could not be accepted. " +
                    "It may have been accepted by another NGO, " +
                    "expired, or a database error occurred. " +
                    "Check the VS Code terminal for details."
                );
            }

        } catch (Exception ex) {

            showError(
                frame,
                "Could not accept the donation.",
                ex
            );
        }
    }


    // ================= ACCEPTED FOOD / COLLECTION =================

    private static void showAcceptedFoodScreen(JFrame frame) {

        if (!isLoggedIn()) {
            JOptionPane.showMessageDialog(
                frame,
                "Please log in with your NGO account first."
            );
            return;
        }

        if (!"NGO".equals(loggedInRole)) {
            JOptionPane.showMessageDialog(
                frame,
                "Only NGO accounts can view accepted food."
            );
            return;
        }

        if (loggedInNgoId == -1) {
            loggedInNgoId = UserService.getNgoIdByUserId(loggedInUserId);
        }

        if (loggedInNgoId == -1) {
            JOptionPane.showMessageDialog(
                frame,
                "No NGO record is linked to this account."
            );
            return;
        }

        while (true) {
            List<String[]> donations =
                FoodDonation.getReservedDonationsByNgo(loggedInNgoId);

            if (donations == null || donations.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "You have no accepted food waiting for collection.",
                    "Accepted Food",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            String[] options = new String[donations.size()];
            for (int i = 0; i < donations.size(); i++) {
                String[] d = donations.get(i);
                options[i] = "ID: " + d[0]
                    + " | Food: " + d[1]
                    + " | Quantity: " + d[2]
                    + " | Location: " + d[3]
                    + " | Expiry: " + d[4];
            }

            String selected = (String) JOptionPane.showInputDialog(
                frame,
                "Select the food after it has physically been collected:",
                "Accepted Food - Mark as Collected",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (selected == null) {
                return;
            }

            int selectedIndex = -1;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(selected)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex < 0) {
                return;
            }

            int foodId = Integer.parseInt(donations.get(selectedIndex)[0]);

            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Have you physically collected this food donation?",
                "Confirm Collection",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                continue;
            }

            boolean collected =
                FoodDonation.markDonationCollected(foodId, loggedInNgoId);

            if (collected) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Food marked as COLLECTED successfully!"
                );
            } else {
                JOptionPane.showMessageDialog(
                    frame,
                    "Could not update the donation. Please try again."
                );
                return;
            }
        }
    }

    // ================= NOTIFICATIONS =================

    private static void showNotifications(JFrame frame) {

        if (!isLoggedIn()) {

            JOptionPane.showMessageDialog(
                frame,
                "Please log in to view your notifications."
            );
            return;
        }

        while (true) {

            List<NotificationService.Notification> notifications =
                NotificationService.getUnreadNotifications(
                    loggedInUserId
                );

            if (notifications.isEmpty()) {

                JOptionPane.showMessageDialog(
                    frame,
                    "You have no unread notifications.",
                    "My Notifications",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            String[] options = new String[notifications.size()];

            for (int i = 0; i < notifications.size(); i++) {

                NotificationService.Notification notification =
                    notifications.get(i);

                options[i] =
                    "ID: " + notification.getNotificationId()
                    + " | " + notification.getMessage()
                    + " | " + notification.getCreatedAt();
            }

            String selected = (String) JOptionPane.showInputDialog(
                frame,
                "Select a notification to mark as read:",
                "My Notifications",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (selected == null) {
                return;
            }

            int selectedIndex = -1;

            for (int i = 0; i < options.length; i++) {

                if (options[i].equals(selected)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex == -1) {
                return;
            }

            int notificationId =
                notifications.get(selectedIndex).getNotificationId();

            boolean updated =
                NotificationService.markAsRead(
                    notificationId,
                    loggedInUserId
                );

            if (updated) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Notification marked as read!"
                );

            } else {

                JOptionPane.showMessageDialog(
                    frame,
                    "Could not update notification. Please try again."
                );
                return;
            }
        }
    }

    // ================= ERROR HANDLING =================

    private static void showError(
            JFrame frame, String message, Exception ex) {

        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            frame,
            message + "\nDetails: " + ex.getMessage(),
            "ShareMeal Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
