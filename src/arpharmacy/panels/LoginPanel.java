package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

/**
 * Login Panel – validates credentials and routes to Admin or Customer panel.
 */
public class LoginPanel extends JPanel {

    private final JFrame        mainFrame;
    private final CardLayout    cardLayout;
    private final JPanel        container;

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JLabel         lblStatus;

    public LoginPanel(JFrame mainFrame, CardLayout cardLayout, JPanel container) {
        this.mainFrame  = mainFrame;
        this.cardLayout = cardLayout;
        this.container  = container;
        buildUI();
    }

    // ── UI Construction ───────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_MAIN);

        JPanel loginCard = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(36, 44, 36, 44)));
        loginCard.setPreferredSize(new Dimension(420, 480));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);

        // ── Logo Block ──────────────────────────────────────────────────────
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.CENTER;
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        logoPanel.setOpaque(false);
        JLabel iconLbl = new JLabel("✚");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        iconLbl.setForeground(Theme.PRIMARY);
        JLabel appName = new JLabel("AR Pharmacy");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appName.setForeground(Theme.PRIMARY_DARK);
        logoPanel.add(iconLbl);
        logoPanel.add(appName);
        gc.insets = new Insets(0, 0, 2, 0);
        loginCard.add(logoPanel, gc);

        gc.gridy = 1;
        JLabel tagline = new JLabel("System", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(Theme.TEXT_MID);
        loginCard.add(tagline, gc);

        gc.gridy = 2; gc.insets = new Insets(8, 0, 16, 0);
        loginCard.add(UIHelper.separator(), gc);

        // ── Sign-in label ───────────────────────────────────────────────────
        gc.gridy = 3; gc.insets = new Insets(0, 0, 18, 0);
        JLabel signIn = new JLabel("Sign in to your account");
        signIn.setFont(Theme.FONT_SUBTITLE);
        signIn.setForeground(Theme.TEXT_DARK);
        signIn.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(signIn, gc);

        gc.anchor = GridBagConstraints.WEST;
        gc.insets  = new Insets(4, 0, 2, 0);

        // ── Username ────────────────────────────────────────────────────────
        gc.gridy = 4;
        loginCard.add(UIHelper.fieldLabel("Username"), gc);

        gc.gridy = 5; gc.insets = new Insets(2, 0, 10, 0);
        txtUsername = UIHelper.createField(20);
        txtUsername.setPreferredSize(new Dimension(320, 38));
        loginCard.add(txtUsername, gc);

        // ── Password ────────────────────────────────────────────────────────
        gc.gridy = 6; gc.insets = new Insets(4, 0, 2, 0);
        loginCard.add(UIHelper.fieldLabel("Password"), gc);

        gc.gridy = 7; gc.insets = new Insets(2, 0, 18, 0);
        txtPassword = UIHelper.createPasswordField(20);
        txtPassword.setPreferredSize(new Dimension(320, 38));
        loginCard.add(txtPassword, gc);

        // ── Login Button ────────────────────────────────────────────────────
        gc.gridy = 8; gc.insets = new Insets(4, 0, 8, 0);
        gc.anchor = GridBagConstraints.CENTER;
        JButton btnLogin = UIHelper.loginBtn("Login  →");
        btnLogin.setPreferredSize(new Dimension(320, 42));
        loginCard.add(btnLogin, gc);

        // ── Status label ────────────────────────────────────────────────────
        gc.gridy = 9; gc.insets = new Insets(4, 0, 0, 0);
        lblStatus = new JLabel(" ");
        lblStatus.setFont(Theme.FONT_SMALL);
        lblStatus.setForeground(Theme.DANGER);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(lblStatus, gc);

        // ── Hint ────────────────────────────────────────────────────────────
        gc.gridy = 10; gc.insets = new Insets(16, 0, 0, 0);
        JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MID);
        loginCard.add(hint, gc);

        // ── Add card to background ──────────────────────────────────────────
        add(loginCard);

        // ── Action wiring ───────────────────────────────────────────────────
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
    }

    // ── Login logic ───────────────────────────────────────────────────────────
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter username and password.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                UIHelper.error(this, "Cannot connect to database.\nCheck DB settings in DBConnection.java");
                return;
            }

            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role   = rs.getString("role");
                int    userId = rs.getInt("user_id");
                String name   = rs.getString("name");
                lblStatus.setText(" ");
                txtPassword.setText("");
                txtUsername.setText("");

                if ("admin".equalsIgnoreCase(role)) {
                    AdminPanel panel = (AdminPanel) findPanel("ADMIN");
                    if (panel != null) panel.refresh(name);
                    cardLayout.show(container, "ADMIN");
                } else {
                    CustomerPanel panel = (CustomerPanel) findPanel("CUSTOMER");
                    if (panel != null) panel.refresh(name, userId);
                    cardLayout.show(container, "CUSTOMER");
                }
                mainFrame.setTitle("AR Pharmacy System  –  " + name + " (" + role + ")");
            } else {
                lblStatus.setText("Invalid username or password.");
            }
            rs.close(); ps.close();

        } catch (SQLException ex) {
            UIHelper.error(this, "Database error:\n" + ex.getMessage());
        }
    }

    /** Look up a named panel inside the card container. */
    private Component findPanel(String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName())) return c;
        }
        return null;
    }

    // ── Shadow border ─────────────────────────────────────────────────────────
    private static class ShadowBorder extends AbstractBorder {
        private static final int SIZE = 6;
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < SIZE; i++) {
                int alpha = (int)(60 * (1 - (double)i / SIZE));
                g2.setColor(new Color(0, 80, 40, alpha));
                g2.draw(new RoundRectangle2D.Double(x+i, y+i, w-i*2-1, h-i*2-1, 20-i, 20-i));
            }
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(SIZE,SIZE,SIZE,SIZE); }
    }
}
