package arpharmacy;

import arpharmacy.db.DBConnection;
import arpharmacy.panels.*;

import javax.swing.*;
import java.awt.*;

/**
 * AR Pharmacy System – Main entry point.
 *
 * HOW TO RUN IN NetBeans:
 *  1. Run pharmacy_setup.sql in MySQL Workbench / CLI first.
 *  2. Edit DBConnection.java → set DB_USER and DB_PASS.
 *  3. Add mysql-connector-j-*.jar to the project Libraries.
 *  4. Right-click project → Run  (or press F6).
 */
public class Main {

    public static void main(String[] args) {
        // Apply system look-and-feel for native controls
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(Main::launchApp);
    }

    private static void launchApp() {
        // ── Main frame ───────────────────────────────────────────────────────
        JFrame frame = new JFrame("AR Pharmacy System");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setPreferredSize(new Dimension(1050, 700));
        frame.setLocationRelativeTo(null);

        // Close hook
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                DBConnection.close();
                System.exit(0);
            }
        });

        // ── CardLayout container ─────────────────────────────────────────────
        CardLayout cardLayout = new CardLayout();
        JPanel     container  = new JPanel(cardLayout);
        container.setBackground(Theme.BG_MAIN);

        // ── Build panels ─────────────────────────────────────────────────────
        AdminPanel    adminPanel    = new AdminPanel(cardLayout, container);
        CustomerPanel customerPanel = new CustomerPanel(cardLayout, container);
        LoginPanel    loginPanel    = new LoginPanel(frame, cardLayout, container);

        container.add(loginPanel,    "LOGIN");
        container.add(adminPanel,    "ADMIN");
        container.add(customerPanel, "CUSTOMER");

        frame.setContentPane(container);
        frame.pack();
        frame.setVisible(true);

        cardLayout.show(container, "LOGIN");

        // Verify DB connection and warn early if it fails
        new Thread(() -> {
            if (DBConnection.getConnection() == null) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(frame,
                        "⚠  Could not connect to MySQL database.\n\n" +
                        "Please check:\n" +
                        "  • MySQL server is running\n" +
                        "  • DBConnection.java has correct URL / user / password\n" +
                        "  • mysql-connector-j-*.jar is in your project classpath\n" +
                        "  • pharmacy_setup.sql has been executed",
                        "Database Connection Error",
                        JOptionPane.WARNING_MESSAGE));
            }
        }).start();
    }
}
