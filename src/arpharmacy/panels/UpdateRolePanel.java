package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Update User Role Panel – select a user and change their role.
 */
public class UpdateRolePanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JComboBox<String> cboRole;

    public UpdateRolePanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("UPDATE_ROLE");
        buildUI();
    }

    public void loadUsers() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet  rs   = conn.createStatement().executeQuery("SELECT user_id, name, username, role FROM users ORDER BY user_id");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }
            rs.close();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(UIHelper.createHeader("AR Pharmacy System", "Update User Role"), BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Full Name", "Username", "Current Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        int[] widths = {50, 180, 150, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.BG_CARD);

        // Bottom action bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        actionBar.setBackground(Theme.BG_MAIN);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        actionBar.add(UIHelper.fieldLabel("New Role:"));
        cboRole = UIHelper.createCombo("admin", "customer");
        cboRole.setPreferredSize(new Dimension(130, 34));
        actionBar.add(cboRole);

        JButton btnUpdate = UIHelper.warningBtn("🔄  Update Role");
        JButton btnBack   = UIHelper.grayBtn("← Back");
        actionBar.add(btnUpdate);
        actionBar.add(btnBack);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.BG_MAIN);
        centre.setBorder(BorderFactory.createEmptyBorder(18, 24, 4, 24));
        centre.add(scroll, BorderLayout.CENTER);
        centre.add(actionBar, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        btnUpdate.addActionListener(e -> updateRole());
        btnBack.addActionListener(e   -> cardLayout.show(container, "ADMIN"));
    }

    private void updateRole() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.error(this, "Select a user first."); return; }

        int    userId = (int)    tableModel.getValueAt(row, 0);
        String name   = (String) tableModel.getValueAt(row, 1);
        String newRole = (String) cboRole.getSelectedItem();

        if (!UIHelper.confirm(this, "Change " + name + "'s role to '" + newRole + "'?")) return;

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET role=? WHERE user_id=?");
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ps.close();
            UIHelper.success(this, "Role updated successfully.");
            loadUsers();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }
}
