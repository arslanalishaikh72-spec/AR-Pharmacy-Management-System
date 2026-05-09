package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Add User Panel – inserts a new user record into the database.
 */
public class AddUserPanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;

    private JTextField     txtName, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cboRole;

    public AddUserPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("ADD_USER");
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(UIHelper.createHeader("AR Pharmacy System", "Add New User"), BorderLayout.NORTH);

        // ── Form card ────────────────────────────────────────────────────────
        JPanel card = UIHelper.createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 8, 8, 8);

        // Title
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.CENTER;
        JLabel title = new JLabel("Create User Account", SwingConstants.CENTER);
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.PRIMARY_DARK);
        card.add(title, gc);

        gc.gridwidth = 1; gc.anchor = GridBagConstraints.WEST;

        // Full Name
        addRow(card, gc, 1, "Full Name:", txtName     = UIHelper.createField(20));
        addRow(card, gc, 2, "Username:", txtUsername  = UIHelper.createField(20));
        addRow(card, gc, 3, "Password:", txtPassword  = UIHelper.createPasswordField(20));

        cboRole = UIHelper.createCombo("customer", "admin");
        addRow(card, gc, 4, "Role:", cboRole);

        // Buttons
        gc.gridy = 5; gc.gridx = 0; gc.gridwidth = 1; gc.insets = new Insets(20, 8, 8, 4);
        JButton btnSave = UIHelper.primaryBtn("💾  Save User");
        card.add(btnSave, gc);

        gc.gridx = 1; gc.insets = new Insets(20, 4, 8, 8);
        JButton btnBack = UIHelper.grayBtn("← Back");
        card.add(btnBack, gc);

        // Wrap card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BG_MAIN);
        wrapper.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // Actions
        btnSave.addActionListener(e -> saveUser());
        btnBack.addActionListener(e -> cardLayout.show(container, "ADMIN"));
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0;
        gc.insets = new Insets(8, 8, 8, 4);
        p.add(UIHelper.fieldLabel(lbl), gc);
        gc.gridx = 1; gc.weightx = 1;
        gc.insets = new Insets(8, 4, 8, 8);
        field.setPreferredSize(new Dimension(240, 36));
        p.add(field, gc);
    }

    private void saveUser() {
        String name     = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role     = (String) cboRole.getSelectedItem();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            UIHelper.error(this, "All fields are required.");
            return;
        }
        if (password.length() < 4) {
            UIHelper.error(this, "Password must be at least 4 characters.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO users(name, username, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();
            ps.close();

            UIHelper.success(this, "User '" + username + "' created successfully.");
            clearForm();
            cardLayout.show(container, "ADMIN");

        } catch (SQLIntegrityConstraintViolationException ex) {
            UIHelper.error(this, "Username '" + username + "' already exists.");
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        cboRole.setSelectedIndex(0);
    }
}
